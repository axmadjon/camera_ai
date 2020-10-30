package uz.datalab.vision;

import android.content.Context;

import androidx.annotation.NonNull;

import uz.datalab.face.Detect;
import uz.datalab.face.Recognition;

public class Vision {

    private static Detect mFaceDetection = null;
    private static Recognition mFaceRecognition = null;
    private static boolean hasModel = false;

    public static void init(@NonNull Context context, @NonNull OnLoadModel onLoadModel) {
        try {
            mFaceDetection = new Detect(context);
            mFaceRecognition = new Recognition(context);
            hasModel = true;
        } catch (RuntimeException e) {
            e.printStackTrace();

            hasModel = onLoadModel.downloadModels();
            if (hasModel) {
                mFaceDetection = new Detect(context);
                mFaceRecognition = new Recognition(context);
            }
        }

        if (hasModel) {
            mFaceDetection.init(context);
            mFaceRecognition.init(context);
        }
    }

    public static Detect getFaceDetection() {
        return mFaceDetection;
    }

    public static Recognition getFaceRecognition() {
        return mFaceRecognition;
    }

    public static boolean isModelDownloaded() {
        return hasModel;
    }

    public interface OnLoadModel {
        boolean downloadModels();
    }
}
