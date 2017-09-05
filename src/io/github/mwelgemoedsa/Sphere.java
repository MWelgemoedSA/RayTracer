package io.github.mwelgemoedsa;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.Vector;

public class Sphere implements SceneObject {
    private double radius;
    private Point3d center;
    private boolean litInternally;

    public Sphere(double radius, Point3d center) {
        this.radius = radius;
        this.center = center;

        this.litInternally = false;
    }

    public double rayIntersect(Ray ray) { //Returns -1 for no intersection
        Vector3d fromSphereCenter = new Vector3d();
        fromSphereCenter.sub(ray.getOrigin(), center);

        double discriminant = Math.pow(ray.getDirection().dot(fromSphereCenter), 2) - fromSphereCenter.lengthSquared() + Math.pow(radius, 2);
        if (discriminant < 0) return -1;

        double k = -(ray.getDirection().dot(fromSphereCenter));
        double d1 = k + Math.sqrt(discriminant);
        double d2 = k - Math.sqrt(discriminant);

        if (d1 > d2) return d2;
        return d1;
    }

    @Override
    public boolean isLitInternally() {
        return litInternally;
    }

    public Vector3d normalAtPoint(Vector3d point) {
        Vector3d normal = new Vector3d();
        normal.sub(point, center);
        normal.normalize();
        return normal;
    }

    public void setLitInternally(boolean litInternally) {
        this.litInternally = litInternally;
    }
}
