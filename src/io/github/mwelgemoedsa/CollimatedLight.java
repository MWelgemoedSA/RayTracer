package io.github.mwelgemoedsa;

import javax.vecmath.Vector3d;

public class CollimatedLight implements Light {
    private Vector3d direction;

    public CollimatedLight(Vector3d direction) {
        this.direction = direction;
        direction.scale(-1); //Invert because we care about a vector to the light
    }

    @Override
    public Vector3d getVectorFrom(Vector3d point) {
        return direction;
    }
}
