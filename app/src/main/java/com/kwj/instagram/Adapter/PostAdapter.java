package com.kwj.instagram.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kwj.instagram.CommentsActivity;
import com.kwj.instagram.Model.Post;
import com.kwj.instagram.Model.User;
import com.kwj.instagram.R;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder>{//Adapter 상속

    public Context mContext;
    public List<Post> mPost;

    private FirebaseUser firebaseUser;

    public PostAdapter(Context mContext, List<Post> mPost) {
        this.mContext = mContext;
        this.mPost = mPost;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {//리소스 파일과 연결
        // ViewHolder 객체를 생성하고 뷰를 연결하는 부분
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);
        return new PostAdapter.ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {//아이템 정보 담기 (onBindViewHolder)
        // 뷰홀더에 데이터를 바인딩하는 부분
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();// firebaseUser 인스턴스 정의
        Post post = mPost.get(position);// Model Post 생성
        // Glide 라이브러리를 사용하여 이미지 로드
        Glide.with(mContext).load(post.getPostimage()).into(holder.post_image);

        // 게시물의 설명 텍스트를 표시하거나 숨김
        if (post.getDescription().equals("")){
            holder.description.setVisibility(View.GONE);
        } else {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(post.getDescription());
        }

        // 게시물 작성자의 정보 표시
        publisherInfo(holder.image_profile, holder.username, holder.publisher, post.getPublisher());
        // 좋아요 상태 표시 및 클릭 이벤트 처리
        isLiked(post.getPostid(), holder.like);//출력값, 입력값
        // 좋아요 개수 표시
        nrLikes(holder.likes, post.getPostid());//입력값, 출력값

        holder.like.setOnClickListener(new View.OnClickListener() {//좋아요 이미지 클릭시
            @Override
            public void onClick(View v) {
                if (holder.like.getTag().equals("like")){// 이미지 태그 값이 "like"인 경우, 좋아요를 누른 상태이므로 좋아요를 해제
                    FirebaseDatabase.getInstance().getReference().child("Likes")//db에 likes path
                            .child(post.getPostid())//포스팅 id
                            .child(firebaseUser.getUid()).setValue(true);//사용자 본인 생성
                } else {
                    // 이미지 태그 값이 "like"가 아닌 경우, 좋아요를 누르지 않은 상태이므로 좋아요를 추가
                    FirebaseDatabase.getInstance().getReference().child("Likes")//db에 likes path
                            .child(post.getPostid())//포스팅 id
                            .child(firebaseUser.getUid()).removeValue();//사용자 본인 삭제(좋아요 해제)
                }
            }
        });

        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("postid", post.getPostid());
                intent.putExtra("publisherid", post.getPublisher());
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {// 아이템의 개수를 반환
        return mPost.size();//getItemCount 에 아이템 사이즈 return
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView image_profile, post_image, like, comment, save;
        public TextView username, likes, publisher, description, comments;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // 뷰들을 ViewHolder와 연결
            image_profile = itemView.findViewById(R.id.image_profile);
            post_image = itemView.findViewById(R.id.post_image);
            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
            save = itemView.findViewById(R.id.save);
            username = itemView.findViewById(R.id.username);
            likes = itemView.findViewById(R.id.likes);
            publisher = itemView.findViewById(R.id.publisher);
            description = itemView.findViewById(R.id.description);
            comments = itemView.findViewById(R.id.comments);


        }
    }

    private void isLiked(String postid, ImageView imageView){//좋아요 이미지 변경

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();//파베 유저 인스턴스

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()//db 참조
                .child("Likes")//Likes path
                .child(postid);//Lokes 하위에 포스팅 id

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // 사용자가 이미 좋아요를 눌렀는지 확인하고 이미지를 변경
                if(snapshot.child(firebaseUser.getUid()).exists()){//포스팅 id path에 사용자 정보 있으면
                    imageView.setImageResource(R.drawable.ic_liked);//이미지 변환
                    imageView.setTag("liked");
                } else {
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("like");
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void nrLikes(TextView likes, String postid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Likes").child(postid);//Likes 하위에 포스팅 아이디
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {//counting 될 TextView 에 갯수 세어 String 출력
                // 좋아요 개수를 가져와 TextView에 표시
                likes.setText(snapshot.getChildrenCount() + "likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getCommetns(String postId, final TextView comments){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Comments").child(postId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                comments.setText("View All "+dataSnapshot.getChildrenCount()+" Comments");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //포스트작성자 정보 셋팅 (publisherInfo(출력1,출력2,출력3,입력1))
    private void publisherInfo (ImageView image_profile, TextView username, TextView publisher, String userid){
        // 입력된 userid 정보의 데이터베이스 참조
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // 게시물 작성자의 프로필 이미지와 사용자명을 표시
                User user = snapshot.getValue(User.class);// Model User에서 값(value) 받아오기
                Glide.with(mContext).load(user.getImageurl()).into(image_profile);// 프로필 사진 셋팅
                username.setText(user.getUsername());// username 셋팅
                publisher.setText(user.getUsername());//포스팅작성자 셋팅
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
