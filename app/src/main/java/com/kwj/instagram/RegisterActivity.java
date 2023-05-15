package com.kwj.instagram;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    EditText username, fullname, email, password;//이름 , 성 이름 , 이메일, 비밀번호
    Button register;// 등록 버튼
    TextView txt_login;// 이미 계정이 있으면 로그인 화면으로

    FirebaseAuth auth;//인증을 위해 인스턴스 선언
    DatabaseReference reference;//Database 접근을 위해
    ProgressDialog pd;//처리중 화면을 띄우기 위해


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        fullname = findViewById(R.id.fullname);
        password = findViewById(R.id.password);
        register = findViewById(R.id.register);
        txt_login = findViewById(R.id.txt_login);

        auth = FirebaseAuth.getInstance();//초기화

        txt_login.setOnClickListener(new View.OnClickListener() {// 로그인 화면으로 가기
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

        register.setOnClickListener(new View.OnClickListener() {// 등록버튼 이벤트 처리
            @Override
            public void onClick(View view) {// 로딩 띄우기
                pd = new ProgressDialog(RegisterActivity.this);
                pd.setMessage("Please wait...");
                pd.show();

                String str_username = username.getText().toString();
                String str_fullname = fullname.getText().toString();
                String str_email = email.getText().toString();
                String str_password = password.getText().toString();

                if (TextUtils.isEmpty(str_username) || TextUtils.isEmpty(str_fullname) || TextUtils.isEmpty(str_email) || TextUtils.isEmpty(str_password)){
                    Toast.makeText(RegisterActivity.this, "모든 양식을 채워주세요!", Toast.LENGTH_SHORT).show();
                } else if (str_password.length() < 6){
                    Toast.makeText(RegisterActivity.this, "비밀번호는 최소 6자리 이상으로 해주세요!", Toast.LENGTH_SHORT).show();
                } else {
                    register(str_username, str_fullname, str_email, str_password);
                }
            }
        });
    }

    public void register(final String username, final String fullname, String email, String password){
        auth.createUserWithEmailAndPassword(email, password)// 회원가입 Firebase 유효성 검사
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            FirebaseUser firebaseUser = auth.getCurrentUser();//아이디 정보를 변수에 담기
                            String userID = firebaseUser.getUid();

                            //reference 초기화
                            reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
                            //chuld Users 만들고 userID
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("id", userID);
                            map.put("username", username.toLowerCase());
                            map.put("fullname", fullname);
                            map.put("imageurl", "https://firebasestorage.googleapis.com/v0/b/instagram-bf5f6.appspot.com/o/placeholder.png?alt=media&token=4ddbd7e5-b708-4380-92f4-cf2dac2760f8");
                            //imageurl  firebase database 에 직접 접근해서 이미지 파일 하나 업로드 해서 token 으로 받는 주소
                            map.put("bio", "");

                            reference.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        pd.dismiss();
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        //Intent.FLAG_ACTIVITY_CLEAR_TASK // 실행 액티비티 외 모두 제거
                                        startActivity(intent);
                                    }
                                }
                            });
                        } else {
                            pd.dismiss();
                            Toast.makeText(RegisterActivity.this, "이 이메일이나 비밀번호로 등록할 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
