#pragma once

#ifndef __MTCNN_NCNN_H__
#define __MTCNN_NCNN_H__
#include "ncnn/net.h"
#include <string>
#include <vector>
#include <time.h>
#include <algorithm>
#include <map>
#include <iostream>

using namespace std;
//using namespace cv;
struct Bbox
{
    float score;
    int x1;	//x1 Is the x coordinate of the upper left corner of the face box
    int y1; //y1 Is the x coordinate of the upper left corner of the face box
    int x2; //x2 Is the x coordinate of the upper left corner of the face box
    int y2; //y2 Is the x coordinate of the upper left corner of the face box
    float area;
    float ppoint[10]; //From left to right, the x-coordinates and y-coordinates of the five key points are stored in the format of x1, x2, ..., x5, y1, y2, ..., y5.
    float regreCoord[4];
};


class MTCNN {

public:
	MTCNN(const string &model_path);
    MTCNN(const std::vector<std::string> param_files, const std::vector<std::string> bin_files);
    ~MTCNN();
	
	void SetMinFace(int minSize);
	void SetNumThreads(int numThreads);
	void SetTimeCount(int timeCount);

    void detect(ncnn::Mat& img_, std::vector<Bbox>& finalBbox);
	void detectMaxFace(ncnn::Mat& img_, std::vector<Bbox>& finalBbox);
private:
    void generateBbox(ncnn::Mat score, ncnn::Mat location, vector<Bbox>& boundingBox_, float scale);
	void nmsTwoBoxs(vector<Bbox> &boundingBox_, vector<Bbox> &previousBox_, const float overlap_threshold, string modelname = "Union");
	void nms(vector<Bbox> &boundingBox_, const float overlap_threshold, string modelname="Union");
    void refine(vector<Bbox> &vecBbox, const int &height, const int &width, bool square);
	void extractMaxFace(vector<Bbox> &boundingBox_);

	void PNet(float scale);
	void PNet();
    void RNet();
    void ONet();
    ncnn::Net Pnet, Rnet, Onet;
    ncnn::Mat img;
    const float nms_threshold[3] = {0.5f, 0.7f, 0.7f};
   
    const float mean_vals[3] = {127.5, 127.5, 127.5};
    const float norm_vals[3] = {0.0078125, 0.0078125, 0.0078125};
	const int MIN_DET_SIZE = 12;
    std::vector<Bbox> firstBbox_, secondBbox_,thirdBbox_;
	std::vector<Bbox> firstPreviousBbox_, secondPreviousBbox_, thirdPrevioussBbox_;
    int img_w, img_h;

private://Partially adjustable parameters
	const float threshold[3] = { 0.8f, 0.8f, 0.6f };
	int minsize = 40;
	const float pre_facetor = 0.709f;

	int count = 10;
	int num_threads = 4;
	
};


#endif //__MTCNN_NCNN_H__
