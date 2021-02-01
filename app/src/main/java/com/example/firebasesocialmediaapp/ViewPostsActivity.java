package com.example.firebasesocialmediaapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ViewPostsActivity extends AppCompatActivity {
    private FirebaseAuth mFirebaseAuth;
    private ListView mListView;
    private ArrayList<String> mArrayList;
    private ArrayAdapter mArrayAdapter;
    private ArrayList<String> uids;
    private TextView tvEmpty;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_posts);

        mFirebaseAuth=FirebaseAuth.getInstance();
        mArrayList=new ArrayList<>();
        mListView=findViewById(R.id.lvUsersNames);
        mArrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,mArrayList);
        mListView.setAdapter(mArrayAdapter);
        uids=new ArrayList();
        tvEmpty=findViewById(R.id.tvEmpty);
        tvEmpty.setVisibility(View.VISIBLE);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ViewPostsActivity.this,ViewPosts.class);
                intent.putExtra("uid",uids.get(position));
                startActivity(intent);
            }
        });

        FirebaseDatabase.getInstance().getReference().child("my_users").child(mFirebaseAuth.getCurrentUser().getUid()).child("receivedPosts").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    tvEmpty.setVisibility(View.INVISIBLE);
                    String username = dataSnapshot.child("fromwhom").getValue().toString();
                    uids.add(dataSnapshot.getKey());
                    mArrayList.add(username);
                    mArrayAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String uid=dataSnapshot.getKey();
                int index=0;
                for(int i=0;i<uids.size();i++){
                    if (uids.get(i).equals(uid)){
                        index=i;
                        break;
                    }
                }
                uids.remove(uid);
                mArrayList.remove(index);
                mArrayAdapter.notifyDataSetChanged();
                if (mArrayList.size()==0){
                    noPosts();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void noPosts(){
        tvEmpty.setVisibility(View.VISIBLE);
    }

}
