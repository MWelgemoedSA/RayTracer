package io.github.mwelgemoedsa;

import javax.imageio.ImageIO;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SurfaceHandler {
    private Color color;

    private boolean internallyLit;
    private boolean isReflective;
    private BufferedImage texture;

    public SurfaceHandler(Color color) {
        this.color = color;
    }

    Color getColor(SceneObject source, Vector3d point, double illumination) { //illumination goes from 0-1
        if (texture != null) return handleTexture(source, point, illumination);
        if (internallyLit) illumination = 1;

        illumination = Math.max(0, illumination);
        illumination = Math.min(illumination, 1);

        int R = (int) (this.color.getRed() * illumination);
        int G = (int) (this.color.getGreen() * illumination);
        int B = (int) (this.color.getBlue() * illumination);

        return new Color(R, G, B);
    }

    private Color handleTexture(SceneObject source, Vector3d point, double illumination) {
        Vector2d textureCoords = source.calculateTextureCoordinates(point);
        int x = (int) (texture.getWidth() * textureCoords.x);
        int y = (int) (texture.getHeight() * textureCoords.y);

        x %= texture.getWidth();
        y %= texture.getHeight();

        //System.out.println(textureCoords + " " + x + "  " + y  + " " + texture.getHeight() + " " + texture.getWidth());
        Color c = new Color(texture.getRGB(x, y));
        return scaleColor(c, illumination);
    }

    static Color scaleColor(Color c, double s) {
        int R = (int) (c.getRed() * s);
        int G = (int) (c.getGreen() * s);
        int B = (int) (c.getBlue() * s);

        R = Math.min(R, 255);
        G = Math.min(G, 255);
        B = Math.min(B, 255);

        return new Color(R, G, B);
    }

    public void setInternallyLit(boolean internallyLit) {
        this.internallyLit = internallyLit;
    }

    public boolean isInternallyLit() {
        return internallyLit;
    }

    public boolean isReflective() {
        return isReflective;
    }

    public void setReflective(boolean reflective) {
        isReflective = reflective;
    }

    public void loadImage(String path) {
        try {
            texture = ImageIO.read(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
