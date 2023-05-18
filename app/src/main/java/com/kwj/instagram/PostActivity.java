package com.kwj.instagram;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Credentials;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    Uri imageUri;
    String myUrl = "";
    UploadTask uploadTask;
    StorageReference storageReference;//파일 업로드 위한

    ImageView close, image_added;
    TextView post;
    EditText description;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        close = findViewById(R.id.close);
        image_added = findViewById(R.id.image_added);
        post = findViewById(R.id.post);
        description = findViewById(R.id.description);

        storageReference = FirebaseStorage.getInstance().getReference("posts");//FirebaseStorage 참조

        close.setOnClickListener(new View.OnClickListener() {//close 클릭시 메인으로 넘어감
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostActivity.this, MainActivity.class));
                finish();
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {// uploadImage() 메서드 실행
                uploadImage();
            }
        });

//        CropImage.activity() //CropImage 엑티비티 실행
//                .setAspectRatio(1, 1)
//                .start(PostActivity.this);
    }

    private String getFileExtension(Uri uri){//파일 확장자 찾기
        android.webkit.MimeTypeMap.getSingleton();
        String extension = MimeTypeMap.getFileExtensionFromUrl(String.valueOf(uri));
        return extension;
    }

    private void  uploadImage(){//업로드 실행중 보여질 다이얼로그 생성
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Posting");
        progressDialog.show();

        if (imageUri != null){//이미지 선택시 스토리지에 저장
            StorageReference filereference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));//시스템 시간, 파일타입

            uploadTask = filereference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {//이미지 다운로드 url 받아오기
                @Override
                public Object then(@NonNull Task task) throws Exception {//예외처리
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }

                    return filereference.getDownloadUrl();//return 값으로 다운로드 url
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString(); //스토리지 업로드 주소 받기

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");//참조 생성

                        String postid = reference.push().getKey();//참조키 받기

                        HashMap<String, Object> hashMap = new HashMap<>();// 키 값 넣기
                        hashMap.put("postid", postid);
                        hashMap.put("postimage", myUrl);
                        hashMap.put("description", description.getText().toString());
                        hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());

                        reference.child(postid).setValue(hashMap);

                        progressDialog.dismiss();//다이얼로그 종료

                        startActivity(new Intent(PostActivity.this, MainActivity.class));//메인으로 넘김
                        finish();
                    } else {
                        Toast.makeText(PostActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {//이미지업로드 실패시 메시지 출력
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(this, "No ImageSelected!", Toast.LENGTH_SHORT).show();
        }



    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//            imageUri = result.getUri();
//            image_added.setImageURI(imageUri);
//        } else {
//            Toast.makeText(this, "Something gone wrong!", Toast.LENGTH_SHORT).show();
//            startActivity(new Intent(PostActivity.this, MainActivity.class));
//            finish();
//        }
//    }
}