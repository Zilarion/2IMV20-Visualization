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
    boolean illuminate;

    public RaycastWorker(int startH, int endH, Volume volume, GradientVolume gradients, BufferedImage target, double[] viewMatrix, boolean interactive, boolean illuminate) {
        image = target;
        this.interactive= interactive;
        this.volume = volume;
        this.gradients = gradients;
        this.illuminate = illuminate;

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
        if (illuminate) {
            if (p[0] < 0 || p[1] < 0 || p[2] < 0 || p[0] >= volume.getDimX() || p[1] >= volume.getDimY() || p[2] >= volume.getDimZ()) {
                return in;
            }

            double i_ambient = 0.2, k_diff = 0.7, k_spec = 0.2, alpha = 10.0;
            TFColor out = new TFColor(in.r, in.g, in.b, in.a);

            //Calculate normal according to gradients
            double[] N = new double[3];
            VoxelGradient gradient = gradients.getGradient((int) Math.floor(p[0]), (int) Math.floor(p[1]), (int) Math.floor(p[2]));

            if (gradient.mag > 0.0 && in.a > 0.0) {
                // Calculate the center point of our view (where the light source and eye is)
//                double[] centerP = new double[3];
//                centerP[0] = uVec[0] * imageCenter + vVec[0] * imageCenter;
//                centerP[1] = uVec[1] * imageCenter + vVec[1] * imageCenter;
//                centerP[2] = uVec[2] * imageCenter + vVec[2] * imageCenter;
//                double[] L = new double[]{centerP[0] - p[0], centerP[1] - p[1], centerP[2] - p[2]};

                // Set L (= V) = H to be the vector pointing from the point to our 'eye'/light source
                double[] L = new double[]{-viewVec[0], -viewVec[1], -viewVec[2]};
                double[] H = L;

                // Calculate normal vector N
                N[0] = (double) gradient.x / (double) gradient.mag;
                N[1] = (double) gradient.y / (double) gradient.mag;
                N[2] = (double) gradient.z / (double) gradient.mag;

                // Compute L dot N and N dot H
                double ln = VectorMath.dotproduct(L, N);
                double nh = VectorMath.dotproduct(N, H);

                if (ln > 0 && nh > 0) {
                    out.r = i_ambient + in.r * (k_diff * ln) + k_spec * Math.pow(nh, alpha);
                    out.g = i_ambient + in.g * (k_diff * ln) + k_spec * Math.pow(nh, alpha);
                    out.b = i_ambient + in.b * (k_diff * ln) + k_spec * Math.pow(nh, alpha);
                }
            }
            return out;
        } else {
            return in;
        }
    }
}
