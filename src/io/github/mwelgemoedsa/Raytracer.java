package io.github.mwelgemoedsa;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

class Raytracer {
    private int xSize = 300;
    private int ySize = 300;
    private int focalLength = 150;

    private ConcurrentHashMap<Point, Color> pixelMap;

    private ConcurrentHashMap<SceneObject, Color> objectMap;

    private ArrayList<Light> lightList;

    Raytracer() {
        pixelMap = new ConcurrentHashMap<>();
        objectMap = new ConcurrentHashMap<>();
        lightList = new ArrayList<>();

        int zPlane = 2000;
        int distBetween = 200;

        Sphere sun = new Sphere(50, new Point3d(0, 0, zPlane));
        sun.setLitInternally(true);
        objectMap.put(sun, Color.WHITE);

        lightList.add(new Light(new Vector3d(0, 0, zPlane), 1));

        int sphereSize = 50;
        objectMap.put(new Sphere(sphereSize, new Point3d(0, distBetween, zPlane)), Color.RED);
        objectMap.put(new Sphere(sphereSize, new Point3d(0, -distBetween, zPlane)), Color.RED);
        objectMap.put(new Sphere(sphereSize, new Point3d(distBetween, 0, zPlane)), Color.RED);
        objectMap.put(new Sphere(sphereSize, new Point3d(-distBetween, 0, zPlane)), Color.RED);

        sphereSize = 100;
        distBetween = 500;

        objectMap.put(new Sphere(sphereSize, new Point3d(0, distBetween, zPlane)), Color.GREEN);
        objectMap.put(new Sphere(sphereSize, new Point3d(0, -distBetween, zPlane)), Color.GREEN);
        objectMap.put(new Sphere(sphereSize, new Point3d(distBetween, 0, zPlane)), Color.GREEN);
        objectMap.put(new Sphere(sphereSize, new Point3d(-distBetween, 0, zPlane)), Color.GREEN);

        objectMap.put(new Sphere(sphereSize, new Point3d(distBetween, distBetween, zPlane)), Color.BLUE);
        objectMap.put(new Sphere(sphereSize, new Point3d(distBetween, -distBetween, zPlane)), Color.BLUE);
        objectMap.put(new Sphere(sphereSize, new Point3d(-distBetween, distBetween, zPlane)), Color.BLUE);
        objectMap.put(new Sphere(sphereSize, new Point3d(-distBetween, -distBetween, zPlane)), Color.BLUE);
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

        double ambient = 0.1;

        double bestIntersect = Double.MAX_VALUE;
        Vector3d intersectPoint = new Vector3d();
        Vector3d normal = null;
        boolean litInternally = false;

        for (Map.Entry<SceneObject, Color> entry : objectMap.entrySet()) {
            Vector3d ray = getRayAtPixel(x, y);
            double intersectDistance = entry.getKey().rayIntersect(ray);
            if (intersectDistance < 0) continue;
            if (intersectDistance < bestIntersect) {
                bestIntersect = intersectDistance;
                c = entry.getValue();

                intersectPoint.scale(bestIntersect, ray);
                normal = entry.getKey().normalAtPoint(intersectPoint);
                litInternally = entry.getKey().isLitInternally();
            }
        }

        double totalLight = 0;
        if (litInternally) {
            totalLight = 1;
        } else {
            if (normal != null) {
                for (Light light : lightList) {
                    double alignmentToLight = normal.dot(light.getRayTo(intersectPoint));

                    alignmentToLight *= -1;
                    totalLight += Math.max(alignmentToLight, 0); //Can't have negative light;
                }
            }
            totalLight += ambient;
            totalLight = Math.min(totalLight, 1);
        }

        Color l = new Color((int)(c.getRed() * totalLight), (int) (c.getGreen() * totalLight), (int) (c.getBlue() * totalLight));

        pixelMap.put(new Point(x, y), l);
    }

    private Vector3d getRayAtPixel(int x, int y) {
        double fov = Math.toRadians(45);

        double px = ((double)x - xSize/2 + 0.5) * 2 * Math.atan(fov/2) / xSize;
        double py = (ySize/2 - (double)y + 0.5) * 2 * Math.atan(fov/2) / ySize;

        Vector3d ray = new Vector3d(px, py, 1);
        ray.normalize();

        return ray;
    }

    void loadFile(String filePath) {
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
                        objectMap.put(triangle, Color.white);
                        System.out.println("New object " + triangle.toString());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
