package com.notioni.uart.manager;
import com.notioni.uart.manager.TtyNativeControl.ReceiveCallback;

/*
* 单例模式
*/
public class UARTCTLManager 
{
    private TtyNativeControl mTtyControl;
    private boolean mIsOpen = false;
    private static UARTCTLManager mManager;
    
    private UARTCTLManager() {
    	//super().TtyNativeControl();
        mTtyControl = new TtyNativeControl();
    }
   
    public static UARTCTLManager getInstance() {
        if(mManager == null) {
            mManager = new UARTCTLManager();
        }
        return mManager;
    }
    
    /*
    * 打开驱动
    */
    public int openDevice() {
        int o = mTtyControl.openTty();
        o=1;
        if(o == 1) {
            mIsOpen = true;
        }
        return o;
    }
    
    /*
    * 关闭驱动
    */
    public int closeDevice() {
        int c = mTtyControl.closeTty();
        if(c == 1) {
            mIsOpen = false;
        }
        return c;
    }
    
    /*
    * 判断驱动是否打开
    */
    public boolean isOpenDevice() {
        return mIsOpen;
    }
    
    /*
    * 发送数据
    */
    public int sendDataToDevice(byte[] data) {
        return mTtyControl.sendMsgToTty(data);
    }
    
    /*
    * 注入接收数据回调方法
    */
    public void receiveDataFromDevice(ReceiveCallback callback) {
        mTtyControl.receiveMsgFromTty(callback);
    }
    
    /**
    * 设置串口数据位，校验位,速率，停止位
    * @param databits 数据位 取值 位7或8
    * @param event 校验类型 取值N ,E, O,,S
    * @param speed 速率 取值 2400,4800,9600,115200
    * @param stopBit 停止位 取值1 或者 2
    */
    public int configDevice(int databits,char event,int speed,int stopBit) {
        if(!mIsOpen) {
            return -1;
        }
        int re = mTtyControl.configTty(databits, event, speed, stopBit);
        return re;
    }
    
    /**
    * 设置串口通信模式，打印串口信息
    * @param mode 是否使用原始模式(Raw Mode)方式来通讯 取值0,1,2 说明：0=nothing,1=Raw mode,2=no raw mode
    * @param showLog 打印出串口信息Log 取值1,0
    * @return
    */
    public int setMode(int mode ,int showLog) {
        return mTtyControl.setMode(mode, showLog);
    }
    
    /**
    *
    * 接收数据回调接口，接收驱动送到的数据要实现这个回调接口
    */
    public interface ReceiveDataCallBack extends ReceiveCallback {
    }
}
