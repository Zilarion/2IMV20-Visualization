package util;

/**
 * Created by ruudandriessen on 14/11/16.
 */
public class Interpolate {
    public static short lerp(double x, int q00, int q01) {
        return (short) ((1.0 - x) * q00 + x  * q01);
    }

    public static short triLerp(double x, double y, double z, int q000, int q001, int q010, int q011, int q100, int q101, int q110, int q111) {
        short x00 = lerp(x, q000, q100);
        short x10 = lerp(x, q010, q110);
        short x01 = lerp(x, q001, q101);
        short x11 = lerp(x, q011, q111);
        short r0 = lerp(y, x00, x01);
        short r1 = lerp(y, x10, x11);

        return lerp(z, r0, r1);
    }
    
    public static short lerp(double x, float q00, float q01) {
        return (short) ((1.0 - x) * q00 + x  * q01);
    }

    public static short triLerp(double x, double y, double z, float q000, float q001, float q010, float q011, float q100, float q101, float q110, float q111) {
        short x00 = lerp(x, q000, q100);
        short x10 = lerp(x, q010, q110);
        short x01 = lerp(x, q001, q101);
        short x11 = lerp(x, q011, q111);
        short r0 = lerp(y, x00, x01);
        short r1 = lerp(y, x10, x11);

        return lerp(z, r0, r1);
    }
}
