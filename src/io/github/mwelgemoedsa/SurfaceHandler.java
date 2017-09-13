package io.github.mwelgemoedsa;

import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.ArrayList;

public class SurfaceHandler {
    private Color color;
    private boolean internallyLit;
    private boolean isReflective;

    public SurfaceHandler(Color color) {
        this.color = color;
    }

    Color getColor(double illumination) { //illumination goes from 0-1
        if (internallyLit) illumination = 1;

        illumination = Math.max(0, illumination);
        illumination = Math.min(illumination, 1);

        int R = (int) (this.color.getRed() * illumination);
        int G = (int) (this.color.getGreen() * illumination);
        int B = (int) (this.color.getBlue() * illumination);

        return new Color(R, G, B);
    }

    public void setInternallyLit(boolean internallyLit) {
        this.internallyLit = internallyLit;
    }

    public boolean isReflective() {
        return isReflective;
    }

    public void setReflective(boolean reflective) {
        isReflective = reflective;
    }
}
