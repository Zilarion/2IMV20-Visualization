package util;

/**
 * Created by ruudandriessen on 14/11/16.
 */
public class Interpolate {
    public static short lerp(double x, double q00, short q01) {
        return (short) ((1.0 - x) * q00 + x  * q01);
    }

    public static short triLerp(double x, double y, double z, short q000, short q001, short q010, short q011, short q100, short q101, short q110, short q111) {
        short x00 = lerp(x, q000, q100);
        short x10 = lerp(x, q010, q110);
        short x01 = lerp(x, q001, q101);
        short x11 = lerp(x, q011, q111);
        short r0 = lerp(y, x00, x01);
        short r1 = lerp(y, x10, x11);

        return lerp(z, r0, r1);
    }
}
