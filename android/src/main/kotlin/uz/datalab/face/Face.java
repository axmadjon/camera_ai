package uz.datalab.face;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import uz.datalab.vision.VisionUtil;

public class Face {

    public static Face create(Bitmap bitmap, com.google.mlkit.vision.face.Face face) {
        return create(bitmap, face, RECT_MARGIN);
    }

    public static Face create(Bitmap bitmap, com.google.mlkit.vision.face.Face face, float margin) {
        Rect rect = face.getBoundingBox();
        Bitmap faceCropped = VisionUtil.cropImage(bitmap, rect, margin);
        faceCropped = VisionUtil.resizeByMaximum(faceCropped, 200);
        return new Face(face, rect, faceCropped);
    }

    public static final float RECT_MARGIN = 0.1f;

    private final int faceId;

    private Rect rect;
    private Bitmap faceCropped;
    private String vector;

    private float rightEyeOpenProbability;
    private float leftEyeOpenProbability;
    private float smilingProbability;

    public Face(com.google.mlkit.vision.face.Face face, Rect rect, Bitmap faceCropped) {
        this.faceId = face.getTrackingId();
        this.rect = VisionUtil.rectWithMargin(rect, RECT_MARGIN);
        this.faceCropped = faceCropped;

        rightEyeOpenProbability = face.getRightEyeOpenProbability();
        leftEyeOpenProbability = face.getLeftEyeOpenProbability();
        smilingProbability = face.getSmilingProbability();
    }

    public int getFaceId() {
        return faceId;
    }

    public Bitmap getFaceCropped() {
        return faceCropped;
    }

    public boolean hasVector() {
        return !TextUtils.isEmpty(vector);
    }

    public void setVector(String vector) {
        this.vector = vector;
    }

    public void changeFace(Bitmap bitmap, com.google.mlkit.vision.face.Face face) {
        if (faceId != face.getTrackingId()) {
            throw new RuntimeException("faceId not match for changes");
        }

        rightEyeOpenProbability = face.getRightEyeOpenProbability();
        leftEyeOpenProbability = face.getLeftEyeOpenProbability();
        smilingProbability = face.getSmilingProbability();

        this.rect = face.getBoundingBox();

        if (!hasVector()) {
            Bitmap faceCropped = VisionUtil.cropImage(bitmap, rect, RECT_MARGIN);
            this.faceCropped = VisionUtil.resizeByMaximum(faceCropped, 200);
        }
    }

    @NonNull
    @Override
    public String toString() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("face_id", String.valueOf(faceId));
            obj.put("rect", "" + rect.left + "," + rect.top + "," + rect.right + "," + rect.bottom);
            obj.put("left_eye_probability", String.valueOf(this.leftEyeOpenProbability));
            obj.put("right_eye_probability", String.valueOf(this.rightEyeOpenProbability));
            obj.put("smile_probability", String.valueOf(this.smilingProbability));
            if (hasVector()) {
                obj.put("vector", vector);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj.toString();
    }
}
