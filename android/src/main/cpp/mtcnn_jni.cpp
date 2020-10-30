#include <jni.h>
#include <string>
#include <vector>

// ncnn
#include "ncnn/net.h"
#include "ncnn/mat.h"

#include "mtcnn.h"

using namespace std;
static MTCNN *mtcnn;

//sdk Whether the initialization was successful
bool detection_sdk_init_ok = false;


extern "C" {

JNIEXPORT jboolean JNICALL
Java_uz_datalab_face_Detect_init(JNIEnv *env, jobject instance, jstring modelPath_) {
    //Return directly if initialized
    if (detection_sdk_init_ok) {
        return static_cast<jboolean>(true);
    }
    if (NULL == modelPath_) {
        return static_cast<jboolean>(false);
    }

    //Directory to get the absolute path of the MTCNN model (not a path like /aaa/bbb.bin, it is / aaa /)
    const char *faceDetectionModelPath = env->GetStringUTFChars(modelPath_, 0);
    if (NULL == faceDetectionModelPath) {
        return static_cast<jboolean>(false);
    }

    string tFaceModelDir = faceDetectionModelPath;
    string tLastChar = tFaceModelDir.substr(tFaceModelDir.length() - 1, 1);
    //Directory completion /
    if ("\\" == tLastChar) {
        tFaceModelDir = tFaceModelDir.substr(0, tFaceModelDir.length() - 1) + "/";
    } else if (tLastChar != "/") {
        tFaceModelDir += "/";
    }

    //Didn't judge if it was imported correctly, too lazy to change
    mtcnn = new MTCNN(tFaceModelDir);
    mtcnn->SetMinFace(40);

    env->ReleaseStringUTFChars(modelPath_, faceDetectionModelPath);
    detection_sdk_init_ok = true;
    return static_cast<jboolean>(true);
}

JNIEXPORT jintArray JNICALL
Java_uz_datalab_face_Detect_FaceDetect(JNIEnv *env, jobject instance, jbyteArray imageDate_,
                                       jint imageWidth, jint imageHeight, jint imageChannel) {
    if (!detection_sdk_init_ok) {
        return NULL;
    }

    int tImageDateLen = env->GetArrayLength(imageDate_);
    if (imageChannel != tImageDateLen / imageWidth / imageHeight) {
        return NULL;
    }

    jbyte *imageDate = env->GetByteArrayElements(imageDate_, NULL);
    if (NULL == imageDate) {
        env->ReleaseByteArrayElements(imageDate_, imageDate, 0);
        return NULL;
    }

    if (imageWidth < 20 || imageHeight < 20) {
        env->ReleaseByteArrayElements(imageDate_, imageDate, 0);
        return NULL;
    }

    if (!(3 == imageChannel || 4 == imageChannel)) {
        env->ReleaseByteArrayElements(imageDate_, imageDate, 0);
        return NULL;
    }

    unsigned char *faceImageCharDate = (unsigned char *) imageDate;
    ncnn::Mat ncnn_img;
    if (imageChannel == 3) {
        ncnn_img = ncnn::Mat::from_pixels(faceImageCharDate, ncnn::Mat::PIXEL_BGR2RGB,
                                          imageWidth, imageHeight);
    } else {
        ncnn_img = ncnn::Mat::from_pixels(faceImageCharDate, ncnn::Mat::PIXEL_RGBA2RGB, imageWidth,
                                          imageHeight);
    }

    std::vector<Bbox> finalBbox;
    mtcnn->detect(ncnn_img, finalBbox);

    int32_t num_face = static_cast<int32_t>(finalBbox.size());

    int out_size = 1 + num_face * 14;
    int *faceInfo = new int[out_size];
    faceInfo[0] = num_face;
    for (int i = 0; i < num_face; i++) {
        faceInfo[14 * i + 1] = finalBbox[i].x1;//left
        faceInfo[14 * i + 2] = finalBbox[i].y1;//top
        faceInfo[14 * i + 3] = finalBbox[i].x2;//right
        faceInfo[14 * i + 4] = finalBbox[i].y2;//bottom
        for (int j = 0; j < 10; j++) {
            faceInfo[14 * i + 5 + j] = static_cast<int>(finalBbox[i].ppoint[j]);
        }
    }

    jintArray tFaceInfo = env->NewIntArray(out_size);
    env->SetIntArrayRegion(tFaceInfo, 0, out_size, faceInfo);
    delete[] faceInfo;
    env->ReleaseByteArrayElements(imageDate_, imageDate, 0);
    return tFaceInfo;
}

JNIEXPORT jboolean JNICALL
Java_uz_datalab_face_Detect_FaceDetectionModelUnInit(JNIEnv *env, jobject instance) {
    if (!detection_sdk_init_ok) {
        return static_cast<jboolean>(true);
    }
    delete mtcnn;
    detection_sdk_init_ok = false;
    return static_cast<jboolean>(true);
}


JNIEXPORT jboolean JNICALL
Java_uz_datalab_face_Detect_SetMinFaceSize(JNIEnv *env, jobject instance, jint minSize) {
    if (!detection_sdk_init_ok) {
        return static_cast<jboolean>(false);
    }

    if (minSize <= 20) {
        minSize = 20;
    }
    mtcnn->SetMinFace(minSize);
    return static_cast<jboolean>(true);
}


JNIEXPORT jboolean JNICALL
Java_uz_datalab_face_Detect_SetThreadsNumber(JNIEnv *env, jobject instance, jint threadsNumber) {
    if (!detection_sdk_init_ok) {
        return static_cast<jboolean>(false);
    }

    if (threadsNumber != 1 && threadsNumber != 2 && threadsNumber != 4 && threadsNumber != 8) {
        return static_cast<jboolean>(false);
    }

    mtcnn->SetNumThreads(threadsNumber);
    return static_cast<jboolean>(true);
}


JNIEXPORT jboolean JNICALL
Java_uz_datalab_face_Detect_SetTimeCount(JNIEnv *env, jobject instance, jint timeCount) {
    if (!detection_sdk_init_ok) {
        return static_cast<jboolean>(false);
    }

    mtcnn->SetTimeCount(timeCount);
    return static_cast<jboolean>(true);
}

}