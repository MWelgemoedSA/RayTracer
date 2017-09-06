package io.github.mwelgemoedsa;

import javax.vecmath.Vector3d;

public class PointLight implements Light {
    private Vector3d center;
    private double brightness; //1 to 0

    PointLight(Vector3d center, double brightness) {
        this.center = center;
        this.brightness = brightness;
    }

    public Vector3d getVectorFrom(Vector3d point) {
        Vector3d ray = new Vector3d();
        ray.sub(center, point);
         return ray;
    }

    public double getBrightness() {
        return brightness;
    }
}
