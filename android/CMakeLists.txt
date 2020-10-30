cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fopenmp -static-openmp")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fopenmp -static-openmp")

#include Header file directory
include_directories(src/main/cpp/include
                    src/main/cpp/)

#source directory Source file directory
file(GLOB native_srcs src/main/cpp/*.cpp)
#set(FACERECOGNITION_COMPILE_CODE ${FACERECOGNITION_SRC})


#Add ncnn library
add_library(libncnn STATIC IMPORTED )
set_target_properties(libncnn
  PROPERTIES IMPORTED_LOCATION
  ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libncnn.a)

#Compile as a dynamic library
add_library(facerecognition SHARED ${native_srcs})

#Add the libraries that the project depends on
target_link_libraries(  facerecognition
                       libncnn )
