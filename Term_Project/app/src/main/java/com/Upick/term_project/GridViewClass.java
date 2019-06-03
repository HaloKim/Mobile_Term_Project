package com.Upick.term_project;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class GridViewClass extends Activity{
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private MyAdapter mAdapter;
    private ImageButton mBtnOpenAlbum;
    private EditText txtSearch;
    private static ArrayList<item> itemArrayList;

    boolean stopLoading = false;

    FirebaseFirestore db;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gridview);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance("gs://term-project-73bb1.appspot.com");
        itemArrayList = new ArrayList<>();
        mAdapter = new MyAdapter(itemArrayList);

        mBtnOpenAlbum = findViewById(R.id.button_album);
        mBtnOpenAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopLoading = true;
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 1);
            }
        });

        txtSearch = findViewById(R.id.txt_search);
        txtSearch.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        txtSearch.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                getListFromFirebase(txtSearch.getText().toString());
                return true;
            }
        });

        mRecyclerView = findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);//옵션

        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);


        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
                Log.d("ASD", "CLICK");

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    View child = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());

                    item currentItem =  ((MyAdapter)recyclerView.getAdapter()).getItem(recyclerView.getChildViewHolder(child).getLayoutPosition());
                    Bitmap currentBitmap = currentItem.getPhoto();

                    Uri imageUri = getImageUri(getApplicationContext(), currentBitmap);

                    Intent intent = new Intent(getApplicationContext(), openCV.class);
                    intent.putExtra("fromOnline", 1);
                    intent.putExtra("itemUri", imageUri);

                    startActivityForResult(intent, 100);
                }

                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean b) {

            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        getListFromFirebase();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                try {
                    Uri uri = data.getData();

                    Intent intent = new Intent(this, UploadActivity.class);
                    intent.putExtra("image_uri", uri);
                    startActivityForResult(intent, 2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                stopLoading = false;
            }
        }else if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                finish();
            }
        }
    }

    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void getListFromFirebase(){
        final File[] localFile = new File[1];
        StorageReference storageRef = storage.getReference();
        final StorageReference pathReference = storageRef.child("images");
        final int[] totalSize = {0};
        final int[] downloadedSize = {0};
        itemArrayList.clear();
        final File storage = getApplicationContext().getCacheDir();

        db.collection("images")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                final File newFile = new File(storage, document.getId()+".jpg");
                                StorageReference fileRef = pathReference.child(document.getId()+".jpg");
                                fileRef.getFile(newFile)
                                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                Log.d("PATH", newFile.getAbsolutePath());
                                                BitmapFactory.Options options = new BitmapFactory.Options();
                                                options.inSampleSize = 4;
                                                itemArrayList.add(new item(BitmapFactory.decodeFile(newFile.getAbsolutePath(), options)));
                                                mAdapter.notifyDataSetChanged();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                mAdapter.notifyDataSetChanged();
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    private void getListFromFirebase(String tag){
        StorageReference storageRef = storage.getReference();
        final StorageReference pathReference = storageRef.child("images");
        final File storage = getApplicationContext().getCacheDir();

        itemArrayList.clear();

        db.collection("images")
                .whereArrayContains("tags", tag)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("ASDASDASD", "YES");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                final File newFile = new File(storage, document.getId()+".jpg");
                                StorageReference fileRef = pathReference.child(document.getId()+".jpg");
                                fileRef.getFile(newFile)
                                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                Log.d("PATH", newFile.getAbsolutePath());
                                                BitmapFactory.Options options = new BitmapFactory.Options();
                                                options.inSampleSize = 4;
                                                itemArrayList.add(new item(BitmapFactory.decodeFile(newFile.getAbsolutePath(), options)));
                                                mAdapter.notifyDataSetChanged();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                mAdapter.notifyDataSetChanged();
                                            }
                                        });
                            }
                        }
                    }
                });
        mAdapter = new MyAdapter(itemArrayList); //스트링 배열 데이터 인자로
        mRecyclerView.setAdapter(mAdapter);
    }



    private static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }
}
