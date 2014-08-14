package com.notioni.uart.manager;

import java.lang.ref.WeakReference;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
* ���ط�����
*/

public class TtyNativeControl {
    private final static String TAG = "TtyNativeControl";
    static {
        System.loadLibrary("uart_ctl");
    }
    private static final int TTY_MSG_RECEIVE = 1;
    private static final int TTY_CLOSE_DEVICE = TTY_MSG_RECEIVE+1;
    private EventHandler mEventHandler; 
    private ReceiveCallback mReceiveCallBack;
    
    TtyNativeControl() 
    {
        mReceiveCallBack = null;
        Looper looper;
        
        if((looper = Looper.myLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else if((looper = Looper.getMainLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else {
            mEventHandler = null;
        }

        native_setup(new WeakReference<TtyNativeControl>(this));
    }

    /**
    * ������
    * @return �Ƿ�򿪳ɹ�
    */
    public int openTty() {
        return _openTty();
    }
    
    /**
    * �ر���������Ҫһ��ʱ�䣬���Բ���Handler����
    */
    public int closeTty() {
        // mEventHandler.obtainMessage(TTY_CLOSE_DEVICE).sendToTarget();
        // return 1;
        return _closeTty();
    }

    /**
    * ��������
    * @param data
    * @return
    */

    public int sendMsgToTty(byte[] data) {
        return _sendMsgToTty(data);
    }

    /**
    * ��������
    * @param callback
    */
    public final void receiveMsgFromTty(ReceiveCallback callback) {
        mReceiveCallBack = callback;
        _receiveMsgFromTty();
    }

    /**
    * ���ô�������λ��У��λ,���ʣ�ֹͣλ
    * @param databits ����λ ȡֵ λ7��8
    * @param event У������ ȡֵN ,E, O,
    * @param speed ���� ȡֵ 2400,4800,9600,115200
    * @param stopBit ֹͣλ ȡֵ1 ���� 2
    */

    public int configTty(int databits,char event,int speed,int stopBit) {
        return _configTty(databits, event, speed, stopBit);
    }


    /**
    * @param mode �Ƿ�ʹ��ԭʼģʽ(Raw Mode)��ʽ��ͨѶ ȡֵ0,1,2 ˵����0=nothing,1=Raw mode,2=no raw mode
    * @param showLog ��ӡ��������ϢLog ȡֵ1,0
    */
    public int setMode(int mode ,int showLog) {
        return _setMode(mode, showLog);
    }

    /**
    * �������ݻص��ӿ�
    */
    public interface ReceiveCallback {
        void onReceiveData(byte[] data,TtyNativeControl tty);
    }


    /****************************************************************
    * ���ط���
    */
    private native final void native_setup(Object tty_this);
    private native int _openTty();
    private native int _closeTty();
    private native int _sendMsgToTty(byte[] data);
    private native void _receiveMsgFromTty();
    private native int _configTty(int databits,char event,int speed,int stopBit);
    private native int _setMode(int mode,int showLog);

    /*
    * ʵ�ֵײ�ص�
    */

    private static void postEventFromNative(Object tty_ref, int what, int arg1, int arg2, Object obj) 
    {
        //Log.i(TAG, "[postEventFromNative] what:"+what);
        TtyNativeControl t = (TtyNativeControl)((WeakReference)tty_ref).get();
        if(t == null)
            return;
        
        if(t.mEventHandler != null) {
            Message m = t.mEventHandler.obtainMessage(what, arg1, arg2,obj);
            t.mEventHandler.sendMessage(m);
        }
    }

    private class EventHandler extends Handler {
        private TtyNativeControl mTty;
        public EventHandler(TtyNativeControl t,Looper looper) {
            super(looper);
            mTty = t;
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
            case TTY_MSG_RECEIVE:       //�ײ�������ݻص��������¼�
                if(mReceiveCallBack != null) {
                    mReceiveCallBack.onReceiveData((byte[])msg.obj,mTty);
                }
                return;
            case TTY_CLOSE_DEVICE://�ر�����
                _closeTty();
                break;
            }
        }
    }
}
