LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_LDLIBS 	:= -llog

LOCAL_MODULE    := libanalog
LOCAL_SRC_FILES := analog_channel_lib.c

include $(BUILD_SHARED_LIBRARY)
