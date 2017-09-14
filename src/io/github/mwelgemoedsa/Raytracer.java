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
    private int xSize = 300;
    private int ySize = 300;
    private int focalLength = 150;

    private ConcurrentHashMap<Point, Color> pixelMap;

    private ConcurrentHashMap<SceneObject, SurfaceHandler> objectMap;

    private ArrayList<Light> lightList;

    private Color defaultColour;

    Raytracer() {
        pixelMap = new ConcurrentHashMap<>();
        objectMap = new ConcurrentHashMap<>();
        lightList = new ArrayList<>();

        defaultColour = new Color(70,130,180);

        int zPlane = 2000;
        int distBetween = 200;

        Sphere sun = new Sphere(50, new Point3d(0, 0, zPlane));
        sun.setLitInternally(true);


        //lightList.add(new PointLight(new Vector3d(0, 0, zPlane), 1));
        lightList.add(new CollimatedLight(new Vector3d(-1, -1, 1)));

        SurfaceHandler red = new SurfaceHandler(Color.RED);

        int sphereSize = 20;
        objectMap.put(new Sphere(sphereSize, new Point3d(0, distBetween, zPlane)), red);
        //objectMap.put(new Sphere(sphereSize, new Point3d(0, -distBetween, zPlane)), red);
        objectMap.put(new Sphere(sphereSize, new Point3d(distBetween, 0, zPlane)), red);
        objectMap.put(new Sphere(sphereSize, new Point3d(-distBetween, 0, zPlane)), red);

        sphereSize = 100;
        distBetween = 500;

        SurfaceHandler green = new SurfaceHandler(Color.GREEN);

        objectMap.put(new Sphere(sphereSize, new Point3d(0, distBetween, zPlane)), green);
        //objectMap.put(new Sphere(sphereSize, new Point3d(0, -distBetween, zPlane)), green);
        objectMap.put(new Sphere(sphereSize, new Point3d(distBetween, 0, zPlane)), green);
        objectMap.put(new Sphere(sphereSize, new Point3d(-distBetween, 0, zPlane)), green);

        SurfaceHandler blue = new SurfaceHandler(Color.BLUE);

        objectMap.put(new Sphere(sphereSize, new Point3d(distBetween, distBetween, zPlane)), blue);
        //objectMap.put(new Sphere(sphereSize, new Point3d(distBetween, -distBetween, zPlane)), blue);
        objectMap.put(new Sphere(sphereSize, new Point3d(-distBetween, distBetween, zPlane)), blue);
        //objectMap.put(new Sphere(sphereSize, new Point3d(-distBetween, -distBetween, zPlane)), blue);

        Triangle triangle = new Triangle(
                new Vector3d(-distBetween*2, -distBetween, zPlane*1.5),
                new Vector3d(distBetween*2,  -distBetween, zPlane*1.5),
                new Vector3d(0,         -distBetween, zPlane/2));
        SurfaceHandler mirror = new SurfaceHandler(Color.cyan);
        mirror.setReflective(true);
        objectMap.put(triangle, mirror);
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
        Color c = defaultColour;

        double ambient = 0.3;

        Vector3d intersectPoint = new Vector3d();

        Ray ray = getRayAtPixel(x, y);
        SceneObject intersectObject = getFirstIntersection(ray, intersectPoint);

        double totalLight = 0;
        if (intersectObject != null) { //We hit something, now calculate the light intensity
            Vector3d objectNormal = intersectObject.normalAtPoint(intersectPoint);

            for (Light light : lightList) { //Check each light to see if we are illuminated by it
                Vector3d lightDir = light.getVectorFrom(intersectPoint);
                lightDir.normalize();

                double alignmentToLight = objectNormal.dot(lightDir);

                //Check to see that the light is visible to us
                if (alignmentToLight > 0) {
                    for (Map.Entry<SceneObject, SurfaceHandler> entry : objectMap.entrySet()) {
                        if (entry.getKey().isLitInternally()) continue;
                        if (entry.getKey() == intersectObject) continue;

                        Ray shadowRay = new Ray(intersectPoint, lightDir);
                        //Check if there is something between us and this light

                        Vector3d intersectionPoint = new Vector3d();
                        SceneObject lightBlockingObject = getFirstIntersection(shadowRay, intersectionPoint);
                        if (lightBlockingObject != null) {
                            alignmentToLight = 0;
                            break;
                        }
                    }
                }

                totalLight += Math.max(alignmentToLight, 0); //Can't have negative light;
            }
            totalLight += ambient;
            totalLight = Math.min(totalLight, 1);
        }

        if (intersectObject != null) {
            SurfaceHandler surfaceHandler = objectMap.get(intersectObject);
            if (surfaceHandler != null)
                c = surfaceHandler.getColor(totalLight);
        }

        pixelMap.put(new Point(x, y), c);
    }

    //Gets the first intersection of an object along the ray, or null if there is none
    //Also fills intersectionPoint with the xyz of the intersection
    private SceneObject getFirstIntersection(Ray ray, Vector3d intersectionPoint) {
        double bestIntersect = Double.MAX_VALUE;

        SceneObject intersectObject = null;
        for (Map.Entry<SceneObject, SurfaceHandler> entry : objectMap.entrySet()) {
            SceneObject sceneObject = entry.getKey();
            double intersectDistance = sceneObject.rayIntersect(ray);
            if (intersectDistance < 0) continue;
            if (intersectDistance < bestIntersect) {
                bestIntersect = intersectDistance;

                if (intersectionPoint != null) {
                    intersectionPoint.scale(bestIntersect, ray.getDirection());
                    intersectionPoint.add(ray.getOrigin());
                }
                intersectObject = sceneObject;
            }
        }

        return intersectObject;
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
