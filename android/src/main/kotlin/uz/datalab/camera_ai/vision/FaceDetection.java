package uz.datalab.camera_ai.vision;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.Image;
import android.util.Base64;

import androidx.annotation.Nullable;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import uz.datalab.face.Detect;
import uz.datalab.face.Face;
import uz.datalab.face.FaceUtil;
import uz.datalab.face.Recognition;
import uz.datalab.vision.Vision;
import uz.mold.job.JobApi;

public class FaceDetection {

    private static final ExecutorService executor = Executors.newFixedThreadPool(2);

    public void init(Context ctx) {
        isDetection = false;

        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .enableTracking()
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
                .build();

        this.mFaceDetector = com.google.mlkit.vision.face.FaceDetection.getClient(options);

        if (Vision.isModelDownloaded()) {
            detection = Vision.getFaceDetection();
            recognition = Vision.getFaceRecognition();
        }
    }

    private final List<Face> faces = new ArrayList<>();

    private FaceDetector mFaceDetector;

    private Bitmap mCurrentFrame;
    private InputImage visionImage;

    private boolean isDetection = false;
    private boolean isRecognition = false;

    @Nullable
    private Detect detection;
    @Nullable
    private Recognition recognition;


    public boolean isDetection() {
        return isDetection;
    }

    public String getCurrentFrame2Base64() {
        if (mCurrentFrame == null) {
            return "";
        }
        try {
            Bitmap bitmap = uz.datalab.vision.VisionUtil.resizeByMaximum(mCurrentFrame, 800);

            byte[] imageBytes = uz.datalab.vision.VisionUtil.toBytes(bitmap, 90);

            if (imageBytes == null || imageBytes.length == 0) {
                return "";
            }
            return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getResultToString() {
        StringBuilder sb = new StringBuilder();
        boolean append = false;
        for (Face face : faces) {
            if (append) sb.append("#");
            sb.append(face.toString());
            append = true;
        }
        return sb.toString();
    }

    public void resetFaceId(Set<String> ids) {
        for (Face face : faces) {
            if (ids.contains(String.valueOf(face.getFaceId()))) {
                face.setVector("");
            }
        }
    }

    public void predict(Image image, int rotation, boolean analyzeMaxFace) {
        try {
            isDetection = true;

            mCurrentFrame = VisionUtil.getBitmapFromInputImage(image, rotation);

            this.visionImage = InputImage.fromBitmap(mCurrentFrame, 0);
            isDetection = false;

            executor.execute(() -> {
                try {
                    isDetection = true;
                    mFaceDetector.process(visionImage)
                            .addOnSuccessListener(result -> {
                                try {
                                    List<com.google.mlkit.vision.face.Face> detectionFaces = new ArrayList<>();
                                    if (analyzeMaxFace) {
                                        Collections.sort(result, SORT_DETECTION_FACE);
                                        if (!result.isEmpty()) {
                                            detectionFaces.add(result.get(0));
                                        }
                                    } else {
                                        detectionFaces = result;
                                    }

                                    Map<Integer, Face> faceMap = new HashMap<>();
                                    for (Face face : faces) {
                                        faceMap.put(face.getFaceId(), face);
                                    }

                                    Set<Integer> trackingIds = new HashSet<>();
                                    List<Face> newFaces = new ArrayList<>();
                                    for (com.google.mlkit.vision.face.Face item : detectionFaces) {
                                        int trackingId = item.getTrackingId();
                                        trackingIds.add(trackingId);

                                        if (faceMap.containsKey(trackingId)) {
                                            Face face = Objects.requireNonNull(faceMap.get(item.getTrackingId()));
                                            face.changeFace(mCurrentFrame, item);
                                        } else {
                                            newFaces.add(Face.create(mCurrentFrame, item));
                                        }
                                    }

                                    Set<Face> removeFaceSet = new HashSet<>();
                                    for (Face face : faces) {
                                        if (!trackingIds.contains(face.getFaceId())) {
                                            removeFaceSet.add(face);
                                        }
                                    }

                                    faces.removeAll(removeFaceSet);
                                    faces.addAll(newFaces);

                                    faceRecognition();

                                } finally {
                                    isDetection = false;
                                }
                            })
                            .addOnCanceledListener(() -> {
                                System.out.println("addOnCanceledListener");
                                isDetection = false;
                                faces.clear();
                            })
                            .addOnFailureListener(e -> {
                                System.out.println("ProcessError");
                                e.printStackTrace();
                                isDetection = false;
                                faces.clear();
                            });
                } catch (Throwable throwable) {
                    System.out.println("ExecuteError");
                    throwable.printStackTrace();
                    isDetection = false;
                }
            });

        } catch (Throwable throwable) {
            System.out.println("MainError");
            throwable.printStackTrace();
            isDetection = false;
        }
    }

    private void faceRecognition() {
        if (isRecognition || detection == null || recognition == null) {
            return;
        }
        isRecognition = true;
        try {
            JobApi.execute(() -> {
                Map<Integer, Face> faceMap = new HashMap<>();
                for (Face face : faces) {
                    faceMap.put(face.getFaceId(), face);
                }

                for (Map.Entry<Integer, Face> entry : faceMap.entrySet()) {
                    if (entry.getValue().hasVector()) {
                        continue;
                    }
                    Bitmap face = entry.getValue().getFaceCropped();
                    Bitmap rgba = face.copy(Bitmap.Config.ARGB_8888, true);
                    int width = rgba.getWidth();
                    int height = rgba.getHeight();

                    byte[] image = FaceUtil.getPixelsRGBA(rgba);
                    Detect.FaceInfo[] faceInfos = detection.detect(image,
                            width, height, 4);

                    if (faceInfos.length == 0) {
                        continue;
                    }

                    float[] vector = recognition.getVector(image, width, height, faceInfos[0]);
                    StringBuilder sb = new StringBuilder();
                    boolean append = false;
                    for (float emb : vector) {
                        if (append) sb.append(",");
                        sb.append(emb);
                        append = true;
                    }
                    entry.getValue().setVector(sb.toString());
                }
                return null;
            }).always((b, o, throwable) -> isRecognition = false);

        } catch (Exception e) {
            e.printStackTrace();
            isRecognition = false;
        }
    }

    private final Comparator<com.google.mlkit.vision.face.Face> SORT_DETECTION_FACE =
            new Comparator<com.google.mlkit.vision.face.Face>() {

                private int getMaxPoint(com.google.mlkit.vision.face.Face item) {
                    Rect b = item.getBoundingBox();
                    int width = b.right - b.left;
                    int height = b.bottom - b.top;
                    return Math.max(width, height);
                }

                @Override
                public int compare(com.google.mlkit.vision.face.Face l,
                                   com.google.mlkit.vision.face.Face r) {
                    return Integer.compare(getMaxPoint(r), getMaxPoint(l));
                }
            };
}
