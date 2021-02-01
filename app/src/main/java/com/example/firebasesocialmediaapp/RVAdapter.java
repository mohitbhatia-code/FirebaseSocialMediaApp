package com.example.firebasesocialmediaapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class RVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<Post> mPosts;
    private ArrayList<String> uids;
    private String senderUID;

    public RVAdapter(Context context, ArrayList<Post> posts, ArrayList<String> uids, String senderUID) {
        mContext = context;
        mPosts = posts;
        this.uids = uids;
        this.senderUID = senderUID;
    }

    public RVAdapter(Context context, ArrayList<Post> posts) {
        mContext = context;
        mPosts = posts;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View postView=
                LayoutInflater.from(mContext).inflate(R.layout.rv_row,parent,false);
        return new PostViewHolder(postView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        new PostViewHolder(holder.itemView).initializeUIComponents(mPosts.get(position).getpDesc(),mPosts.get(position).getpImage());
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(mContext);
                builder.setTitle("Delete the post!");
                builder.setMessage("Do you really want to delete the post?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final ProgressDialog progressDialog=new ProgressDialog(mContext);
                        progressDialog.setMessage("Deleting the post!");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        FirebaseDatabase.getInstance().getReference().child("my_users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("receivedPosts").child(senderUID).child(uids.get(position)).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                mPosts.remove(position);
                                notifyDataSetChanged();
                                progressDialog.dismiss();
                                uids.remove(position);
                                Toast.makeText(mContext,"Post is deleted!",Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
               return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }
    class PostViewHolder extends RecyclerView.ViewHolder{

        private TextView pDescTextView=itemView.findViewById(R.id.rv_row_tv);
        private ImageView pImageView=itemView.findViewById(R.id.rv_row_iv);

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
        }
        private void initializeUIComponents(String desc,String imageLink){
            pDescTextView.setText(desc);

        }

    }
}
