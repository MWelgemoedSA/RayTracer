package io.github.mwelgemoedsa;

import javax.vecmath.Vector3d;

public class Ray {
    private Vector3d origin;
    private Vector3d direction;

    Ray(Vector3d origin, Vector3d direction) {
        this.origin = origin;
        this.direction = direction;
    }

    @Override
    public String toString() {
        return "Ray{" +
                "origin=" + origin +
                ", direction=" + direction +
                '}';
    }

    Vector3d getOrigin() {
        return origin;
    }

    Vector3d getDirection() {
        return direction;
    }
}
