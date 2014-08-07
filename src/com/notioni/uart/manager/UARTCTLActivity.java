package com.notioni.uart;

import com.notioni.uart.manager.TtyNativeControl;
import com.notioni.uart.manager.R;
import com.notioni.uart.manager.UARTCTLManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class UARTCTLActivity extends Activity implements View.OnClickListener
{
    private static final String TAG = "UARTCTLActivity";
    private UARTCTLManager mUartctlManager;
    private Button mOpenOrCloseBtn;
    private Button mSendBtn;
    private EditText mSendText;
    private TextView mReceiveText;
    private ReceiveData mReceiveData;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mUartctlManager = UARTCTLManager.getInstance();
        mReceiveData = new ReceiveData();
        mOpenOrCloseBtn = (Button)findViewById(R.id.openOrcloseBtn);
        mSendBtn = (Button)findViewById(R.id.sendBtn);
        mSendText = (EditText)findViewById(R.id.sendMsg);
        mReceiveText = (TextView)findViewById(R.id.receiveMsg);
        mOpenOrCloseBtn.setOnClickListener(this);
        mSendBtn.setOnClickListener(this);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }
    
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
        case R.id.openOrcloseBtn://打开或关闭驱动
            Log.i(TAG, "isOpen:"+mUartctlManager.isOpenDevice());
            if(mUartctlManager.isOpenDevice()) {
                mUartctlManager.closeDevice();
                mOpenOrCloseBtn.setText(R.string.open);
            } else {
                int i =mUartctlManager.openDevice();
                Log.i(TAG, "openDevice result:"+i);
                mOpenOrCloseBtn.setText(R.string.close);
                mUartctlManager.setMode(1, 1);//查看串口配置信息
            }
            break;
        case R.id.sendBtn:
            openDevice();
            int re = mUartctlManager.sendDataToDevice(getSendText());
            Log.i(TAG, "send result:"+re);
            break;
        }
    }
    
    /*
    * 打开驱动
    */
    private void openDevice() {
        if(!mUartctlManager.isOpenDevice()) {
            mUartctlManager.openDevice();
            mUartctlManager.receiveDataFromDevice(mReceiveData);
        }
    }
    
    /*
    * 关闭驱动
    */
    public void closeDevice() {
        if(mUartctlManager.isOpenDevice()) {
            mUartctlManager.closeDevice();
        }
    }
    
    /*
    * 取出待发送的数据
    */
    private byte[] getSendText() {
        String st = mSendText.getText().toString()+"\r";
        if(st == null || "".equals(st)) {
            return null;
        }
        return st.getBytes();
    }
    
    /*
    * 接收数据
    */
    class ReceiveData implements UARTCTLManager.ReceiveDataCallBack {
        @Override
        public void onReceiveData(byte[] data, TtyNativeControl tty) {
            if(mReceiveText != null && data != null) {
                Log.w(TAG, "[onReceiveData] data:"+data.toString());
                mReceiveText.setText(data.toString());
            }
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1,1,0,R.string.config);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == 1) {
            Intent intent = new Intent();
            intent.setClass(this, ConfigActivity.class);
            startActivity(intent);
        }
        return true;
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // closeDevice();
    }
    
    @Override
    protected void onDestroy() {
        closeDevice();
        super.onDestroy();
    }
}
