package com.notioni.uart;

import com.notioni.uart.manager.UARTCTLManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;
//import com.notioni.uart.manager.R;

public class ConfigActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener{
    private static final String TAG = "ConfigActivity";
    private static final String KEY_DATABITS = "tty_databits";
    private static final String KEY_EVENT = "tty_event";
    private static final String KEY_SPEED = "tty_speed";
    private static final String KEY_STOPBITS = "tty_stopbits";
    ListPreference dataBitsPrefer ;
    ListPreference eventPrefer ;
    ListPreference speedPrefer ;
    ListPreference stopBitsPrefer ;
    Object dataBitsValues = null;
    Object eventValues = null;
    Object speedValues = null;
    Object stopbitsValues = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.config_options);
        PreferenceScreen prefSet = getPreferenceScreen();
        dataBitsPrefer = (ListPreference)prefSet.findPreference(KEY_DATABITS);
        eventPrefer = (ListPreference)prefSet.findPreference(KEY_EVENT);
        speedPrefer = (ListPreference)prefSet.findPreference(KEY_SPEED);
        stopBitsPrefer = (ListPreference)prefSet.findPreference(KEY_STOPBITS);
        
        dataBitsPrefer.setValueIndex(0);
        eventPrefer.setValueIndex(0);
        speedPrefer.setValueIndex(0);
        stopBitsPrefer.setValueIndex(0);
        dataBitsPrefer.setOnPreferenceChangeListener(this);
        eventPrefer.setOnPreferenceChangeListener(this);
        speedPrefer.setOnPreferenceChangeListener(this);
        stopBitsPrefer.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) 
    {
        if(preference == dataBitsPrefer) {
            dataBitsValues = newValue;
        } else if(preference == eventPrefer) {
            eventValues = newValue;
        } else if(preference == speedPrefer) {
            speedValues = newValue;
        } else if(preference == stopBitsPrefer) {
            stopbitsValues = newValue;
        }
        Log.i(TAG, "newValue:"+newValue);
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        UARTCTLManager mUartctlManager = UARTCTLManager.getInstance();
        int databits=7,speed=9600,stopbits=1;//设置默认值
        char event='N';//设置默认值
    
        if(dataBitsValues != null) {
            databits = Integer.parseInt(dataBitsValues.toString());
        }
        if(eventValues != null) {
            event = eventValues.toString().charAt(0);
        }
        if(speedValues != null) {
            speed = Integer.parseInt(speedValues.toString());
        }
        if(stopbitsValues != null) {
            stopbits = Integer.parseInt(stopbitsValues.toString());
        }
        if(!mUartctlManager.isOpenDevice()) {
            mUartctlManager.openDevice();
        }
        
        Log.e(TAG,"databit="+databits+",event="+event+",speed="+speed+",stopbits="+stopbits);
        if(databits == -1 || speed == -1 || stopbits == -1 || event=='Q') {
            Toast.makeText(this, "有参数没有设置，不去配置串口!", Toast.LENGTH_LONG).show();
            return;
        }
        
        mUartctlManager.configDevice(databits, event, speed, stopbits);
        mUartctlManager.setMode(0, 1);//查看串口配置信息

        // Log.e(TAG, "databit="+databits+",event="+event+",speed="+speed+",stopbits="+stopbits);
    }
}
