package volvis;

import volume.Volume;

import java.awt.image.BufferedImage;

/**
 * Created by ruudandriessen on 27/11/16.
 */
public class SliceWorker extends RaycastWorker {
    int max;

    public SliceWorker(int startH, int endH, int volumeMax, Volume volume, BufferedImage target, double[] viewMatrix, boolean interactive) {
        super(startH, endH, volume, target, viewMatrix, interactive);
        max = volumeMax;
    }

    @Override
    public void run() {
        TFColor voxelColor = new TFColor();
        double[] p = new double[3];

        for (int j = startH; j < endH; j++) {
            for (int i = 0; i < image.getWidth(); i++) {
                p[0] = uVec[0] * (i - imageCenter) + vVec[0] * (j - imageCenter)
                        + volumeCenter[0];
                p[1] = uVec[1] * (i - imageCenter) + vVec[1] * (j - imageCenter)
                        + volumeCenter[1];
                p[2] = uVec[2] * (i - imageCenter) + vVec[2] * (j - imageCenter)
                        + volumeCenter[2];

                int val = interVoxel(p);

                // Map the intensity to a grey value by linear scaling
                voxelColor.r = (double) val/max;
                voxelColor.g = voxelColor.r;
                voxelColor.b = voxelColor.r;
                voxelColor.a = val > 0 ? 1.0 : 0.0;  // this makes intensity 0 completely transparent and the rest opaque
                // Alternatively, apply the transfer function to obtain a color
                // voxelColor = tFunc.getColor(val);


                // BufferedImage expects a pixel color packed as ARGB in an int
                image.setRGB(i, j, voxelColor.toARGB());
            }
        }
    }
}
