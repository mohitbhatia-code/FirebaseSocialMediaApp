package com.example.firebasesocialmediaapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class SocialMediaActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private ImageView ivPlaceHolder;
    private Button btnUpload;
    private Bitmap mBitmap;
    private EditText edtDesc;
    private ListView lvUserNames;
    private ArrayList<String> aLUserNames;
    private ArrayAdapter mArrayAdapter;
    private String imageIdentifier;
    private ArrayList<String> uids;
    private String imageDownloadLink;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_media);

        mAuth=FirebaseAuth.getInstance();

        ivPlaceHolder=findViewById(R.id.ivPlaceHolder);
        btnUpload=findViewById(R.id.btnPostImage);
        edtDesc=findViewById(R.id.edtDescription);
        lvUserNames=findViewById(R.id.lvUsers);
        aLUserNames=new ArrayList();
        mArrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,aLUserNames);
        lvUserNames.setAdapter(mArrayAdapter);
        uids=new ArrayList<>();

        TooltipCompat.setTooltipText(ivPlaceHolder,"Add an Image");

        ivPlaceHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtDesc.setVisibility(View.INVISIBLE);
                selectImage();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBitmap == null) {
                    Toast.makeText(SocialMediaActivity.this, "Please select an image!", Toast.LENGTH_SHORT).show();
                } else {
                    aLUserNames.clear();
                    uploadImageToServer();
                }
            }
        });
        lvUserNames.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FirebaseDatabase.getInstance().getReference().child("my_users").child(uids.get(position)).child("receivedPosts").child(mAuth.getCurrentUser().getUid()).child("fromwhom").setValue(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                HashMap<String,String> dataMap=new HashMap();
                dataMap.put("imageIdentifier",imageIdentifier);
                dataMap.put("imageDownloadLink",imageDownloadLink);
                dataMap.put("description",edtDesc.getText().toString());
                FirebaseDatabase.getInstance().getReference().child("my_users").child(uids.get(position)).child("receivedPosts").child(mAuth.getCurrentUser().getUid()).push().setValue(dataMap);
               }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logOutItem:
                mAuth.signOut();
                Intent intent=new Intent(this,MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.viewPostsItem:
                Intent intent1= new Intent(this,ViewPostsActivity.class);
                startActivity(intent1);
                break;

        }
        return super.onOptionsItemSelected(item);
    }


    private void selectImage(){
        if (Build.VERSION.SDK_INT<23){
            Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,1000);
        }else if (Build.VERSION.SDK_INT>=23){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},2000);
            }else{
                Intent intent=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,1000);
            }
        }
    }
    private void uploadImageToServer() {

        final ProgressDialog mProgressDialog = new ProgressDialog(SocialMediaActivity.this);
        mProgressDialog.setTitle("Photo is uploading!");
        mProgressDialog.setMessage("Please Wait...");
        mProgressDialog.show();
        mProgressDialog.setCancelable(false);

        // Get the data from an ImageView as bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        imageIdentifier = UUID.randomUUID() + ".png";
        UploadTask uploadTask = FirebaseStorage.getInstance().getReference().child("my_images").child(imageIdentifier).putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                mProgressDialog.dismiss();
                Toast.makeText(SocialMediaActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mProgressDialog.dismiss();
                Toast.makeText(SocialMediaActivity.this, "Photo is uploaded", Toast.LENGTH_SHORT).show();
                edtDesc.setVisibility(View.VISIBLE);
                aLUserNames.clear();
                FirebaseDatabase.getInstance().getReference().child("my_users").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        String username = dataSnapshot.child("username").getValue().toString();
                        if (!mAuth.getCurrentUser().getUid().equals(dataSnapshot.getKey())) {
                            aLUserNames.add(username);
                            mArrayAdapter.notifyDataSetChanged();
                            uids.add(dataSnapshot.getKey());
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        imageDownloadLink=task.getResult().toString();
                    }
                });
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==2000 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            selectImage();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1000 && resultCode== RESULT_OK && data!=null){
            Uri chosenImageData=data.getData();
            try{
                mBitmap=MediaStore.Images.Media.getBitmap(this.getContentResolver(),chosenImageData);
                ivPlaceHolder.setImageBitmap(mBitmap);
            }catch (Exception ex){
                Toast.makeText(this,ex.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }

}
