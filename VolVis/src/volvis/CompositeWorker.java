package volvis;

import volume.GradientVolume;
import volume.Volume;

import java.awt.image.BufferedImage;

/**
 * Created by ruudandriessen on 27/11/16.
 */
public class CompositeWorker extends RaycastWorker {
    TransferFunction tFunc;

    public CompositeWorker(int startH, int endH, Volume volume, GradientVolume gradients, BufferedImage target, TransferFunction tFunc, double[] viewMatrix, boolean interactive) {
        super(startH, endH, volume, gradients, target, viewMatrix, interactive);
        this.tFunc = tFunc;
    }

    @Override
    public void run() {
        TFColor compColor, voxelColor;
        double[] p = new double[3];

        for (int j = startH; j < endH; j++) {
            for (int i = 0; i < image.getWidth(); i++) {
                if (currentThread().isInterrupted()) {
                    return;
                }
                compColor = new TFColor(0,0,0,1);
                if(i%4 == 0 && j%4 == 0 || !interactive) {
                    for (double k = -maxRange/2; k < maxRange/2; k++) {
                        p[0] = uVec[0] * (i - imageCenter) + vVec[0] * (j - imageCenter)
                                + viewVec[0] * k + volumeCenter[0];
                        p[1] = uVec[1] * (i - imageCenter) + vVec[1] * (j - imageCenter)
                                + viewVec[1] * k + volumeCenter[1];
                        p[2] = uVec[2] * (i - imageCenter) + vVec[2] * (j - imageCenter)
                                + viewVec[2] * k + volumeCenter[2];
                        short val = interVoxel(p);
                        voxelColor = tFunc.getColor(val);

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
