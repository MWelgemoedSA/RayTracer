package io.github.mwelgemoedsa;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

class Raytracer {
    private int xSize = 300;
    private int ySize = 300;
    private int focalLength = 150;

    ConcurrentHashMap<Point, Color> pixelMap;

    ConcurrentHashMap<SceneObject, Color> objectMap;
    
    Raytracer() {
        pixelMap = new ConcurrentHashMap<>();
        objectMap = new ConcurrentHashMap<>();

        objectMap.put(new Sphere(100, new Point3d(0, 0, 1600)), Color.GREEN);
        objectMap.put(new Sphere(500, new Point3d(100, 100, 4000)), Color.BLUE);
        objectMap.put(new Sphere(50, new Point3d(-300, -300, 1600)), Color.RED);

        objectMap.put(
                new Triangle(
                        new Vector3d(-500, -100, 2000),
                        new Vector3d(-400, -100, 2000),
                        new Vector3d(-450, 100, 2100)
                        ), Color.MAGENTA);
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

    public int getXSize() {
        return xSize;
    }

    public int getYSize() {
        return ySize;
    }

    void calculatePixel(int x, int y) {
        Color c = Color.BLACK;

        Vector3d lightDirection = new Vector3d(-1, -2,  0);
        lightDirection.normalize();
        double ambient = 0.2;

        double bestIntersect = Double.MAX_VALUE;
        double alignmentToLight = 0;
        for (Map.Entry<SceneObject, Color> entry : objectMap.entrySet()) {
            Vector3d ray = getRayAtPixel(x, y);
            double intersectDistance = entry.getKey().rayIntersect(ray);
            if (intersectDistance < 0) continue;
            if (intersectDistance < bestIntersect) {
                bestIntersect = intersectDistance;
                c = entry.getValue();

                Vector3d intersectPoint = new Vector3d();
                intersectPoint.scale(bestIntersect, ray);
                Vector3d normal = entry.getKey().normalAtPoint(intersectPoint);
                alignmentToLight = normal.dot(lightDirection);

            }
        }
        alignmentToLight = Math.max(alignmentToLight, 0); //Can't have negative light

        alignmentToLight += ambient;
        alignmentToLight = Math.min(alignmentToLight, 1);

        Color l = new Color((int)(c.getRed() * alignmentToLight), (int) (c.getGreen() * alignmentToLight), (int) (c.getBlue() * alignmentToLight));

        pixelMap.put(new Point(x, y), l);
    }

    Vector3d getRayAtPixel(int x, int y) {
        double fov = Math.toRadians(45);

        double px = ((double)x - xSize/2 + 0.5) * 2 * Math.atan(fov/2) / xSize;
        double py = ((double)y - ySize/2 + 0.5) * 2 * Math.atan(fov/2) / ySize;

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
