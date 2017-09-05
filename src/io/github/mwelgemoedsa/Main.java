package io.github.mwelgemoedsa;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

class ImageHolder extends JPanel {
    private BufferedImage image;

    ImageHolder(BufferedImage image) {
        this.image = image;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.drawImage(image, 0, 0, this);
    }

    public Dimension getPreferredSize() {
        return new Dimension(image.getWidth(), image.getHeight());
    }

    BufferedImage getImage() {
        return image;
    }
}

public class Main extends  JFrame {
    private ImageHolder imageHolder;
    private final Raytracer raytracer;

    private Main() {
        raytracer = new Raytracer();
        raytracer.loadFile("teapot.obj");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BufferedImage image = new BufferedImage(raytracer.getXSize(), raytracer.getYSize(), BufferedImage.TYPE_INT_RGB);
        this.imageHolder = new ImageHolder(image);
        add(imageHolder);

        setTitle("Maze drawer");
        setSize(raytracer.getXSize(), raytracer.getYSize());
        setLocationRelativeTo(null);
        setVisible(true);

        Timer redrawTimer = new Timer(100, actionEvent -> redraw());
        redrawTimer.setInitialDelay(0);
        redrawTimer.start();

        new Thread(this::recalculateAllPixels).start();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(Main::new);
    }

    private void redraw() {
        raytracer.drawOnImage(imageHolder.getImage());
        imageHolder.repaint();
    }

    private void recalculateAllPixels() {
        for (int i = 0; i != raytracer.getXSize(); i++) {
            for (int j = 0; j < raytracer.getYSize(); j++) {
                raytracer.calculatePixel(i, j);
            }
        }

        File outputfile = new File("saved.png");
        try {
            ImageIO.write(imageHolder.getImage(), "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
