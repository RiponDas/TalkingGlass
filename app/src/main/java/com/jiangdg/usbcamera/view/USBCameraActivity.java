package com.jiangdg.usbcamera.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.usb.UsbDevice;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

//import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.samples.vision.ocrreader.OcrCaptureActivity;
import com.google.firebase.codelab.mlkit.MainActivity;
import com.jiangdg.usbcamera.R;
import com.jiangdg.usbcamera.UVCCameraHelper;
import com.jiangdg.usbcamera.application.MyApplication;
import com.jiangdg.usbcamera.utils.FileUtils;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.common.AbstractUVCCameraHandler;
import com.serenegiant.usb.encoder.RecordParams;
import com.serenegiant.usb.widget.CameraViewInterface;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class USBCameraActivity extends AppCompatActivity implements CameraDialog.CameraDialogParent, CameraViewInterface.Callback {
    private static final String TAG = "Debug";
    @BindView(R.id.camera_view)
    public View mTextureView;
    @BindView(R.id.toolbar)
    public Toolbar mToolbar;
    //@BindView(R.id.seekbar_brightness)
    //public SeekBar mSeekBrightness;
    //@BindView(R.id.seekbar_contrast)
    //public SeekBar mSeekContrast;
    //@BindView(R.id.switch_rec_voice)
    public Switch mSwitchVoice;

    @BindView(R.id.imageView)
    public ImageView mImageView;


    public FloatingActionButton floatingGallery;
    public FloatingActionButton floatingObstacle;
    public FloatingActionButton mPicButton;
    public FloatingActionButton floatingCamera;
    public FloatingActionButton floatingLive;
    public FloatingActionButton floatingCloudText;


    FloatingActionButton floatingFace, floatingObject, floatingText;
    FloatingActionButton floatingUSBCamera;
    TextView ProximitySensor, imageTextData,text;
    SensorManager mySensorManager;
    Sensor myProximitySensor;

    private static final String IMAGE_DIRECTORY = "/TalkingGlass";
    private int GALLERY = 1, CAMERA = 2;
    private int flag=0;


    public byte[] byteArrayNew;

    public int temp;
    public byte[] byteArray;

    private UVCCameraHelper mCameraHelper;
    private CameraViewInterface mUVCCameraView;
    private AlertDialog mDialog;

    private boolean isRequest;
    private boolean isPreview;

    public Bitmap bitImage;

    private UVCCameraHelper.OnMyDevConnectListener listener = new UVCCameraHelper.OnMyDevConnectListener() {

        @Override
        public void onAttachDev(UsbDevice device) {
            // request open permission
            if (!isRequest) {
                isRequest = true;
                if (mCameraHelper != null) {
                    temp = mCameraHelper.getUsbDeviceCount();
                    if(temp==0){
                        showShortMsg("Camera Count is 0");
                    }
                    else{
                        showShortMsg("Camera Found in camera count");
                    }

                    mCameraHelper.requestPermission(0);
                }
            }
        }

        @Override
        public void onDettachDev(UsbDevice device) {
            // close camera
            if (isRequest) {
                isRequest = false;
                mCameraHelper.closeCamera();
                showShortMsg(device.getDeviceName() + " is out");
            }
        }
        @Override
        public void onConnectDev(UsbDevice device, boolean isConnected) {
            if (!isConnected) {
                showShortMsg("fail to connect,please check resolution params");
                isPreview = false;
            } else {
                isPreview = true;
                showShortMsg("connecting");
                // initialize seekbar
                // need to wait UVCCamera initialize over
                new Thread(() -> {
                    try {
                        Thread.sleep(2500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Looper.prepare();
                    if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                       //mSeekBrightness.setProgress(mCameraHelper.getModelValue(UVCCameraHelper.MODE_BRIGHTNESS));
                       // mSeekContrast.setProgress(mCameraHelper.getModelValue(UVCCameraHelper.MODE_CONTRAST));
                    }
                    Looper.loop();
                }).start();
            }
        }
        @Override
        public void onDisConnectDev(UsbDevice device) {
            showShortMsg("disconnecting");
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_activity);
        ButterKnife.bind(this);
        //initView();

        // step.1 initialize UVCCameraHelper
        mUVCCameraView = (CameraViewInterface) mTextureView;
        mUVCCameraView.setCallback(this);
        mCameraHelper = UVCCameraHelper.getInstance();
        mCameraHelper.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_MJPEG);
        mCameraHelper.initUSBMonitor(this, mUVCCameraView, listener);


        mCameraHelper.setOnPreviewFrameListener(nv21Yuv -> Log.d(TAG, "onPreviewResult: "+nv21Yuv.length));

        floatingObstacle = (FloatingActionButton) findViewById(R.id.fabObstacle);
        floatingGallery = (FloatingActionButton) findViewById(R.id.fabGallery);
        floatingCamera = (FloatingActionButton) findViewById(R.id.fabCamera);
        floatingLive = (FloatingActionButton) findViewById(R.id.fabLive);
        floatingCloudText = (FloatingActionButton) findViewById(R.id.fabAdvanceText);
        mPicButton = (FloatingActionButton)findViewById(R.id.fabTakePic);

        floatingObstacle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO something when floating action menu first item clicked
                mySensorManager = (SensorManager) getSystemService(
                        Context.SENSOR_SERVICE);
                myProximitySensor = mySensorManager.getDefaultSensor(
                        Sensor.TYPE_PROXIMITY);
                if (myProximitySensor == null) {
                    showShortMsg("sorry,No Proximity Sensor Detected");
                } else {
                    mySensorManager.registerListener(proximitySensorEventListener,
                            myProximitySensor,
                            SensorManager.SENSOR_DELAY_NORMAL);
                }
            }
        });
        floatingGallery.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO something when floating action menu first item clicked
                //Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                //    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // startActivityForResult(galleryIntent, GALLERY);
                launchImagePicker();
            }
        });
        floatingCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA);
            }
        });
        mPicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCameraHelper == null || !mCameraHelper.isCameraOpened()) {
                    showShortMsg("sorry,camera open failed");
                    //return super.onOptionsItemSelected(item);
                }
                String picPath = MyApplication.getPictureFolder()+ System.currentTimeMillis()
                        + UVCCameraHelper.SUFFIX_JPEG;
                mCameraHelper.capturePicture(picPath, path -> {
                    bitImage = BitmapFactory.decodeFile(path);
                    // mImageView.setImageBitmap(bitImage);
                    Log.i(TAG,"save path：" + path);
                });
            }
        });
        floatingLive.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO something when floating action menu third item clicked
                Intent intent = new Intent(USBCameraActivity.this, OcrCaptureActivity.class );
                startActivity(intent);
            }
        });
        floatingCloudText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    ByteArrayOutputStream bStream = new ByteArrayOutputStream();
                    bitImage.compress(Bitmap.CompressFormat.PNG, 100, bStream);
                    byte[] byteArray = bStream.toByteArray();

                    Intent anotherIntent = new Intent(USBCameraActivity.this, MainActivity.class);
                    anotherIntent.putExtra("image", byteArray);
                    startActivity(anotherIntent);
                    finish();
            }
        });
    }

    private void launchImagePicker() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    bitImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    String path = saveImage(bitImage);
                    Toast.makeText(USBCameraActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
                    mImageView.setImageBitmap(bitImage);

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(USBCameraActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == CAMERA) {
            bitImage = (Bitmap) data.getExtras().get("data");
            mImageView.setImageBitmap(bitImage);
            saveImage(bitImage);
            Toast.makeText(USBCameraActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
        }
    }
    public String saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, Calendar.getInstance()
                    .getTimeInMillis() + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved::--->" + f.getAbsolutePath());

            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }


    SensorEventListener proximitySensorEventListener
            = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub
        }
        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
            if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                if (event.values[0] == 0) {
                    showShortMsg("Obstacle Detected");
                } else {
                    showShortMsg("No");
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        // step.2 register USB event broadcast
        if (mCameraHelper != null) {
            mCameraHelper.registerUSB();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // step.3 unregister USB event broadcast
        if (mCameraHelper != null) {
            mCameraHelper.unregisterUSB();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toobar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_takepic:
                if (mCameraHelper == null || !mCameraHelper.isCameraOpened()) {
                    showShortMsg("sorry,camera open failed");
                    return super.onOptionsItemSelected(item);
                }
                String picPath = MyApplication.getPictureFolder()+ System.currentTimeMillis()
                        + UVCCameraHelper.SUFFIX_JPEG;
                mCameraHelper.capturePicture(picPath, path -> {
                    bitImage = BitmapFactory.decodeFile(path);
                   // mImageView.setImageBitmap(bitImage);
                    Log.i(TAG,"save path：" + path);
                });



                break;
            case R.id.menu_recording:
                if (mCameraHelper == null || !mCameraHelper.isCameraOpened()) {
                    showShortMsg("sorry,camera open failed");
                    return super.onOptionsItemSelected(item);
                }
                if (!mCameraHelper.isPushing()) {
                    String videoPath = MyApplication.getVideoFolder() + System.currentTimeMillis();
                    FileUtils.createfile(FileUtils.ROOT_PATH + "test666.h264");
                    // if you want to record,please create RecordParams like this
                    RecordParams params = new RecordParams();
                    params.setRecordPath(videoPath);
                    params.setRecordDuration(0);                       // 设置为0，不分割保存
                    params.setVoiceClose(mSwitchVoice.isChecked());    // is close voice
                    mCameraHelper.startPusher(params, new AbstractUVCCameraHandler.OnEncodeResultListener() {
                        @Override
                        public void onEncodeResult(byte[] data, int offset, int length, long timestamp, int type) {
                            // type = 1,h264 video stream
                            if (type == 1) {
                                FileUtils.putFileStream(data, offset, length);
                            }
                            // type = 0,aac audio stream
                            if(type == 0) {

                            }
                        }

                        @Override
                        public void onRecordResult(String videoPath) {
                            Log.i(TAG,"videoPath = "+videoPath);
                        }
                    });
                    // if you only want to push stream,please call like this
                    // mCameraHelper.startPusher(listener);
                    showShortMsg("start record...");
                    mSwitchVoice.setEnabled(false);
                } else {
                    FileUtils.releaseFile();
                    mCameraHelper.stopPusher();
                    showShortMsg("stop record...");
                    mSwitchVoice.setEnabled(true);
                }
                break;
            case R.id.menu_resolution:
                if (mCameraHelper == null || !mCameraHelper.isCameraOpened()) {
                    showShortMsg("sorry,camera open failed");
                    return super.onOptionsItemSelected(item);
                }
                showResolutionListDialog();
                break;
            case R.id.menu_focus:
                if (mCameraHelper == null || !mCameraHelper.isCameraOpened()) {
                    showShortMsg("sorry,camera open failed");
                    return super.onOptionsItemSelected(item);
                }
                mCameraHelper.startCameraFoucs();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showResolutionListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(USBCameraActivity.this);
        View rootView = LayoutInflater.from(USBCameraActivity.this).inflate(R.layout.layout_dialog_list, null);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_dialog);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(USBCameraActivity.this, android.R.layout.simple_list_item_1, getResolutionList());
        if (adapter != null) {
            listView.setAdapter(adapter);
        }
        listView.setOnItemClickListener((adapterView, view, position, id) -> {
            if (mCameraHelper == null || !mCameraHelper.isCameraOpened())
                return;
            final String resolution = (String) adapterView.getItemAtPosition(position);
            String[] tmp = resolution.split("x");
            if (tmp != null && tmp.length >= 2) {
                int widht = Integer.valueOf(tmp[0]);
                int height = Integer.valueOf(tmp[1]);
                mCameraHelper.updateResolution(widht, height);
            }
            mDialog.dismiss();
        });

        builder.setView(rootView);
        mDialog = builder.create();
        mDialog.show();
    }

    // example: {640x480,320x240,etc}
    private List<String> getResolutionList() {
        List<Size> list = mCameraHelper.getSupportedPreviewSizes();
        List<String> resolutions = null;
        if (list != null && list.size() != 0) {
            resolutions = new ArrayList<>();
            for (Size size : list) {
                if (size != null) {
                    resolutions.add(size.width + "x" + size.height);
                }
            }
        }
        return resolutions;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FileUtils.releaseFile();
        // step.4 release uvc camera resources
        if (mCameraHelper != null) {
            mCameraHelper.release();
        }
    }

    private void showShortMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public USBMonitor getUSBMonitor() {
        return mCameraHelper.getUSBMonitor();
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (canceled) {
            showShortMsg("Cancel operation");
        }
    }

    public boolean isCameraOpened() {
        return mCameraHelper.isCameraOpened();
    }

    @Override
    public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
        if (!isPreview && mCameraHelper.isCameraOpened()) {
            mCameraHelper.startPreview(mUVCCameraView);
            isPreview = true;
        }
    }

    @Override
    public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {

    }

    @Override
    public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
        if (isPreview && mCameraHelper.isCameraOpened()) {
            mCameraHelper.stopPreview();
            isPreview = false;
        }
    }
}
