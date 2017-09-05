package io.github.mwelgemoedsa;

import javax.vecmath.Vector3d;

public class Ray {
    private Vector3d origin;
    private Vector3d direction;

    public Ray(Vector3d origin, Vector3d direction) {
        this.origin = origin;
        this.direction = direction;
    }

    public Vector3d getOrigin() {
        return origin;
    }

    public Vector3d getDirection() {
        return direction;
    }
}
