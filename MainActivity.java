package com.example.term_project;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "android_camera_example";
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_BACK; // Camera.CameraInfo.CAMERA_FACING_FRONT
    private SurfaceView surfaceView;
    private CameraPreview mCameraPreview;
    private View mLayout;  // Snackbar 사용하기 위해서는 View가 필요합니다.
    // (참고로 Toast에서는 Context가 필요했습니다.)
    //갤러리참조하기
    ImageView sample;
    LayoutInflater ex;
    FrameLayout eex;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 상태바를 안보이도록 합니다.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 화면 켜진 상태를 유지합니다.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mLayout = findViewById(R.id.layout_main);
        surfaceView = findViewById(R.id.camera_preview_main);
        // 런타임 퍼미션 완료될때 까지 화면에서 보이지 않게 해야합니다.
        surfaceView.setVisibility(View.GONE);
        //필터버튼을 누른 액션
        Button filterBt = findViewById(R.id.button_filter);
        filterBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getApplicationContext(), openCV.class);
                startActivity(intent);
                /*
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, GET_GALLERY_IMAGE);
                ((openCV)openCV.context).imageprocess_and_showResult(50, 50);//openCV 액티비티 함수호출
                sample = ((openCV)openCV.context).imageVIewOuput;//openCV 결과 이미지 호출
                */
            }
        });
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder)
            {
                try {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        Log.d("Permission", "Granted");
                        sample.invalidate();
                        return;
                    }
                }catch (Exception e){}
            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
            { }
            @Override
            public void surfaceDestroyed(SurfaceHolder holder)
            {}
        });
        //사진찍기 버튼
        Button button = findViewById(R.id.button_main_capture);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraPreview.takePicture();
            }
        });
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            int writeExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if ( cameraPermission == PackageManager.PERMISSION_GRANTED
                    && writeExternalStoragePermission == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            }else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Snackbar.make(mLayout, "이 앱을 실행하려면 카메라와 외부 저장소 접근 권한이 필요합니다.",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions( MainActivity.this, REQUIRED_PERMISSIONS,
                                    PERMISSIONS_REQUEST_CODE);
                        }
                    }).show();
                } else {
                    // 2. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                    // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                    ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS,
                            PERMISSIONS_REQUEST_CODE);
                }

            }
        }
        else //카메라없는에러
            {
            final Snackbar snackbar = Snackbar.make(mLayout, "디바이스가 카메라를 지원하지 않습니다.",
                    Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("확인", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                }
            });
            snackbar.show();
        }
    }
    void startCamera(){
        // Create the Preview view and set it as the content of this Activity.
        mCameraPreview = new CameraPreview(this, this, CAMERA_FACING, surfaceView);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {
        if ( requestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            boolean check_result = true;
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }
            if ( check_result ) {

                startCamera();
            }
            else //퍼미션거부
                {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                }else {
                    Snackbar.make(mLayout, "설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                }
            }
        }
    }
}
