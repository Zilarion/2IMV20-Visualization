package volvis;

import util.Interpolate;
import util.VectorMath;
import volume.GradientVolume;
import volume.Volume;
import volume.VoxelGradient;

import java.awt.image.BufferedImage;

/**
 * Created by ruudandriessen on 27/11/16.
 */
public abstract class RaycastWorker extends Thread {
    int startH, endH;
    boolean interactive;
    double maxRange;
    int imageCenter;
    double[] viewVec = new double[3],
            uVec = new double[3],
            vVec = new double[3],
            volumeCenter = new double[3];
    BufferedImage image;
    Volume volume;
    GradientVolume gradients;

    public RaycastWorker(int startH, int endH, Volume volume, GradientVolume gradients, BufferedImage target, double[] viewMatrix, boolean interactive) {
        image = target;
        this.interactive= interactive;
        this.volume = volume;
        this.gradients = gradients;

        imageCenter = image.getWidth()/2;

        this.startH = startH;
        this.endH = endH;

        VectorMath.setVector(viewVec, viewMatrix[2], viewMatrix[6], viewMatrix[10]);
        VectorMath.setVector(uVec, viewMatrix[0], viewMatrix[4], viewMatrix[8]);
        VectorMath.setVector(vVec, viewMatrix[1], viewMatrix[5], viewMatrix[9]);

        // Get the highest dimension of the volume
        maxRange = volume.getDiagonal();

        // Calculate volume center
        VectorMath.setVector(volumeCenter, volume.getDimX() / 2, volume.getDimY() / 2, volume.getDimZ() / 2);
    }

    protected void setColor(int x, int y, TFColor c) {
        int width = image.getWidth();
        int height = image.getHeight();
        if (interactive) {
            for (int m = -2; m < 2; m++) {
                for (int n = -2; n < 2; n++) {
                    if (x+n > 0 && x+n < width && y+m > 0 && y+m < height)
                        image.setRGB(x + n,y + m, c.toARGB());
                }
            }
        } else {
            image.setRGB(x, y, c.toARGB());
        }
    }

    protected short interVoxel(double[] coord) {
        if (coord[0] < 0 || coord[0] > volume.getDimX() || coord[1] < 0 || coord[1] > volume.getDimY()
                || coord[2] < 0 || coord[2] > volume.getDimZ()) {
            return 0;
        }

        int xMin = (int) Math.floor(coord[0]);
        int yMin = (int) Math.floor(coord[1]);
        int zMin = (int) Math.floor(coord[2]);
        int xMax = (int) Math.ceil(coord[0]);
        int yMax = (int) Math.ceil(coord[1]);
        int zMax = (int) Math.ceil(coord[2]);

        if (xMax > volume.getDimX() - 1 || yMax > volume.getDimY() - 1 || zMax > volume.getDimZ() - 1) {
            xMin = xMax > volume.getDimX() - 1 ? volume.getDimX() - 1 : xMin;
            yMin = yMax > volume.getDimY() - 1 ? volume.getDimY() - 1 : yMin;
            zMin = zMax > volume.getDimZ() - 1 ? volume.getDimZ() - 1 : zMin;
            return volume.getVoxel(xMin, yMin, zMin);
        }

        short value = Interpolate.triLerp(
                coord[0]/volume.getDimX(), coord[1]/volume.getDimY(), coord[2]/volume.getDimZ(),
                volume.getVoxel(xMin, yMin, zMin),
                volume.getVoxel(xMin, yMax, zMin),
                volume.getVoxel(xMin, yMin, zMax),
                volume.getVoxel(xMin, yMax, zMax),
                volume.getVoxel(xMax, yMin, zMin),
                volume.getVoxel(xMax, yMax, zMin),
                volume.getVoxel(xMax, yMin, zMax),
                volume.getVoxel(xMax, yMax, zMax)
        );

        return value;
    }


    public TFColor phong(TFColor in, double[] p) {
        if (p[0] < 0 || p[1] < 0 || p[2] < 0 || p[0] > volume.getDimX() || p[1] > volume.getDimY() || p[2] > volume.getDimZ() ) { return in; }

        double k_ambient = 0.3, k_diff = 0.7, k_spec = 0.8, alpha = 5;
        TFColor out = new TFColor(in.r, in.g, in.b, in.a);

        double[] L = viewVec; // L = R, light comes from our view vec (reversed?)
        double[] R = viewVec;

        // Calculate normal of this point according to
        double[] N = new double[3];
        VoxelGradient gradient = gradients.getGradient((int) Math.floor(p[0]), (int) Math.floor(p[1]), (int) Math.floor(p[2]));

        if (gradient.mag > 0.0 && in.a > 0.0) {
            // Filling N-Vector:
            N[0] = gradient.x / gradient.mag;
            N[1] = gradient.y / gradient.mag;
            N[2] = gradient.z / gradient.mag;

            // Computing required dot products
            double ln = Math.abs(VectorMath.dotproduct(L, N));
            double nr = Math.abs(VectorMath.dotproduct(N, R));

            if (ln > 0 && nr > 0) {
                out.r = in.r * (k_ambient + (k_diff * ln) + (k_spec * Math.pow(nr, alpha)));
                out.g = in.g * (k_ambient + (k_diff * ln) + (k_spec * Math.pow(nr, alpha)));
                out.b = in.b * (k_ambient + (k_diff * ln) + (k_spec * Math.pow(nr, alpha)));
            }
        }

        return out;
    }
}
