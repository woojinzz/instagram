package com.kwj.instagram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.kwj.instagram.Fragment.HomeFragment;
import com.kwj.instagram.Fragment.NotificationFragment;
import com.kwj.instagram.Fragment.ProfileFragment;
import com.kwj.instagram.Fragment.SearchFragment;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    Fragment selectedFragment  = null; //Fragment null 로 초기화

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        //   getSupportFragmentManager() 호출하여 FragmentManager 가져옴
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new HomeFragment()).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()){

                        case  R.id.nav_home://홈 화면

                            selectedFragment = new HomeFragment();

                            break;
                        case  R.id.nav_search://검색 화면

                            selectedFragment = new SearchFragment();

                            break;
                        case  R.id.nav_add://추가 화면

                            selectedFragment = null;
                            startActivity(new Intent(MainActivity.this, PostActivity.class));

                            break;
                        case  R.id.nav_heart://알림 화면

                            selectedFragment = new NotificationFragment();

                            break;
                        case  R.id.nav_profile://설정 화면

                            SharedPreferences.Editor editor = getSharedPreferences("PREFS",MODE_PRIVATE).edit();
                            editor.putString("profileid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                            editor.apply();
                            selectedFragment = new ProfileFragment();
                            break;

                    }

                    if(selectedFragment != null){

                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                selectedFragment).commit();

                    }
                    return true;

                }
            };

}