package io.github.mwelgemoedsa;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

class Raytracer {
    private int xSize = 1000;
    private int ySize = 1000;
    private int focalLength = 150;

    private ConcurrentHashMap<Point, Color> pixelMap;

    private ConcurrentHashMap<SceneObject, SurfaceHandler> objectMap;

    private ArrayList<Light> lightList;

    Raytracer() {
        pixelMap = new ConcurrentHashMap<>();
        objectMap = new ConcurrentHashMap<>();
        lightList = new ArrayList<>();

        int zPlane = 2000;
        int distBetween = 200;

        Sphere sun = new Sphere(50, new Point3d(0, 0, zPlane));
        sun.setLitInternally(true);

        SurfaceHandler white = new SurfaceHandler(Color.WHITE);
        white.setInternallyLit(true);
        objectMap.put(sun, white);

        //lightList.add(new PointLight(new Vector3d(0, 0, zPlane), 1));
        lightList.add(new CollimatedLight(new Vector3d(0, -1, 0)));

        SurfaceHandler red = new SurfaceHandler(Color.RED);

        int sphereSize = 20;
        objectMap.put(new Sphere(sphereSize, new Point3d(0, distBetween, zPlane)), red);
        objectMap.put(new Sphere(sphereSize, new Point3d(0, -distBetween, zPlane)), red);
        objectMap.put(new Sphere(sphereSize, new Point3d(distBetween, 0, zPlane)), red);
        objectMap.put(new Sphere(sphereSize, new Point3d(-distBetween, 0, zPlane)), red);

        sphereSize = 100;
        distBetween = 500;

        SurfaceHandler green = new SurfaceHandler(Color.GREEN);

        objectMap.put(new Sphere(sphereSize, new Point3d(0, distBetween, zPlane)), green);
        objectMap.put(new Sphere(sphereSize, new Point3d(0, -distBetween, zPlane)), green);
        objectMap.put(new Sphere(sphereSize, new Point3d(distBetween, 0, zPlane)), green);
        objectMap.put(new Sphere(sphereSize, new Point3d(-distBetween, 0, zPlane)), green);

        SurfaceHandler blue = new SurfaceHandler(Color.BLUE);

        objectMap.put(new Sphere(sphereSize, new Point3d(distBetween, distBetween, zPlane)), blue);
        objectMap.put(new Sphere(sphereSize, new Point3d(distBetween, -distBetween, zPlane)), blue);
        objectMap.put(new Sphere(sphereSize, new Point3d(-distBetween, distBetween, zPlane)), blue);
        objectMap.put(new Sphere(sphereSize, new Point3d(-distBetween, -distBetween, zPlane)), blue);
    }
    
    void drawOnImage(BufferedImage image) {
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, xSize, ySize);

        pixelMap.forEach((point2D, color) -> image.setRGB((int)point2D.getX(), (int)point2D.getY(), color.getRGB()));

        //drawGridLines(g2d);
    }

    private void drawGridLines(Graphics2D g2d) {
        g2d.setColor(Color.MAGENTA);
        g2d.drawRect(0, ySize/2, xSize, 0);
        g2d.drawRect(xSize/2, 0, 0, ySize);
    }

    int getXSize() {
        return xSize;
    }

    int getYSize() {
        return ySize;
    }

    void calculatePixel(int x, int y) {
        Color c = Color.BLACK;

        double ambient = 0.3;

        double bestIntersect = Double.MAX_VALUE;
        Vector3d intersectPoint = new Vector3d();
        Vector3d normal = null;
        SurfaceHandler surfaceHandler = null;

        for (Map.Entry<SceneObject, SurfaceHandler> entry : objectMap.entrySet()) {
            Ray ray = getRayAtPixel(x, y);
            double intersectDistance = entry.getKey().rayIntersect(ray);
            if (intersectDistance < 0) continue;
            if (intersectDistance < bestIntersect) {
                bestIntersect = intersectDistance;

                intersectPoint.scale(bestIntersect, ray.getDirection());
                normal = entry.getKey().normalAtPoint(intersectPoint);
                surfaceHandler = entry.getValue();
            }
        }

        double totalLight = 0;
        if (normal != null) {
            for (Light light : lightList) {
                Vector3d lightDir = light.getVectorFrom(intersectPoint);
                bestIntersect = lightDir.length();
                lightDir.normalize();
                double alignmentToLight = normal.dot(lightDir);

                //Check to see that the light is visible to us
                for (Map.Entry<SceneObject, SurfaceHandler> entry : objectMap.entrySet()) {
                    if (entry.getKey().isLitInternally()) continue;

                    Ray ray = new Ray(intersectPoint, lightDir);

                    double intersectDistance = entry.getKey().rayIntersect(ray);
                    if (intersectDistance < 0) continue;
                    if (intersectDistance < bestIntersect) {
                        alignmentToLight = 0;
                        break;
                    }
                }

                totalLight += Math.max(alignmentToLight, 0); //Can't have negative light;
            }
            totalLight += ambient;
            totalLight = Math.min(totalLight, 1);
        }

        if (surfaceHandler != null)
            c = surfaceHandler.getColor(totalLight);

        pixelMap.put(new Point(x, y), c);
    }

    private Ray getRayAtPixel(int x, int y) {
        double fov = Math.toRadians(45);

        double px = ((double)x - xSize/2 + 0.5) * 2 * Math.tan(fov/2) / xSize;
        double py = (ySize/2 - (double)y - 0.5) * 2 * Math.tan(fov/2) / ySize;

        Vector3d ray = new Vector3d(px, py, 1);
        ray.normalize();

        return new Ray(new Vector3d(0, 0, 0), ray);
    }

    void loadFile(String filePath) {
        SurfaceHandler surface = new SurfaceHandler(Color.WHITE);
        try {
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                ArrayList<Vector3d> vertices = new ArrayList<>();

                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                    String[] parts = line.split(" +");
                    assert(parts.length == 4);

                    if (parts[0].equals("v")) {
                        vertices.add(new Vector3d(
                                Double.parseDouble(parts[1]),
                                Double.parseDouble(parts[2]),
                                Double.parseDouble(parts[3]) + 300
                        ));
                    } else if (parts[0].equals("f")) {
                        Triangle triangle = new Triangle(
                                vertices.get(Integer.parseInt(parts[2])-1),
                                vertices.get(Integer.parseInt(parts[1])-1),
                                vertices.get(Integer.parseInt(parts[3])-1)
                        );
                        objectMap.put(triangle, surface);
                        System.out.println("New object " + triangle.toString());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
