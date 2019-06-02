package com.example.term_project;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UploadActivity extends Activity {
    final String TAG = "ASDF";
    ImageView imageView;
    EditText txtTag;
    ImageButton buttonUpload;

    FirebaseFirestore db;
    StorageReference storageRef;
    FirebaseStorage storage;
    private DatabaseReference mDatabase;

    Uri dataUri;
    InputStream in;
    Bitmap bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        Intent myIntent = this.getIntent();
        dataUri = (Uri) myIntent.getExtras().get("image_uri");
        try {
            in = getContentResolver().openInputStream(dataUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance("gs://term-project-73bb1.appspot.com");
        storageRef = storage.getReference();

        imageView = findViewById(R.id.image_upload);
        txtTag = findViewById(R.id.txt_tag);
        txtTag.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtTag.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return false;
            }
        });
        buttonUpload = findViewById(R.id.button_upload);
        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload();
            }
        });

        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), dataUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(bitmap);
    }

    private void upload(){

        String[] tagList;
        final String tagString;
        final String[] imageId = new String[1];
        int tagNum;
        DocumentReference uploadedDoc;
        Map<String, Object> image = new HashMap<>();

        tagString = txtTag.getText().toString();
        tagNum = countChar(tagString, ',');
        tagList = tagString.split(",");

        int index = 0;
        for (String eachTag : tagList) {
            eachTag = eachTag.trim();
            tagList[index] = eachTag;
            index++;
        }

        image.put("path", ".jpg");
        image.put("tag", tagList);

        Date currentDate = new Date();
        String currentDateMillis = String.valueOf(currentDate.getTime());
        final String docId = currentDateMillis + UUID.randomUUID().toString();

        DocumentReference docRef = db.collection("images").document(docId);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                addNewImageData(docId, tagString);
                uploadNewImageFile(docId);
                finish();
            }
        });
    }

    private void addNewImageData(String docId, String tagString) {
        ArrayList<String> tagList = new ArrayList<>(Arrays.asList(tagString.split(",")));
        ImageItem item = new ImageItem(docId+".jpg", tagList);

        db.collection("images").document(docId).set(item);
    }

    private void uploadNewImageFile(String docId) {
        StorageReference imageRef = storageRef.child("images/"+docId+".jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
        byte[] data = baos.toByteArray();

        try{
            UploadTask task = imageRef.putBytes(data);
            task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getApplicationContext(), "Upload 성공", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "Upload 실패", Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d("ASDASD", "QWEQWEWQE");
    }

    private int countChar(String str, char c)
    {
        int count = 0;

        for(int i=0; i < str.length(); i++)
        {    if(str.charAt(i) == c)
            count++;
        }

        return count;
    }
}
