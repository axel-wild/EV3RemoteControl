package com.example.ev3control;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.ev3control.util.AccSensorBuffer;

import org.catrobat.catroid.bluetooth.ConnectBluetoothDeviceActivity;
import org.catrobat.catroid.bluetooth.base.BluetoothDevice;
import org.catrobat.catroid.common.CatroidService;
import org.catrobat.catroid.common.ServiceProvider;
import org.catrobat.catroid.devices.mindstorms.ev3.LegoEV3;


public class MainActivity extends AppCompatActivity implements View.OnTouchListener, SensorEventListener {

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    /** Lego EV3 Brick to control */
    private LegoEV3 legoEV3;

    AccSensorBuffer accBuffer = AccSensorBuffer.getInstance();

    private SensorManager sensorManager;

    /** toggle button ON/OFF*/
    ToggleButton btnStartStop;
    /** visible control for controlling */
    ImageView joyStickControl;
    /** visible bounds for joyStickControl */
    RelativeLayout joyStickBounds;

    // moving control with touch
    private float dX;
    private float dY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //btnStartStop  = (ToggleButton)findViewById(R.id.btnStartStop); // not used mor checked yet
        joyStickControl = findViewById(R.id.iv_control);
        joyStickBounds = findViewById(R.id.layoutControl);

        joyStickControl.setOnTouchListener(this);

        /* Sensor */
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor sensorACC = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    }

    /**
     * Callback after bluetooth pairing
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the BLuetoothDevice object
                    legoEV3 = ServiceProvider.getService(CatroidService.BLUETOOTH_DEVICE_SERVICE).getDevice(LegoEV3.class);
                    // start motorA with speed 50% backwards
                    //legoEV3.moveMotorSpeed(legoEV3.getMotorA().getOutputField(), 0, -50);
                    legoEV3.playTone(80, 1000, 100);
                    // now have fun!!!
                }
                break;
        }
    }


    /**
     * Updates GUI
     */
    private void updateGuiData(){
        TextView textView = findViewById(R.id.textView);

        float xRel, yRel;
        float xCenter, yCenter;
        float xControlCenter, yControlCenter;

        xCenter = joyStickBounds.getWidth() / 2;
        yCenter = joyStickBounds.getHeight() / 2;

        xControlCenter = joyStickControl.getX() + (joyStickControl.getWidth() / 2);
        yControlCenter = joyStickControl.getY() + joyStickControl.getHeight() / 2;

        xRel =  (xCenter - xControlCenter) / yCenter * 100;
        yRel = (yCenter - yControlCenter) / yCenter * 100;

        textView.setText("xRel: " + String.format("%3.1f", xRel) + "%; yRel: " + String.format("%3.1f", yRel) + "%");

        /* debug */
        TextView tvSpeed = findViewById(R.id.tv_speed);
        TextView tvAcc = findViewById(R.id.tv_acc);

        tvAcc.setText("Acc: xAVG=" + String.format("%3.1f", this.accBuffer.getAVG()[0]) + "; yAVG=" + String.format("%3.1f", this.accBuffer.getAVG()[1]));
        //tvSpeed.setText("Speed=" + this.legoEV3.getMotorA();
    }

    /**
     *
     * @param xNew
     * @param yNew
     */
    private void move(float xNew, float yNew){
        moveGuiControl(xNew,yNew);
        moveModelControl(xNew,yNew);
    }

    /**
     * controls the gui
     * @param xNew
     * @param yNew
     */
    private void moveGuiControl(float xNew, float yNew){
        joyStickControl.animate()
                .x(xNew  - joyStickControl.getWidth()/2)
                .y(yNew  - joyStickControl.getHeight()/2)
                .setDuration(0)
                .start();
        updateGuiData();
    }

    /**
     * controls robot
     * @param xNew
     * @param yNew
     */
    private void moveModelControl(float xNew, float yNew){
        float speed;

        speed = limit(yNew, 0, joyStickBounds.getHeight(), 100, -100);

        sendData(xNew, speed);
    }

    /**
     * commits data to EV3 brick
     * @param x
     * @param y
     */
    private void sendData(float x, float y){
        //legoEV3.setSpeedMessage(EV3MotorOutputByteCode.MOTOR_A_OUT.getByte(), 0, (int)y);
        if(legoEV3 != null) {
            legoEV3.moveMotorSpeed(legoEV3.getMotorA().getOutputField(), 0, (int) y);
        }
    }

    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                dX = view.getX() - event.getRawX();
                dY = view.getY() - event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                float xNew = event.getRawX() + dX;
                float yNew = event.getRawY() + dY;

                this.move(xNew, yNew);
                break;
            default:
                return false;
        }
        return true;
    }

    /**
     * Receives sensor data
     * @param sensorEvent
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float x, y;
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] values = sensorEvent.values;

            // buffers data for avg calculation
            this.accBuffer.addValue(values);

            //for c-acceleration and y-acceleration = 0, control should be centered
            x = limit(-this.accBuffer.getAVG()[0], -4, 4, 0, joyStickBounds.getWidth());
            y = limit(this.accBuffer.getAVG()[1], -4, 4, 0, joyStickBounds.getHeight());

            // move control on gui and control robot
            this.move(x, y);
        }
    }

    /**
     * Calculates the the corresponding y-value for input x by a linear function: limit(x) = m*x+t
     * @param x
     * @param x0
     * @param x1
     * @param y0
     * @param y1
     * @return
     */
    private float limit(float x, float x0, float x1, float y0, float y1){
        float m, t;
        x = x < x0 ? x0: x;
        x = x > x1 ? x1: x;

        m = (y1 - y0) / (x1 - x0);
        t = y0 - m * x0;

        return m * x + t;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
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

    @Override
    protected void onResume(){
        super.onResume();

        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                sensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
