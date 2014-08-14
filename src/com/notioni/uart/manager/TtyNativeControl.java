package com.notioni.uart.manager;

import java.lang.ref.WeakReference;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
* 本地方法类
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
    * 打开驱动
    * @return 是否打开成功
    */
    public int openTty() {
        return _openTty();
    }
    
    /**
    * 关闭驱动，需要一定时间，所以采用Handler机制
    */
    public int closeTty() {
        // mEventHandler.obtainMessage(TTY_CLOSE_DEVICE).sendToTarget();
        // return 1;
        return _closeTty();
    }

    /**
    * 发送数据
    * @param data
    * @return
    */

    public int sendMsgToTty(byte[] data) {
        return _sendMsgToTty(data);
    }

    /**
    * 接收数据
    * @param callback
    */
    public final void receiveMsgFromTty(ReceiveCallback callback) {
        mReceiveCallBack = callback;
        _receiveMsgFromTty();
    }

    /**
    * 设置串口数据位，校验位,速率，停止位
    * @param databits 数据位 取值 位7或8
    * @param event 校验类型 取值N ,E, O,
    * @param speed 速率 取值 2400,4800,9600,115200
    * @param stopBit 停止位 取值1 或者 2
    */

    public int configTty(int databits,char event,int speed,int stopBit) {
        return _configTty(databits, event, speed, stopBit);
    }


    /**
    * @param mode 是否使用原始模式(Raw Mode)方式来通讯 取值0,1,2 说明：0=nothing,1=Raw mode,2=no raw mode
    * @param showLog 打印出串口信息Log 取值1,0
    */
    public int setMode(int mode ,int showLog) {
        return _setMode(mode, showLog);
    }

    /**
    * 接收数据回调接口
    */
    public interface ReceiveCallback {
        void onReceiveData(byte[] data,TtyNativeControl tty);
    }


    /****************************************************************
    * 本地方法
    */
    private native final void native_setup(Object tty_this);
    private native int _openTty();
    private native int _closeTty();
    private native int _sendMsgToTty(byte[] data);
    private native void _receiveMsgFromTty();
    private native int _configTty(int databits,char event,int speed,int stopBit);
    private native int _setMode(int mode,int showLog);

    /*
    * 实现底层回调
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
            case TTY_MSG_RECEIVE:       //底层接收数据回调上来的事件
                if(mReceiveCallBack != null) {
                    mReceiveCallBack.onReceiveData((byte[])msg.obj,mTty);
                }
                return;
            case TTY_CLOSE_DEVICE://关闭驱动
                _closeTty();
                break;
            }
        }
    }
}
