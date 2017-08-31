package io.github.mwelgemoedsa;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
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

    ConcurrentHashMap<Sphere, Color> sphereMap;
    
    Raytracer() {
        pixelMap = new ConcurrentHashMap<>();
        sphereMap = new ConcurrentHashMap<>();

        sphereMap.put(new Sphere(100, new Point3d(0, 0, 400)), Color.GREEN);
        sphereMap.put(new Sphere(500, new Point3d(100, 100, 1000)), Color.BLUE);
        sphereMap.put(new Sphere(50, new Point3d(-300, -300, 400)), Color.RED);
    }
    
    void drawOnImage(BufferedImage image) {
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, xSize, ySize);

        pixelMap.forEach((point2D, color) -> image.setRGB((int)point2D.getX(), (int)point2D.getY(), color.getRGB()));

        drawGridLines(g2d);
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

        double bestIntersect = Double.MAX_VALUE;
        for (Map.Entry<Sphere, Color> entry : sphereMap.entrySet()) {
            double intersectDistance = entry.getKey().rayIntersect(getRayAtPixel(x, y));
            if (intersectDistance < 0) continue;
            if (intersectDistance < bestIntersect) {
                bestIntersect = intersectDistance;
                c = entry.getValue();
            }
        }

        pixelMap.put(new Point(x, y), c);
    }

    Vector3d getRayAtPixel(int x, int y) {
        Vector3d ray = new Vector3d(x - xSize/2, y - ySize/2, focalLength);
        ray.normalize();
        return ray;
    }
}
