LOCAL_PATH:=$(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES:= \
  com_notioni_uart_manager_TtyNativeControl.cpp
LOCAL_C_INCLUDES := \
$(JNI_H_INCLUDE)
LOCAL_SHARED_LIBRARIES := \
  libcutils \
  libutils \
  libui \
  libandroid_runtime
LOCAL_PRELINK_MODULE := false
LOCAL_MODULE := libuart_ctl
include $(BUILD_SHARED_LIBRARY)
