package volvis;

import volume.Volume;

import java.awt.image.BufferedImage;

/**
 * Created by ruudandriessen on 27/11/16.
 */
public class MIPWorker extends RaycastWorker {
    int max;

    public MIPWorker(int startH, int endH, int volumeMax, Volume volume, BufferedImage target, double[] viewMatrix, boolean interactive) {
        super(startH, endH, volume, target, viewMatrix, interactive);
        max = volumeMax;
    }

    @Override
    public void run() {
        TFColor voxelColor = new TFColor();
        double[] p = new double[3];

        for (int j = startH; j < endH; j++) {
            for (int i = 0; i < image.getWidth(); i++) {
                if (currentThread().isInterrupted()) {
                    return;
                }
                if(i%4 == 0 && j%4 == 0 || !interactive) {
                    short val = 0;
                    for (double k = -maxRange/2; k < maxRange/2; k++) {
                        p[0] = uVec[0] * (i - imageCenter) + vVec[0] * (j - imageCenter)
                                + viewVec[0] * k + volumeCenter[0];
                        p[1] = uVec[1] * (i - imageCenter) + vVec[1] * (j - imageCenter)
                                + viewVec[1] * k + volumeCenter[1];
                        p[2] = uVec[2] * (i - imageCenter) + vVec[2] * (j - imageCenter)
                                + viewVec[2] * k + volumeCenter[2];
                        short newVal = interVoxel(p);

                        if (newVal > val) {
                            val = newVal;
                        }
                    }
                    voxelColor.r = (double) val/max;
                    voxelColor.g = voxelColor.r;
                    voxelColor.b = voxelColor.r;
                    voxelColor.a = val > 0 ? 1.0 : 0.0;

                    setColor(i, j, voxelColor);
                }
            }
        }
    }
}