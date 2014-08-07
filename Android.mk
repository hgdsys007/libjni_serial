LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS = optional
LOCAL_SRC_FILES := \
        $(call all-java-files-under, src)

#LOCAL_JAVA_LIBRARIES = uart-ctl
LOCAL_JNI_SHARED_LIBRARIES = uart_ctl #so文件打包到apk中

LOCAL_PACKAGE_NAME = UARTCTL
LOCAL_CERTIFICATE = platform

include $(BUILD_PACKAGE)

#include $(LOCAL_PATH)/jni/Android.mk
include $(call all-makefiles-under,$(LOCAL_PATH))
