//
// Created by wayne on 19-4-25.
//
#include <jni.h>
#include <string>
#include <vector>
#include <ctime>

// ncnn
#include "ncnn/net.h"
#include "ncnn/mat.h"

#include "base.h"
#include "arcface.h"

using namespace std;
static Arcface *arc;
//sdk Whether the initialization was successful
bool detection_sdk_init_ok_ = false;

extern "C" {

JNIEXPORT jboolean JNICALL
Java_uz_datalab_face_Recognition_init(JNIEnv *env, jobject instance, jstring modelPath_) {
    //Return directly if initialized
    if (detection_sdk_init_ok_) {
        return static_cast<jboolean>(true);
    }

    if (NULL == modelPath_) {
        return static_cast<jboolean>(false);
    }

    //Directory to get the absolute path of the ArcFace model (not a path like /aaa/bbb.bin, it is / aaa /)
    const char *arcFaceModelPath = env->GetStringUTFChars(modelPath_, 0);
    if (NULL == arcFaceModelPath) {
        return static_cast<jboolean>(false);
    }

    string tArcFaceModelDir = arcFaceModelPath;
    string tLastChar = tArcFaceModelDir.substr(tArcFaceModelDir.length() - 1, 1);
    if ("\\" == tLastChar) {
        tArcFaceModelDir = tArcFaceModelDir.substr(0, tArcFaceModelDir.length() - 1) + "/";
    } else if (tLastChar != "/") {
        tArcFaceModelDir += "/";
    }

    arc = new Arcface(tArcFaceModelDir);

    env->ReleaseStringUTFChars(modelPath_, arcFaceModelPath);
    detection_sdk_init_ok_ = true;
    return static_cast<jboolean>(true);
}

JNIEXPORT jfloatArray JNICALL
Java_uz_datalab_face_Recognition_GetFeature(JNIEnv *env, jobject instance, jbyteArray imageData1_,
                                            jint imageWidth1, jint imageHeight1, jintArray info1_) {
    jbyte *imageData1 = env->GetByteArrayElements(imageData1_, NULL);
    if (NULL == imageData1) {
        env->ReleaseByteArrayElements(imageData1_, imageData1, 0);
        return NULL;
    }
    unsigned char *featureImageCharData1 = (unsigned char *) imageData1;
    //Failure to specify whether the number of channels is 3 or 4 may cause problems
    ncnn::Mat ncnn_img1;
    ncnn_img1 = ncnn::Mat::from_pixels(featureImageCharData1, ncnn::Mat::PIXEL_RGBA2RGB,
                                       imageWidth1, imageHeight1);

    jint *info1 = env->GetIntArrayElements(info1_, NULL);
    FaceInfo firstInfo;
    //firstInfo.score = 0;
    //Face box corresponding assignment
    firstInfo.x[0] = info1[1];
    firstInfo.y[0] = info1[2];
    firstInfo.x[1] = info1[3];
    firstInfo.y[1] = info1[4];
    //Assignment of five key point coordinates
    firstInfo.landmark[0] = info1[5];
    firstInfo.landmark[1] = info1[10];
    firstInfo.landmark[2] = info1[6];
    firstInfo.landmark[3] = info1[11];
    firstInfo.landmark[4] = info1[7];
    firstInfo.landmark[5] = info1[12];
    firstInfo.landmark[6] = info1[8];
    firstInfo.landmark[7] = info1[13];
    firstInfo.landmark[8] = info1[9];
    firstInfo.landmark[9] = info1[14];

    ncnn::Mat det1 = preprocess(ncnn_img1, firstInfo);

    vector<float> feature1 = arc->getFeature(det1);

    float *feature = new float[128];
    vector<float>::iterator it;
    int i = 0;
    for (it = feature1.begin(); it != feature1.end(); it++) {
        feature[i] = *it;
        i += 1;
    }

    jfloatArray tFeature = env->NewFloatArray(128);
    env->SetFloatArrayRegion(tFeature, 0, 128, feature);
    delete[] feature;

    env->ReleaseByteArrayElements(imageData1_, imageData1, 0);
    env->ReleaseIntArrayElements(info1_, info1, 0);

    return tFeature;
}

JNIEXPORT jfloat JNICALL
Java_uz_datalab_face_Recognition_NewCalcSimilarity(JNIEnv *env, jobject instance,
                                                   jfloatArray feature1_,
                                                   jfloatArray feature2_) {
    jfloat *feature1 = env->GetFloatArrayElements(feature1_, NULL);
    vector<float> Feature1;
    for (int i = 0; i < 128; i++) {
        Feature1.push_back(feature1[i]);
    }
    jfloat *feature2 = env->GetFloatArrayElements(feature2_, NULL);
    vector<float> Feature2;
    for (int i = 0; i < 128; i++) {
        Feature2.push_back(feature2[i]);
    }
    env->ReleaseFloatArrayElements(feature1_, feature1, 0);
    env->ReleaseFloatArrayElements(feature2_, feature2, 0);
    return calcSimilar(Feature1, Feature2);
}

}
