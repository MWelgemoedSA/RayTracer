package io.github.mwelgemoedsa;

import javax.vecmath.Vector3d;

public interface SceneObject {
    Vector3d normalAtPoint(Vector3d point);
    double rayIntersect(Ray ray);

    boolean isLitInternally();
}
