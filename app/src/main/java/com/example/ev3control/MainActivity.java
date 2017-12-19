package com.example.ev3control;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.catrobat.catroid.bluetooth.ConnectBluetoothDeviceActivity;
import org.catrobat.catroid.bluetooth.base.BluetoothDevice;
import org.catrobat.catroid.common.CatroidService;
import org.catrobat.catroid.common.ServiceProvider;
import org.catrobat.catroid.devices.mindstorms.ev3.LegoEV3;

public class MainActivity extends AppCompatActivity {

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    /** Lego EV3 Brick to control */
    private LegoEV3 legoEV3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the BLuetoothDevice object
                    legoEV3 = ServiceProvider.getService(CatroidService.BLUETOOTH_DEVICE_SERVICE).getDevice(LegoEV3.class);
                    // start motorA with speed 50% backwards
                    legoEV3.moveMotorSpeed(legoEV3.getMotorA().getOutputField(), 0, -50);
                    legoEV3.playTone(80, 1000, 100);
                    // now have fun!!!
                }
                break;
        }
    }

    /**
     * Connect to EV3Brick
     * @param view
     */
    public void connect(View view) {
        Intent intent = new Intent(this, ConnectBluetoothDeviceActivity.class);
        intent.putExtra(ConnectBluetoothDeviceActivity.DEVICE_TO_CONNECT, BluetoothDevice.LEGO_EV3);
        startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
    }
}
