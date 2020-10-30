package uz.datalab.face;

import android.content.Context;

public class Recognition {

    private boolean isInit = false;

    public Recognition(Context context) {
        if (!FaceUtil.isModelFileExists(context, "mobilefacenet.param") ||
                !FaceUtil.isModelFileExists(context, "mobilefacenet.bin")) {
            throw new RuntimeException("Face recognition module is not exists");
        }
    }

    public boolean init(Context context) {
        if (isInit) {
            return true;
        }
        isInit = init(FaceUtil.getModelFolder(context));
        return isInit;
    }

    public boolean isModelInit(){
        return isInit;
    }

    public float[] getVector(byte[] imageData1, int imageWidth1, int imageHeight1, Detect.FaceInfo faceInfo) {
        if (!isInit) {
            throw new RuntimeException("Recognition is not init");
        }

        float[] vector = GetFeature(imageData1, imageWidth1, imageHeight1, faceInfo.getInfos());
        if (vector == null || vector.length == 0) {
            return new float[0];
        }
        return vector;
    }

    native boolean init(String modelPath);

    native float[] GetFeature(byte[] imageData1, int imageWidth1, int imageHeight1, int[] info1);

    native float NewCalcSimilarity(float[] feature1, float[] feature2);

    static {
        System.loadLibrary("facerecognition");
    }

}
