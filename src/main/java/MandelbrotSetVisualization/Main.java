package MandelbrotSetVisualization;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class Main {

    public static final int w = 1920;
    public static final int h = 1056;
    public static final int size = 1024;

    private static double minX = -2.0;
    private static double minY = -1.0;
    private static double maxX = 1.0;
    private static double maxY = 1.0;

    private static BufferedImage image;
    private static JFrame frame;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            JLabel label = new JLabel(new ImageIcon(image));
            label.addMouseListener(mouseListener());

            frame = new JFrame("This is a Visualization for Mandelbrot Set");
            frame.add(label);
            frame.pack();
            frame.setVisible(true);
            frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            calculateImageWithParallelExe();
        });
    }

    private static MouseListener mouseListener() {
        return new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent event) {
                double zoomFactor = 0.5;
                double newX = (event.getX() / (double) w) * (maxX - minX) + minX;
                double newY = (event.getY() / (double) h) * (maxY - minY) + minY;
                if (event.getButton() == MouseEvent.BUTTON1) {
                    // zoom in
                    zoomFactor = 0.5;
                } else if (event.getButton() == MouseEvent.BUTTON3) {
                    // zoom out
                    zoomFactor = 2.0;
                }
                double newWidth = (maxX - minX) * zoomFactor;
                double newHeight = (maxY - minY) * zoomFactor;
                minX = newX - newWidth / 2;
                maxX = newX + newWidth / 2;
                minY = newY - newHeight / 2;
                maxY = newY + newHeight / 2;
                calculateImageWithParallelExe();
            }
        };
    }

    // If you want to use only one single Thread uncomment the line below and comment the line NO.72
    // private static Executor executor = Executors.newSingleThreadExecutor();
    private static Executor executor = Executors.newFixedThreadPool(6);
    // This line above gives us the option to make a pool of Threads and run multiple Threads on the same pool
   private static void calculateImageWithParallelExe() {
        executor.execute(() -> {
            displayThreadInfo();
            long startTime = System.nanoTime();
            IntStream.range(0, h).parallel().forEach(y -> {
                double ci = (y / (double) h) * (maxY - minY) + minY;
                for (int x = 0; x < w; ++x) {
                    double cr = (x / (double) w) * (maxX - minX) + minX;
                    double zr = 0.0, zi = 0.0;
                    int color = 0x000000;
                    int i;
                    for (i = 0; i < size; ++i) {
                        double zrzr = zr * zr;
                        double zizi = zi * zi;
                        if (zrzr + zizi >= 4) {
                            // Then c is outside --> Blow Up
                            color = arrayOfColors[i & 15];
                            break;
                        }
                        zi = 2.0 * zr * zi + ci;
                        zr = zrzr - zizi + cr;
                    }
                    image.setRGB(x, y, color);
                }
            });
            long endTime = System.nanoTime();
            long duration = (endTime - startTime);
            System.out.println("Parallel Execution Time: " + duration / 1_000_000 + " ms");

            frame.repaint();
        });
    }

    private static void calculateImageWithSerialExe() {
       displayThreadInfo();
       long startTime = System.nanoTime();
        for (int y = 0; y < h; ++y) {
            double ci = (y / (double) h) * (maxY - minY) + minY;
            for (int x = 0; x < w; ++x) {
                double cr = (x / (double) w) * (maxX - minX) + minX;
                double zr = 0.0, zi = 0.0;
                int color = 0x000000; // Black for mandelbrot
                int i;
                for (i = 0; i < size; ++i) {
                    double zrzr = zr * zr;
                    double zizi = zi * zi;
                    if (zrzr + zizi >= 4) {
                        color = arrayOfColors[i & 15];
                        break;
                    }
                    zi = 2.0 * zr * zi + ci;
                    zr = zrzr - zizi + cr;
                }
                image.setRGB(x, y, color);
            }
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("Serial Execution Time: " + duration / 1_000_000 + " ms");

        frame.repaint();
    }


    private static void displayThreadInfo() {
        System.out.println(Thread.currentThread());
        Arrays.stream(Thread.currentThread().getStackTrace())
                .skip(2)
                .limit(4)
                .forEach(System.out::println);
        System.out.println();
    }

    private static final int[] arrayOfColors = {
            0x00421E0F, 0x0019071A, 0x0009012F, 0x00040449,
            0x00000764, 0x000C2C8A, 0x001852B1, 0x00397DD1,
            0x0086B5E5, 0x00D3ECF8, 0x00F1E9BF, 0x00F8C95F,
            0x00FFAA00, 0x00CC8000, 0x00995700, 0x006A3403
    };

}
