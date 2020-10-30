package uz.datalab.face;

import android.content.Context;

public class Detect {

    private boolean isInit = false;

    public Detect(Context context) {
        if (!FaceUtil.isModelFileExists(context, "det1.bin") ||
                !FaceUtil.isModelFileExists(context, "det2.bin") ||
                !FaceUtil.isModelFileExists(context, "det3.bin") ||
                !FaceUtil.isModelFileExists(context, "det1.param") ||
                !FaceUtil.isModelFileExists(context, "det2.param") ||
                !FaceUtil.isModelFileExists(context, "det3.param")) {
            throw new RuntimeException("Face detection module is not exists");
        }
    }

    public boolean init(Context context) {
        if (isInit) {
            return true;
        }

        this.isInit = init(FaceUtil.getModelFolder(context));
        if (isInit) {
            SetMinFaceSize(80);
            SetThreadsNumber(4);
            SetTimeCount(1);
        }

        return isInit;
    }

    public boolean isModelInit() {
        return isInit;
    }

    public FaceInfo[] detect(byte[] imageDate, int imageWidth, int imageHeight, int imageChannel) {
        if (!isInit) {
            throw new RuntimeException("Detection is not init");
        }
        int[] infos = FaceDetect(imageDate, imageWidth, imageHeight, imageChannel);

        FaceInfo[] results = new FaceInfo[infos[0]];
        for (int i = 0; i < infos[0]; i++) {
            int[] singleFaceInfo = new int[14];
            for (int j = 0; j < 14; j++) {
                singleFaceInfo[j] = infos[14 * i + j + 1];
            }
            results[i] = new FaceInfo(singleFaceInfo);
        }
        return results;
    }

    public boolean release() {
        if (!isInit) {
            throw new RuntimeException("Detection is not init");
        }
        boolean isUnInit = FaceDetectionModelUnInit();
        if (isUnInit) {
            isInit = false;
        }
        return isUnInit;
    }

    //Face detection model import
    native boolean init(String modelPath);

    //Face Detection
    native int[] FaceDetect(byte[] imageDate, int imageWidth, int imageHeight, int imageChannel);

    //Face detection model de-initialization
    native boolean FaceDetectionModelUnInit();

    //Minimum face detection setting
    native boolean SetMinFaceSize(int minSize);

    //Thread settings
    native boolean SetThreadsNumber(int threadsNumber);

    //Cycle test times
    native boolean SetTimeCount(int timeCount);

    static {
        System.loadLibrary("facerecognition");
    }

    public static class FaceInfo {

        private final int[] infos;

        FaceInfo(int[] infos) {
            this.infos = infos;
        }

        public int[] getInfos() {
            int[] singleFaceInfo = new int[15];
            singleFaceInfo[0] = 0;
            System.arraycopy(infos, 0, singleFaceInfo, 1, 14);
            return singleFaceInfo;
        }
    }
}
