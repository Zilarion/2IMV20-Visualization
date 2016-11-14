package util;

/**
 * Created by ruudandriessen on 14/11/16.
 */
public class Interpolate {
    public static double lerp(double x, double q00, double q01) {
        return (1 - x) * q00 + x * q01;
    }

    public static double triLerp(double x, double y, double z, double q000, double q001, double q010, double q011, double q100, double q101, double q110, double q111) {
        double x00 = lerp(x, q000, q100);
        double x10 = lerp(x, q010, q110);
        double x01 = lerp(x, q001, q101);
        double x11 = lerp(x, q011, q111);
        double r0 = lerp(y, x00, x01);
        double r1 = lerp(y, x10, x11);

        return lerp(z, r0, r1);
    }
}
