package com.example.term_project;

import android.Manifest; import android.app.Activity; import android.content.Intent; import android.content.pm.PackageManager; import android.content.pm.ResolveInfo; import android.graphics.Bitmap; import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.net.Uri; import android.os.Bundle; import android.os.Environment; import android.provider.MediaStore; import android.support.v7.app.AppCompatActivity; import android.view.View; import android.widget.Button; import android.widget.ImageView; import android.widget.Toast; import java.io.File; import java.io.IOException; import java.text.SimpleDateFormat; import java.util.Date; import java.util.List;
public class CameraActivity extends AppCompatActivity
{
    private Button btTakePicture;
    private ImageView ivPicture;
    private String imagePath;
        @Override protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_camera);
            btTakePicture = (Button) findViewById(R.id.btTakePicture);
            ivPicture = (ImageView) findViewById(R.id.ivPicture);
            btTakePicture.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v)
            { // Camera Application이 있으면
                if (isExistCameraApplication())
                {
                    // Camera Application을 실행한다.
                    Intent cameraApp = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    // 찍은 사진을 보관할 파일 객체를 만들어서 보낸다.
                    File picture = savePictureFile();
                    if (picture != null)
                    {
                        cameraApp.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(picture));
                        startActivityForResult(cameraApp, 10000);
                    }
                }
                else
                    {
                        Toast.makeText(CameraActivity.this, "카메라 앱을 설치하세요.", Toast.LENGTH_SHORT).show(); } } }); }
                        @Override protected void onActivityResult(int requestCode, int resultCode, Intent data)
                        { // 사진찍기 버튼을 누른 후 잘찍고 돌아왔다면
                        if (requestCode == 10000 && resultCode == RESULT_OK)
                        { // 사진을 ImageView에 보여준다.
                            BitmapFactory.Options factory = new BitmapFactory.Options();
                            factory.inJustDecodeBounds = true;
                            BitmapFactory.decodeFile(imagePath);
                            factory.inJustDecodeBounds = false;
                            factory.inPurgeable = true; Bitmap bitmap = BitmapFactory.decodeFile(imagePath, factory);
                            ivPicture.setImageBitmap(bitmap);
                        }
            } /** * Android에 Camera Application이 설치되어있는지 확인한다. * * @return 카메라 앱이 있으면 true, 없으면 false */
            private boolean isExistCameraApplication()
            {
                // Android의 모든 Application을 얻어온다.
                PackageManager packageManager = getPackageManager();
                //Camera Application
                Intent cameraApp = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // MediaStore.ACTION_IMAGE_CAPTURE을 처리할 수 있는 App 정보를 가져온다.
                List cameraApps = packageManager.queryIntentActivities( cameraApp, PackageManager.MATCH_DEFAULT_ONLY);
                // 카메라 App이 적어도 한개 이상 있는지 리턴
                return cameraApps.size() > 0;
            } /** * 카메라에서 찍은 사진을 외부 저장소에 저장한다. * * @return */
                private File savePictureFile()
                { // 외부 저장소 쓰기 권한을 얻어온다.
                    PermissionRequest.Builder requester = new PermissionRequest.Builder(this);
                    int result = requester .create() .request( Manifest.permission.WRITE_EXTERNAL_STORAGE, 20000, new PermissionRequest.OnClickDenyButtonListener()
                    {
                        @Override public void onClick(Activity activity) { } }); // 사용자가 권한을 수락한 경우
                        if (result == PermissionRequest.ALREADY_GRANTED || result == PermissionRequest.REQUEST_PERMISSION)
                        {
                            // 사진 파일의 이름을 만든다. // Date는 java.util 을 Import 한다.
                            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss") .format(new Date());
                            String fileName = "IMG_" + timestamp; /** * 사진파일이 저장될 장소를 구한다. * 외장메모리에서 사진을 저장하는 폴더를 찾아서 * 그곳에 MYAPP 이라는 폴더를 만든다. */
                            File pictureStorage = new File( Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES), "MYAPP/");
                            // 만약 장소가 존재하지 않는다면 폴더를 새롭게 만든다.
                            if (!pictureStorage.exists())
                            {
                                /** * mkdir은 폴더를 하나만 만들고, * mkdirs는 경로상에 존재하는 모든 폴더를 만들어준다. */
                                pictureStorage.mkdirs();
                            }
                            try
                            {
                                File file = File.createTempFile(fileName, ".jpg", pictureStorage);
                                // ImageView에 보여주기위해 사진파일의 절대 경로를 얻어온다.
                                imagePath = file.getAbsolutePath(); // 찍힌 사진을 "갤러리" 앱에 추가한다.
                                Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE );
                                File f = new File( imagePath ); Uri contentUri = Uri.fromFile( f );
                                mediaScanIntent.setData( contentUri );
                                this.sendBroadcast( mediaScanIntent );
                                return file;
                            }
                            catch (IOException e) { e.printStackTrace(); }
                        } // 사용자가 권한을 거부한 경우
                        else { }
                        return null;
                }
}
