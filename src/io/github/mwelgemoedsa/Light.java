package io.github.mwelgemoedsa;

import javax.vecmath.Vector3d;

public class Light {
    private Vector3d center;
    private double brightness; //1 to 0

    public Light(Vector3d center, double brightness) {
        this.center = center;
        this.brightness = brightness;
    }

    Vector3d getRayTo(Vector3d point) {
        Vector3d ray = new Vector3d();
        ray.sub(point, center);
        ray.normalize();
        return ray;
    }

    public double getBrightness() {
        return brightness;
    }
}
