package com.example.firebasesocialmediaapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ViewPosts extends AppCompatActivity {
    private ArrayList<Post> mPosts;
    private RVAdapter mRVAdapter;
    private RecyclerView mRecyclerView;
    private FirebaseAuth mAuth;
    private String senderUId;
    private ArrayList<String> uids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_posts2);

        senderUId=getIntent().getStringExtra("uid");
        mRecyclerView=findViewById(R.id.rv_posts);
        mPosts=new ArrayList();
        mAuth=FirebaseAuth.getInstance();
        uids=new ArrayList();
        mRVAdapter=new RVAdapter(ViewPosts.this,mPosts,uids,senderUId);
        FirebaseDatabase.getInstance().getReference().child("my_users").child(mAuth.getCurrentUser().getUid()).child("receivedPosts").child(senderUId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (!dataSnapshot.getKey().equals("fromwhom")) {
                    mPosts.add(new Post(dataSnapshot.child("description").getValue().toString(), dataSnapshot.child("imageDownloadLink").getValue().toString()));
                    uids.add(dataSnapshot.getKey());
                    mRVAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
               if (mPosts.size()==1){
                   FirebaseDatabase.getInstance().getReference().child("my_users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("receivedPosts").child(senderUId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                       @Override
                       public void onComplete(@NonNull Task<Void> task) {
                           Toast.makeText(ViewPosts.this,"All posts of selected user are deleted!",Toast.LENGTH_SHORT).show();
                           finish();
                       }
                   });
               }
            }


            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(ViewPosts.this);

        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mRVAdapter);



    }
}
