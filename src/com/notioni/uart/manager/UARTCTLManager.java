package com.notioni.uart.manager;
import com.notioni.uart.manager.TtyNativeControl.ReceiveCallback;

/*
* ����ģʽ
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
    * ������
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
    * �ر�����
    */
    public int closeDevice() {
        int c = mTtyControl.closeTty();
        if(c == 1) {
            mIsOpen = false;
        }
        return c;
    }
    
    /*
    * �ж������Ƿ��
    */
    public boolean isOpenDevice() {
        return mIsOpen;
    }
    
    /*
    * ��������
    */
    public int sendDataToDevice(byte[] data) {
        return mTtyControl.sendMsgToTty(data);
    }
    
    /*
    * ע��������ݻص�����
    */
    public void receiveDataFromDevice(ReceiveCallback callback) {
        mTtyControl.receiveMsgFromTty(callback);
    }
    
    /**
    * ���ô�������λ��У��λ,���ʣ�ֹͣλ
    * @param databits ����λ ȡֵ λ7��8
    * @param event У������ ȡֵN ,E, O,,S
    * @param speed ���� ȡֵ 2400,4800,9600,115200
    * @param stopBit ֹͣλ ȡֵ1 ���� 2
    */
    public int configDevice(int databits,char event,int speed,int stopBit) {
        if(!mIsOpen) {
            return -1;
        }
        int re = mTtyControl.configTty(databits, event, speed, stopBit);
        return re;
    }
    
    /**
    * ���ô���ͨ��ģʽ����ӡ������Ϣ
    * @param mode �Ƿ�ʹ��ԭʼģʽ(Raw Mode)��ʽ��ͨѶ ȡֵ0,1,2 ˵����0=nothing,1=Raw mode,2=no raw mode
    * @param showLog ��ӡ��������ϢLog ȡֵ1,0
    * @return
    */
    public int setMode(int mode ,int showLog) {
        return mTtyControl.setMode(mode, showLog);
    }
    
    /**
    *
    * �������ݻص��ӿڣ����������͵�������Ҫʵ������ص��ӿ�
    */
    public interface ReceiveDataCallBack extends ReceiveCallback {
    }
}
