package io.github.mwelgemoedsa;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

public interface SceneObject {
    Vector3d normalAtPoint(Vector3d point);
    double rayIntersect(Ray ray);
;

    Vector2d calculateTextureCoordinates(Vector3d pointOnSurface);
}
