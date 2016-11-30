package volvis;

import gui.TransferFunction2DEditor;
import volume.GradientVolume;
import volume.Volume;
import volume.VoxelGradient;

import java.awt.image.BufferedImage;

/**
 * Created by ruudandriessen on 27/11/16.
 */
public class GradientWorker extends RaycastWorker {
    GradientVolume gradients;
    TransferFunction2DEditor tf2d;

    public GradientWorker(int startH, int endH, Volume volume, GradientVolume gradients, TransferFunction2DEditor tf2d, BufferedImage target, double[] viewMatrix, boolean interactive, boolean illuminate) {
        super(startH, endH, volume, gradients, target, viewMatrix, interactive, illuminate);
        this.gradients = gradients;
        this.tf2d = tf2d;
    }


    @Override
    public void run() {
        TFColor compColor, voxelColor;
        double[] p = new double[3];
        double intensity =  tf2d.triangleWidget.baseIntensity;
        double radius =  tf2d.triangleWidget.radius;
        TFColor tfColor =  tf2d.triangleWidget.color;

        for (int j = startH; j < endH; j++) {
            for (int i = 0; i < image.getWidth(); i++) {
                if (currentThread().isInterrupted()) {
                    return;
                }
                compColor = new TFColor(0, 0, 0, 1);
                if (i % 4 == 0 && j % 4 == 0 || !interactive) {
                    for (double k = -maxRange / 2; k < maxRange / 2; k++) {
                        p[0] = uVec[0] * (i - imageCenter) + vVec[0] * (j - imageCenter)
                                + viewVec[0] * k + volumeCenter[0];
                        p[1] = uVec[1] * (i - imageCenter) + vVec[1] * (j - imageCenter)
                                + viewVec[1] * k + volumeCenter[1];
                        p[2] = uVec[2] * (i - imageCenter) + vVec[2] * (j - imageCenter)
                                + viewVec[2] * k + volumeCenter[2];

                        if (p[0] >= volume.getDimX() || p[0] < 0 || p[1] >= volume.getDimY() || p[1] < 0 || p[2] >= volume.getDimZ() || p[2] < 0) {
                            continue;
                        }

                        short val = interVoxel(p);
                        voxelColor = new TFColor(tfColor.r, tfColor.g, tfColor.b, tfColor.a);
                        VoxelGradient gradient = gradients.getGradient((int) Math.floor(p[0]), (int) Math.floor(p[1]), (int) Math.floor(p[2]));

                        // Calculate isovalue contour surfaces according to Levoy
                        if (gradient.mag == 0 && val == intensity) {
                            voxelColor.a = tfColor.a * 1.0;
                        } else if (gradient.mag > 0.0 && ((val - radius * gradient.mag) <= intensity) && ((val + radius * gradient.mag) >= intensity)) {
                            voxelColor.a = tfColor.a *  (1.0 - (1.0 / radius) * (Math.abs((intensity - val) / gradient.mag)));
                        } else {
                            voxelColor.a = 0.0;
                        }
                        voxelColor = phong(voxelColor, p);

                        compColor.r = voxelColor.r * voxelColor.a + (1.0 - voxelColor.a) * compColor.r;
                        compColor.g = voxelColor.g * voxelColor.a + (1.0 - voxelColor.a) * compColor.g;
                        compColor.b = voxelColor.b * voxelColor.a + (1.0 - voxelColor.a) * compColor.b;

                    }
                    setColor(i, j, compColor);
                }

            }
        }
    }
}