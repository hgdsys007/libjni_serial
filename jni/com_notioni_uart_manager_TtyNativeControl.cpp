#include "JNIHelp.h"
#include "android_runtime/AndroidRuntime.h"
#include <utils/misc.h>
#include <utils/Log.h>
#include <utils/threads.h>
#include <pthread.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <utils/Log.h>
#include <sys/ioctl.h>
#include <termios.h>
#include "com_notioni_uart_manager_TtyNativeControl.h"

#ifndef LOGD
#define LOGD(...) ALOGD( __VA_ARGS__)
#endif

#ifndef LOGE
#define LOGE(...) ALOGE( __VA_ARGS__)
#endif

#ifndef LOGW
#define LOGW(...) ALOGW( __VA_ARGS__)
#endif

#ifndef LOGI
#define LOGI(...) ALOGI( __VA_ARGS__)
#endif

#ifndef LOGV
#define LOGV(...) ALOGV( __VA_ARGS__)
#endif

#ifndef LOGE_IF
#define LOGE_IF(...) ALOGE_IF( __VA_ARGS__)
#endif

#ifndef LOGV_IF
#define LOGV_IF(...) ALOGV_IF( __VA_ARGS__)
#endif

using namespace android;
struct fields_t {
    jfieldID tty;
    jmethodID post_event;
};

static fields_t fields;
JavaVM* g_JavaVM;

#define TTY_DEVICE "/dev/ttyS3"
#define LOG_TAG "TtyNativeControl"
#define RECEIVE_DATA_INDEX (1)
#define POST_EVENT()

static int mTtyfd = -1;
static int mOpen = 0;

/**
* class Listener
*/
class JNIMyObserver {
public:
    JNIMyObserver(JNIEnv* env,jobject thiz,jobject weak_thiz);
    ~JNIMyObserver();
    void OnEvent(const char* buffer,int length,int what);
private:
    JNIMyObserver();
    jclass mClass;
    jobject mObject;
    Mutex mLock;
};

JNIMyObserver::JNIMyObserver(JNIEnv* env,jobject thiz,jobject weak_thiz) 
{
    jclass clazz = env->GetObjectClass(thiz);
    if(clazz == NULL) {
        // jniThrowException(env,"java/lang/Exception",NULL);
        LOGE("clazz is null");
        return;
    }
    mClass = (jclass)env->NewGlobalRef(clazz);
    mObject = env->NewGlobalRef(weak_thiz);
    LOGW("mClass=%d",mClass);
    LOGW("mObject=%d",mObject);
}

JNIMyObserver::~JNIMyObserver() 
{
    JNIEnv *env = AndroidRuntime::getJNIEnv();
    env->DeleteGlobalRef(mObject);
    env->DeleteGlobalRef(mClass);
}

void JNIMyObserver::OnEvent(const char* buffer,int length,int what) 
{
    //LOGW("OnEvent");
    Mutex::Autolock _l(mLock);
    if(NULL == g_JavaVM) {
        LOGE("JNIObserver::Event g_JavaVM is NULL");
        return;
    }
    bool isAttacked = false;
    
    /*
    * ����envָ��
    */
    JNIEnv* env;
    bool status = (g_JavaVM->GetEnv((void**) &env,JNI_VERSION_1_4) != JNI_OK);
    if(status) {
        g_JavaVM->AttachCurrentThread(&env,NULL);
    }
    
    /*
    * ����JAVA byte[]����
    */
    jbyteArray obj = NULL;
    if(buffer !=NULL && buffer != 0) {
        const jbyte* data = reinterpret_cast<const jbyte*>(buffer);
        obj = env->NewByteArray(length);
        env->SetByteArrayRegion(obj,0,length,data);
    }
    env->CallStaticVoidMethod(mClass,fields.post_event,mObject,what,0,0,obj);
    if(obj) {
        env->DeleteLocalRef(obj);
    }
    
    if(isAttacked) {
        g_JavaVM->DetachCurrentThread();//�����߳�
    }

    return;
}


/**
* class Listener end -----------------
*/
JNIMyObserver* listener;

/*
* setup
*/
static void JNICALL com_notioni_uart_manager_TtyNativeControl_native_setup(
    JNIEnv *env,jobject clazz,jobject weak_this)
{    
    LOGW("com_notioni_uart_manager_TtyNativeControl_native_setup");
    env->GetJavaVM(&g_JavaVM);
    if(listener != NULL) {
        delete listener;
    }
    listener = new JNIMyObserver(env,clazz,weak_this);
}

/*
* _openTty
*/
static int JNICALL com_notioni_uart_manager_TtyNativeControl__openTty(JNIEnv *env,jobject clazz) 
{
    LOGW("com_notioni_uart_manager_TtyNativeControl__openTty");
    mTtyfd = open(TTY_DEVICE,O_RDWR|O_NONBLOCK);//��д��ʽ
    if(mTtyfd < 0) {
        LOGE("mTtyfd open failure");
        return -1;
    }
    if(fcntl(mTtyfd,F_SETFL,0)<0) { //�ָ�����Ϊ����״̬
        LOGE("mTtyfd fcntl failure");
    }

    // if(isatty(STDIN_FILENO)==0){//�����Ƿ�Ϊ�ж��豸����0��ʹ�ն��豸
    // LOGE("standard inputs is not a terminal device");
    // }else{
    // LOGE("isatty success");
    // }
    mOpen = 1;
    LOGW("open device success");
    return 1;
}

/*
* _closeTty
*/
static int JNICALL com_notioni_uart_manager_TtyNativeControl__closeTty(JNIEnv *env,jobject clazz) 
{
    LOGW("com_notioni_uart_manager_TtyNativeControl__closeTty");
    if(mTtyfd < 0) {
        LOGE("mTtyfd open failure ,non't close");
        return -1;
    }
    mOpen = 0;
    sleep(2);//�ȴ��߳��˳�
    int c = close(mTtyfd);
    if(c < 0) {
        LOGE("mTtyfd close failure");
        return -1;
    }
    LOGW("close device success");
    return 1;
}

/*
* _sendMsgToTty
*/
static int JNICALL com_notioni_uart_manager_TtyNativeControl__sendMsgToTty(
    JNIEnv *env,jobject clazz,jbyteArray data) 
{ 
    //byte[]
   // LOGW("com_notioni_uart_manager_TtyNativeControl__sendMsgToTty");
    //jbyte * arrayBody = env->GetByteArrayElements(data,0); jsize theArrayLengthJ = env->GetArrayLength(data); BYTE * starter = (BYTE *)arrayBody;
    if(mTtyfd < 0) {
        LOGE("mTtyfd open failure ,non't write");
        return -1;
    }
    jbyte* arrayData = (jbyte*)env->GetByteArrayElements(data,0);
    jsize arrayLength = env->GetArrayLength(data);
    char* byteData = (char*)arrayData;
    int len = (int)arrayLength;
    //LOGW("write data len:%d",len);
    int re = write(mTtyfd,byteData,len);
    if(re == -1) {
        LOGE("write device error");
    }
    env->ReleaseByteArrayElements(data,arrayData,0);
    return re;
}

/*
* �߳�Run
*/
void* threadreadTtyData(void* arg) {
    //LOGW("run read data");
    if(!(arg)) {
        return NULL;
    }
    char* buf = new char[200];
    int result = 0,ret;
    fd_set readfd;
    struct timeval timeout;
#if 0
    //while(mOpen) {
    //    sleep(2);//�ȴ��߳��˳�
   // }
#else
    while(mOpen) {
        timeout.tv_sec = 0;//�趨��ʱ����
        timeout.tv_usec = 200*1000;// 500;//�趨��ʱ������
        FD_ZERO(&readfd);//��ռ���
        FD_SET(mTtyfd,&readfd);///* ��Ҫ���ľ��mTtyfd���뵽������ */
        ret = select(mTtyfd+1,&readfd,NULL,NULL,&timeout);/* ��������������õ�����readfd��ľ���Ƿ��пɶ���Ϣ */
        switch(ret) {
        case -1:/* ��˵��select�������� */
            result = -1;
            LOGE("mTtyfd read failure");
            break;
        case 0:/* ˵���������趨��ʱ��ֵ5���0�����ʱ���ڣ�mTty��״̬û�з����仯 */
            break;
        default:/* ˵���ȴ�ʱ�仹δ��5���0���룬mTty��״̬�����˱仯 */
            if(FD_ISSET(mTtyfd,&readfd)) { /* ���ж�һ��mTty���ⱻ���ӵľ���Ƿ���ı�ɿɶ����� */
                int len = read(mTtyfd,buf,sizeof(buf));
               // LOGE("#####################richard: read len: %d #####",len);
               // LOGE("#####################richard: read buf: %02x, #####",buf[0]);
                /**��������**/
                if(!(arg))break;
                JNIMyObserver *l = static_cast<JNIMyObserver *>(arg);
                l->OnEvent(buf,len,RECEIVE_DATA_INDEX);
                memset(buf,0,sizeof(buf));
            }
            break;
        }
        if(result == -1) {
            break;
        }
    }
    if(buf != NULL) {
        delete buf;
        buf = NULL;
    }
#endif
    LOGE("stop run!");
    return NULL;
}

/*
* _receiveMsgFromTty
*/
static void JNICALL com_notioni_uart_manager_TtyNativeControl__receiveMsgFromTty(JNIEnv *env,jobject clazz)
{
    LOGW("com_notioni_uart_manager_TtyNativeControl__receiveMsgFromTty");
    if(mTtyfd < 0) {
        LOGE("mTtyfd open failure ,non't read");
        return ;
    }
    pthread_t id;
    int ret;
    ret = pthread_create(&id,NULL,threadreadTtyData,listener);
    //ret = pthread_create(&id,NULL,threadreadTtyData,NULL);
    if(ret != 0) {
        LOGE("create receiver thread failure ");
    } else {
        LOGW("create read data thred success");
    }
    return;
}

/**
* ���ô������ݣ�У��λ,���ʣ�ֹͣλ
* @param nBits ���� int����λ ȡֵ λ7��8
* @param nEvent ���� char У������ ȡֵN ,E, O,,S
* @param mSpeed ���� int ���� ȡֵ 2400,4800,9600,115200
* @param mStop ���� int ֹͣλ ȡֵ1 ���� 2
*/
int set_opt(int nBits,char nEvent,int nSpeed,int nStop) {
    LOGW("set_opt:nBits=%d,nEvent=%c,nSpeed=%d,nStop=%d",nBits,nEvent,nSpeed,nStop);
    struct termios newtio,oldtio;
    if(tcgetattr(mTtyfd,&oldtio) != 0) {
        LOGE("setup serial failure");
        return -1;
    }
    bzero(&newtio,sizeof(newtio));
    //c_cflag��־���Զ���CLOCAL��CREAD���⽫ȷ���ó��򲻱������˿ڿ��ƺ��źŸ��ţ�
    //ͬʱ������������ȡ��������ݡ�CLOCAL��CREADͨ�����Ǳ����ܵ�
    newtio.c_cflag |=CLOCAL|CREAD;
    //newtio.c_cflag &=~CSIZE;
    switch(nBits) { //��������λ��
    case 7:
        newtio.c_cflag &=~CSIZE;
        newtio.c_cflag |=CS7;
        break;
    case 8:
        newtio.c_cflag &=~CSIZE;
        newtio.c_cflag |=CS8;
        break;
    default:
        LOGW("nBits:%d,invalid param",nBits);
        break;
    }
    switch(nEvent) { //����У��λ
    case 'O':
        newtio.c_cflag |=PARENB;//enable parity checking
        newtio.c_cflag |=PARODD;//��У��λ
        newtio.c_iflag |=(INPCK|ISTRIP);
        //options.c_iflag |= INPCK;//Disable parity checking
        break;
    case 'E':
        newtio.c_cflag|=PARENB;//
        newtio.c_cflag&=~PARODD;//żУ��λ
        newtio.c_iflag|=(INPCK|ISTRIP);
        //options.c_iflag |= INPCK;//Disable parity checking
        break;
    case 'N':
        newtio.c_cflag &=~PARENB;//���У��λ
        //options.c_iflag &=~INPCK;//Enable parity checking
        break;
    //case 'S':
    // options.c_cflag &= ~PARENB;//���У��λ
    // options.c_cflag &=~CSTOPB;
    // options.c_iflag |=INPCK;//Disable parity checking
    // break;
    default:
        LOGW("nEvent:%c,invalid param",nEvent);
        break;
    }
    switch(nSpeed) { //��������
    case 2400:
        LOGW("B2400:%d",B2400);
        cfsetispeed(&newtio,B2400);
        cfsetospeed(&newtio,B2400);
        break;
    case 4800:
        LOGW("B4800:%d",B4800);
        cfsetispeed(&newtio,B4800);
        cfsetospeed(&newtio,B4800);
        break;
    case 9600:
        LOGW("B9600:%d",B9600);
        cfsetispeed(&newtio,B9600);
        cfsetospeed(&newtio,B9600);
        break;
    case 115200:
        LOGW("B115200:%d",B115200);
        cfsetispeed(&newtio,B115200);
        cfsetospeed(&newtio,B115200);
        break;
    default:
        cfsetispeed(&newtio,B9600);
        cfsetospeed(&newtio,B9600);
        LOGW("nSpeed:%d,invalid param",nSpeed);
        break;
    }
    switch(nStop) { //����ֹͣλ
    case 1:
        newtio.c_cflag &= ~CSTOPB;
        break;
    case 2:
        newtio.c_cflag |= CSTOPB;
        break;
    default:
        LOGW("nStop:%d,invalid param",nStop);
        break;
    }
    newtio.c_cc[VTIME] = 0;//���õȴ�ʱ��
    newtio.c_cc[VMIN] = 0;//������С�����ַ�
    tcflush(mTtyfd,TCIFLUSH);
    if(tcsetattr(mTtyfd,TCSANOW,&newtio) != 0) {
        LOGE("options set error");
        return -1;
    }
    return 1;
}

/*
* _configTty
*/
static int JNICALL com_notioni_uart_manager_TtyNativeControl__configTty(
    JNIEnv *env,jobject clazz,int nBits,jchar nEvent,int nSpeed,int nStop) 
{
    LOGW("com_notioni_uart_manager_TtyNativeControl__configTty");
    return set_opt(nBits,nEvent,nSpeed,nStop);
}

int set_mode(int nMode,int showLog)
{
    LOGW("set_mode:nMode%d,nshowLog=%d",nMode,showLog);
    struct termios options;
    if(tcgetattr(mTtyfd,&options) != 0) {
        LOGE("setup serial failure");
        return -1;
    }
    int result = 1;
    if(nMode != 0) {
        if(nMode ==1) {
            options.c_lflag &=~(ICANON | ECHO | ECHOE | ISIG);//input
            options.c_oflag &=~OPOST;//out put
        } else if(nMode == 2) {
            options.c_lflag |=(ICANON | ECHO | ECHOE | ISIG);//input
            options.c_oflag |=OPOST;//out put
        }
        if(tcsetattr(mTtyfd,TCSANOW,&options) != 0) {
            LOGE("tcsetattr device fail");
            result = -1;
        }
    }
    if(showLog == 1) {
        LOGI("options c_cflag.CS7:%d,CS8:%d",options.c_cflag & CS7,options.c_cflag & CS8);
        LOGI("options c_cflag.PARENB:%d,PARODD:%d",options.c_cflag & PARENB,options.c_cflag & PARODD);
        LOGI("options c_iflag.INPCK%d,ISTRIP:%d",options.c_iflag & INPCK,options.c_iflag & ISTRIP);
        LOGI("option c_ispeed:%d,c_ospeed:%d",cfgetispeed(&options) ,cfgetospeed(&options));
        LOGI("options c_cflag.CSTOPB:%d,",options.c_cflag & CSTOPB);
        LOGI("options c_cc.VTIME:%d,VMIN:%d",options.c_cc[VTIME],options.c_cc[VMIN]);
        LOGI("options c_cflag.CLOCAL:%d,CREAD:%d",options.c_cflag & CLOCAL,options.c_cflag&CREAD);
        LOGI("options c_lflag.ICANON:%d,ECHO:%d,ECHOE:%d,ISIG:%d",options.c_lflag & ICANON,options.c_lflag&ECHO,options.c_lflag&ECHOE,options.c_lflag&ISIG);
        LOGI("options c_oflag.OPOST:%d,",options.c_oflag &OPOST);
    }
    return result;
}

static int JNICALL com_notioni_uart_manager_TtyNativeControl__setMode(JNIEnv *env,jobject clazz,int nMode,int showLog) {
    LOGW("com_notioni_uart_manager_TtyNativeControl__setMode");
    return set_mode(nMode,showLog);
}

static JNINativeMethod method_table[] = {
    {"native_setup","(Ljava/lang/Object;)V",(void*)com_notioni_uart_manager_TtyNativeControl_native_setup},
    {"_openTty","()I",(void*)com_notioni_uart_manager_TtyNativeControl__openTty},
    {"_closeTty","()I",(void*)com_notioni_uart_manager_TtyNativeControl__closeTty},
    {"_sendMsgToTty","([B)I",(void*)com_notioni_uart_manager_TtyNativeControl__sendMsgToTty},
    {"_receiveMsgFromTty","()V",(void*)com_notioni_uart_manager_TtyNativeControl__receiveMsgFromTty},
    {"_configTty","(ICII)I",(void*)com_notioni_uart_manager_TtyNativeControl__configTty},
    {"_setMode","(II)I",(void*)com_notioni_uart_manager_TtyNativeControl__setMode},
};

static const char* classPathName="com/notioni/uart/manager/TtyNativeControl";
static int register_com_notioni_uart_manager_TtyNativeControl(JNIEnv *env) {
    LOGW("register_com_notioni_uart_manager_TtyNativeControl");
    jclass clazz;
    clazz = env->FindClass(classPathName);
    if(clazz == NULL) {
        return -1;
    }
    fields.post_event = env->GetStaticMethodID(clazz,"postEventFromNative",
                        "(Ljava/lang/Object;IIILjava/lang/Object;)V");
    if(fields.post_event == NULL) {
        LOGE("Can't find com/notioni/uart/manager.postEventFromNative");
        return -1;
    }
    return AndroidRuntime::registerNativeMethods(env,classPathName,method_table,NELEM(method_table));
}

jint JNI_OnLoad(JavaVM* vm,void* reserved) {
    LOGW("JNI_OnLoad");
    JNIEnv* env = NULL;
    jint result = -1;
    if(vm->GetEnv((void**)&env,JNI_VERSION_1_4) != JNI_OK) {
        goto bail;
    }
    LOGW("register mothod");
    assert(env != NULL);
    if(register_com_notioni_uart_manager_TtyNativeControl(env) < 0) {
        goto bail;
    }
    return JNI_VERSION_1_4;
bail:
    return result;
}
