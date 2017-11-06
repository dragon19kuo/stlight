package btcubs.stlight;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class STlightActivity extends AppCompatActivity implements SensorEventListener, Camera.PreviewCallback ,SurfaceHolder.Callback{
    protected SensorManager sensorMgr;
    protected List<Sensor> lightSensor;
    Camera camera;
    int cameraID;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    private byte[] yuvBuffer;
    private void setFlashMode(String mode)
    {
        final Camera.Parameters params = camera.getParameters();
        mode=Camera.Parameters.FLASH_MODE_AUTO;
        params.setFlashMode(mode);
        camera.setParameters(params);
    }
    private void start_camera()
    {
        try{
            camera = Camera.open(cameraID);
        }catch(RuntimeException e){
            Log.e("STLight", "init_camera: " + e);
            return;
        }
        Camera.Parameters param;
        param = camera.getParameters();
        //modify parameter
        param.setPreviewFrameRate(20);
        param.setPreviewSize(176, 144);
        final int yuvPerBits = ImageFormat.getBitsPerPixel(param.getPreviewFormat());
        camera.setParameters(param);
        yuvBuffer = new byte[176 *144 * yuvPerBits / 8];
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            Log.e("STLight", "init_camera: " + e);
            return;
        }
    }

    private void stop_camera()
    {
        camera.stopPreview();
        camera.release();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stlight);
        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorMgr.getSensorList(Sensor.TYPE_LIGHT);
        surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        cameraID=1;

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorMgr.registerListener(this, lightSensor.get(0),SensorManager.SENSOR_DELAY_NORMAL);

        start_camera();
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d("STLight", String.valueOf(event.values[0]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        final Camera.Parameters params = camera.getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

        camera.setParameters(params);
        camera.addCallbackBuffer(yuvBuffer);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        camera.addCallbackBuffer(yuvBuffer);
        camera.setPreviewCallbackWithBuffer(this);
        camera.startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
