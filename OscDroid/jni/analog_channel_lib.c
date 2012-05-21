#include <jni.h>
#include <string.h>
#include <android/log.h>

#define DEBUG_TAG "NDK_OscDroidActivityTest"

jfloat Java_com_kvw_oscdroid_AnalogChannel_calcDisplayX(JNIEnv *env, jobject this, jint num, jfloat scrnWidth, jfloat zoomX, jfloat offsetX)
{
	jfloat x=0;

	x = (scrnWidth + zoomX)/1024*num + offsetX;
	

	return x;
}

jfloat Java_com_kvw_oscdroid_AnalogChannel_calcDisplayY(JNIEnv *env, jobject this, jint dataPoint, jfloat scrnHeight, jfloat zoomY, jfloat offsetY)
{
	jfloat y=0;

	y = (scrnHeight + zoomY)/256*(255-dataPoint)+ offsetY;

	return y;
}
