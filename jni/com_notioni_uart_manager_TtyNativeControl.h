#include <jni.h>
#ifndef _Included_com_notioni_uart_manager_TtyNativeControl
#define _Included_com_notioni_uart_manager_TtyNativeControl
#ifdef __cplusplus
extern "C" {
#endif

  /**
* Class com_notioni_uart_TtyNativeControl
* Method
*/
JNIEXPORT static void JNICALL com_notioni_uart_manager_TtyNativeControl_native_setup(JNIEnv *env,jobject clazz,jobject weak_this);
JNIEXPORT static int JNICALL com_notioni_uart_manager_TtyNativeControl__openTty(JNIEnv *env,jobject clazz);
JNIEXPORT static int JNICALL com_notioni_uart_manager_TtyNativeControl__closeTty(JNIEnv *env,jobject clazz);
JNIEXPORT static int JNICALL com_notioni_uart_manager_TtyNativeControl__sendMsgToTty(JNIEnv *env,jobject clazz,jbyteArray data);
JNIEXPORT static void JNICALL com_notioni_uart_manager_TtyNativeControl__receiveMsgFromTty(JNIEnv *env,jobject clazz);
JNIEXPORT static int JNICALL com_notioni_uart_manager_TtyNativeControl__configTty(JNIEnv *env,jobject clazz,int nBits,jchar nEvent,int nSpeed,int nStop);
JNIEXPORT static int JNICALL com_notioni_uart_manager_TtyNativeControl__setMode(JNIEnv *env,jobject clazz,int nMode,int showLog);
//JNIEXPORT int JNICALL com_notioni_uart_manager_TtyNativeControl__setSpeed(JNIEnv *env,jobjectclazz,int speed);
//JNIEXPORT int JNICALL com_notioni_uart_manager_TtyNativeControl__setParity(JNIEnv *env,jobjectclazz,int databits,int stopbits,int parity);
#ifdef __cplusplus
}
#endif
#endif
