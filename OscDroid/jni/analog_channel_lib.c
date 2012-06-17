#include <jni.h>
#include <string.h>

#define DEBUG_TAG "NDK_OscDroidActivityTest"

jfloat Java_com_kvw_oscdroid_channels_AnalogChannel_calcDisplayX(JNIEnv *env, jobject this, jint num, jint numSamples, jfloat scrnWidth, jfloat zoomX, jfloat offsetX)
{
	jfloat x=0;

	x = (scrnWidth + zoomX)/numSamples*num;

	return x;
}

jfloat Java_com_kvw_oscdroid_channels_AnalogChannel_calcDisplayY(JNIEnv *env, jobject this, jint dataPoint, jfloat scrnHeight, jfloat zoomY, jfloat offsetY)
{
	jfloat y=0;
	jfloat tmp = ((jfloat)dataPoint-128)*zoomY+128;
		
	y = (scrnHeight)/256*(255-tmp)+ offsetY;

	return y;
}

jfloat Java_com_kvw_oscdroid_channels_AnalogChannel_getMax(JNIEnv *env, jobject this, jintArray mDataSet, jint numSamples)
{
	jint *mData;
	jfloat maximum=0;
	int i=0;	
	
	mData = (*env)->GetIntArrayElements(env, mDataSet, NULL);

	for(i=0;i<numSamples;i++)
	{
		if(mData[i] > maximum) maximum = mData[i];
	}
	
	(*env)->ReleaseIntArrayElements(env, mDataSet, mData,0);
	return maximum;
}
