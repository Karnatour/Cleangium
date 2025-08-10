package net.fabricmc.fabric.impl.client.indigo.renderer.helper;

public class MathHelp {
    public static boolean equalsApproximate(float a, float b) {
        return Math.abs(b - a) < 1.0E-5F;
    }

    public static boolean equalsApproximate(double a, double b) {
        return Math.abs(b - a) < (double) 1.0E-5F;
    }

}
