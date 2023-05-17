package com.kwj.instagram.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kwj.instagram.Fragment.ProfileFragment;
import com.kwj.instagram.Model.User;
import com.kwj.instagram.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends  RecyclerView.Adapter<UserAdapter.ViewHolder>{

    private Context mContext;
    private List<User> mUsers;

    private FirebaseUser firebaseUser;

    public  UserAdapter(Context mContext, List<User> mUsers){
        this.mContext = mContext;
        this.mUsers = mUsers;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent ,false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {//데이터를 바인딩

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();//firebaseUser 에 최근 유저 할당
        final User user = mUsers.get(position);//목록에 보여질 user / 객체화한 배열 mUsers에서 position 으로 인덱스 값 받아서 user 에 담는다.
        holder.btn_follow.setVisibility(View.VISIBLE);// follow 버튼 보이게

        holder.username.setText(user.getUsername());
        holder.fullname.setText(user.getFullname());
        Glide.with(mContext).load(user.getImageurl()).into(holder.image_profile);
        isFollowing(user.getId(), holder.btn_follow);


        if(user.getId().equals(firebaseUser.getUid())){
            holder.btn_follow.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
                editor.putString("profileid", user.getId());
                editor.apply();

                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();

            }
        });

        holder.btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.btn_follow.getText().toString().equals("follow")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(user.getId()).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId())
                            .child("followers").child(firebaseUser.getUid()).setValue(true);
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(user.getId()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId())
                            .child("followers").child(firebaseUser.getUid()).removeValue();
                }
            }
        });


    }

    @Override
    public int getItemCount() {//유저 List 의 크기를 size() 로 return

        return mUsers.size();
    }

    public  class  ViewHolder extends RecyclerView.ViewHolder{// view holder 로 사용자 정보 입력

        public TextView username;
        public TextView fullname;
        public CircleImageView image_profile;
        public Button btn_follow;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            fullname = itemView.findViewById(R.id.fullname);
            image_profile = itemView.findViewById(R.id.image_profile);
            btn_follow = itemView.findViewById(R.id.btn_follow);
        }
    }

    private  void isFollowing(String userid, Button button){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(userid).exists()){
                    button.setText("following");
                } else {
                    button.setText("follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}