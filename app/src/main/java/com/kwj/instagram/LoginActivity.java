package com.kwj.instagram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    EditText email, password;// 이메일 , 비번
    Button login;// 로그인
    TextView txt_signup;// 회원가입
    FirebaseAuth auth;//인증을 위해 인스턴스 선언



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        txt_signup = findViewById(R.id.txt_signup);

        auth = FirebaseAuth.getInstance();//초기화

        txt_signup.setOnClickListener(new View.OnClickListener() {//회원가입 화면으로 화면전환
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        login.setOnClickListener(new View.OnClickListener() {//로그인 처리
            @Override
            public void onClick(View v) {//로딩 띄우기
                ProgressDialog pd = new ProgressDialog(LoginActivity.this);
                pd.setMessage("Please wait..");
                pd.show();

                String str_email = email.getText().toString();
                String str_password = password.getText().toString();

                if (TextUtils.isEmpty(str_email) || TextUtils.isEmpty(str_password)){
                    Toast.makeText(LoginActivity.this, "모두 작성해 주세요!", Toast.LENGTH_SHORT).show();
                } else {
                    auth.signInWithEmailAndPassword(str_email, str_password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){
                                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users")
                                                .child(auth.getCurrentUser().getUid());

                                        reference.addValueEventListener(new ValueEventListener() {//데이터베이스 정보 불러오기
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                //로그인 성공, 로그인한 사용자의 UI로 업뎃
                                                pd.dismiss();
                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                //Intent.FLAG_ACTIVITY_CLEAR_TASK // 실행 액티비티 외 모두 제거
                                                startActivity(intent);
                                                finish();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                pd.dismiss();
                                            }
                                        });
                                    } else {
                                        //로그인에 실패하면 사용자에세 메시지 표시
                                        pd.dismiss();
                                        Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }
}