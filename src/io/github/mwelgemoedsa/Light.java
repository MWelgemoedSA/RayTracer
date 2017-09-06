package io.github.mwelgemoedsa;

import javax.vecmath.Vector3d;

public interface Light {
    Vector3d getVectorFrom(Vector3d point);
}
