package com.kwj.instagram.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kwj.instagram.Adapter.PostAdapter;
import com.kwj.instagram.Model.Post;
import com.kwj.instagram.R;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postLists;
    private List<String> followingList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 레이아웃 파일과 연결된 View를 생성합니다.
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // RecyclerView를 초기화하고 설정합니다.
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        // 게시물 목록을 담을 리스트와 어댑터를 초기화합니다.
        postLists = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postLists);
        recyclerView.setAdapter(postAdapter);

        // 팔로잉 목록을 확인하는 메서드를 호출합니다.
       checkFollowing();

        return view;
    }

    // 현재 사용자가 팔로잉하는 사용자 목록을 확인하는 메서드입니다.
    private void checkFollowing() {
        followingList = new ArrayList<>();//팔로우 리스트 생성

        //데이터베이스 참조 (Follow path / 유저명 / following)
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followingList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    //팔로우 리스트에 유저가 팔로우 하고 있는 대상자 모두 추가('키'로 받아옴)
                    followingList.add(dataSnapshot.getKey());
                }

                // 게시물을 읽어오는 메서드를 호출합니다.
                readPosts();//팔로우 대상자들의 포스트들 읽기
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // 홈 화면에 표시할 게시물을 읽어오는 메서드입니다.
    private void readPosts() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postLists.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    for (String id : followingList) {//id(String) 가 리스트 최대값까지 반복
                        if (post.getPublisher().equals(id)) { //id 가 publisher 와 같으면 포스트리스트에 post 추가
                            postLists.add(post);
                        }
                    }
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}