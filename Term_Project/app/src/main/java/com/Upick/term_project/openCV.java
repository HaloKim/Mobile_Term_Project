package com.Upick.term_project;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class openCV extends AppCompatActivity {
    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }
    ImageView imageVIewInput;
    Bitmap bitmapOutput;
    public ImageView imageVIewOuput;//변환된 이미지
    public String ipath;
    public static Context context;
    private Mat img_input;
    private Mat img_output;
    private int threshold1=50;
    private int threshold2=150;
    private static final String TAG = "opencv";
    private final int GET_GALLERY_IMAGE = 200;
    boolean isReady = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opencv);

        context = this;
        imageVIewInput = (ImageView)findViewById(R.id.imageViewInput);
        imageVIewOuput = (ImageView)findViewById(R.id.imageViewOutput);
        //엣지검출버튼
        Button button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                try {
                    imageprocess_and_showResult(threshold1, threshold2);
                    Toast.makeText(getApplicationContext(), "이미지가 지정 되었습니다", Toast.LENGTH_SHORT).show();
                }catch (Exception e){Toast.makeText(getApplicationContext(),"이미지를 선택해주세요.",Toast.LENGTH_SHORT).show();}
            }
        });
        Button back = (Button)findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent returnedIntent = new Intent();
                setResult(Activity.RESULT_OK, returnedIntent);
                finish();
            }
        });

        final TextView textView1 = (TextView)findViewById(R.id.textView_threshold1);
        SeekBar seekBar1=(SeekBar)findViewById(R.id.seekBar_threshold1);
        seekBar1.setProgress(threshold1);
        seekBar1.setMax(200);
        seekBar1.setMin(0);
        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                threshold1 = progress;
                textView1.setText(threshold1+"");
                imageprocess_and_showResult(threshold1, threshold2);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        final TextView textView2 = (TextView)findViewById(R.id.textView_threshold2);
        SeekBar seekBar2=(SeekBar)findViewById(R.id.seekBar_threshold2);
        seekBar2.setProgress(threshold2);
        seekBar2.setMax(200);
        seekBar2.setMin(0);
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                threshold2 = progress;
                textView2.setText(threshold2+"");
                imageprocess_and_showResult(threshold1, threshold2);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        if (!hasPermissions(PERMISSIONS)) { //퍼미션 허가를 했었는지 여부를 확인
            requestNecessaryPermissions(PERMISSIONS);//퍼미션 허가안되어 있다면 사용자에게 요청
        }

        Intent thisIntent = this.getIntent();

        if (thisIntent.getIntExtra("fromOnline", 0) == 1) {
            Uri uri = (Uri) thisIntent.getExtras().get("itemUri");
            setImage(uri);
        }else {
            //상단 갤러리 이동버튼
            imageVIewInput.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent, GET_GALLERY_IMAGE);
                }
            });
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        isReady = true;
    }
    public native void imageprocessing(long inputImage, long outputImage, int th1, int th2);
    //이미지 변환 후 출력
    public void imageprocess_and_showResult(int th1, int th2)
    {
        if (isReady==false) return;
        if (img_output == null)
            img_output = new Mat();
        imageprocessing(img_input.getNativeObjAddr(), img_output.getNativeObjAddr(), th1, th2);
        bitmapOutput = Bitmap.createBitmap(img_output.cols(), img_output.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img_output, bitmapOutput);
        imageVIewOuput.setImageBitmap(bitmapOutput);
        File storage = context.getCacheDir(); // 이 부분이 임시파일 저장 경로
        String fileName = "Sample" + ".jpg";  // 파일이름은 마음대로!
        File tempFile = new File(storage,fileName);
        try{
            tempFile.createNewFile();  // 파일을 생성해주고
            FileOutputStream out = new FileOutputStream(tempFile);
            bitmapOutput.compress(Bitmap.CompressFormat.JPEG, 10 , out);  // 넘겨 받은 bitmap을 jpeg(손실압축)으로 저장해줌
            out.close(); // 마무리로 닫아줍니다.
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch(Exception e) {
            Toast.makeText(this, "file error", Toast.LENGTH_SHORT).show();
        }
        ipath = tempFile.getAbsolutePath();//이미지경로
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( requestCode == GET_GALLERY_IMAGE){
            if (data.getData() != null) {
                setImage(data.getData());
            }
        }
    }

    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void setImage (Uri uri) {
        try {
            String path = getRealPathFromURI(uri);
            int orientation = getOrientationOfImage(path); // 런타임 퍼미션 필요
            Bitmap temp = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            Bitmap bitmap = getRotatedBitmap(temp, orientation);
            imageVIewInput.setImageBitmap(bitmap);
            img_input = new Mat();
            Bitmap bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Utils.bitmapToMat(bmp32, img_input);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        cursor.moveToFirst();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        return cursor.getString(column_index);
    }
    // 출처 - http://snowdeer.github.io/android/2016/02/02/android-image-rotation/
    public int getOrientationOfImage(String filepath) {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException e) {
            Log.d("@@@", e.toString());
            return -1;
        }
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
        if (orientation != -1) {
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
            }
        }
        return 0;
    }
    public Bitmap getRotatedBitmap(Bitmap bitmap, int degrees) throws Exception {
        if(bitmap == null) return null;
        if (degrees == 0) return bitmap;
        Matrix m = new Matrix();
        m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    }
    //----------------------------------------퍼미션 획득 체크 부분
    // 퍼미션 코드
    static final int PERMISSION_REQUEST_CODE = 1;
    String[] PERMISSIONS  = {"android.permission.WRITE_EXTERNAL_STORAGE"};
    private boolean hasPermissions(String[] permissions) {
        int ret = 0;
        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (String perms : permissions){
            ret = checkCallingOrSelfPermission(perms);
            if (!(ret == PackageManager.PERMISSION_GRANTED)){
                //퍼미션 허가 안된 경우
                return false;
            }
        }
        //모든 퍼미션이 허가된 경우
        return true;
    }
    private void requestNecessaryPermissions(String[] permissions) {
        //마시멜로( API 23 )이상에서 런타임 퍼미션(Runtime Permission) 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        switch(permsRequestCode){
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean writeAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!writeAccepted )
                        {
                            showDialogforPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
                            return;
                        }
                    }
                }
                break;
        }
    }
    private void showDialogforPermission(String msg) {
        final AlertDialog.Builder myDialog = new AlertDialog.Builder(  openCV.this);
        myDialog.setTitle("알림");
        myDialog.setMessage(msg);
        myDialog.setCancelable(false);
        myDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(PERMISSIONS, PERMISSION_REQUEST_CODE);
                }
            }
        });
        myDialog.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        myDialog.show();
    }
    @Override//뒤로가기버튼이눌렸을때
    public void onBackPressed() {
        if(ipath==null)
            Toast.makeText(this, "지정된 이미지가 없습니다!", Toast.LENGTH_SHORT).show();
        super.onBackPressed();
        finish();
    }
}