package ssaha.hey;

import android.app.ProgressDialog;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;

import static android.R.color.darker_gray;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mProfielSendRequestBtn, mDeclineBtn;

    //Database Reference
    private DatabaseReference mUsersDatabases;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;


    //Firebase Auth
    private FirebaseUser mCurrentUser;

    private String mCurrent_state;

    //Progress Dialog

    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // user id of the users profile
        final String user_id = getIntent().getStringExtra("user_id");

        //Database
        mUsersDatabases = FirebaseDatabase.getInstance().getReference().child("User").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");



        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();


        mProfileImage = (ImageView) findViewById(R.id.profile_image);
        mProfileName = (TextView)findViewById(R.id.profile_displayName);
        mProfileStatus = (TextView)findViewById(R.id.profile_status);
        mProfileFriendsCount = (TextView)findViewById(R.id.profile_friends_count);
        mProfielSendRequestBtn = (Button)findViewById(R.id.profile_request_btn);
        mDeclineBtn = (Button)findViewById(R.id.profile_reject_btn);

        mDeclineBtn.setVisibility(View.INVISIBLE);
        mDeclineBtn.setEnabled(false);

        //Indication friend or not
        mCurrent_state = "not_friend";

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please Wait");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUsersDatabases.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String dispaly_name = dataSnapshot.child("name").getValue().toString();
                String dispaly_status = dataSnapshot.child("status").getValue().toString();
                String dispaly_image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(dispaly_name);
                mProfileStatus.setText(dispaly_status);
                Picasso.with(ProfileActivity.this).load(dispaly_image).placeholder(R.drawable.default_avatar).into(mProfileImage);

                /*----------------Friend Request / Accept ------------------*/
                mFriendReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_id)){
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if (req_type.equals("received")){
                                mCurrent_state = "req_received";
                                mProfielSendRequestBtn.setText("Accept Friend Request");
                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);
                            }else if(req_type.equals("sent")) {
                                mCurrent_state = "req_sent";
                                mProfielSendRequestBtn.setText("Cancel Friend Request");

                            }
                            mProgressDialog.dismiss();
                        }else{
                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)){
                                        mCurrent_state = "friends";
                                        mProfielSendRequestBtn.setText("Un-friend");


                                    }
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                            });
                        }



                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });





            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfielSendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {

                mProfielSendRequestBtn.setEnabled(false);
                mProfielSendRequestBtn.setBackgroundColor(getResources().getColor(darker_gray));
                mDeclineBtn.setVisibility(View.INVISIBLE);
                mDeclineBtn.setEnabled(false);

                /*----------------Not Friend------------------*/

                if (mCurrent_state.equals("not_friend")){
                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_id).child("request_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                mFriendReqDatabase.child(user_id).child(mCurrentUser.getUid()).child("request_type")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        HashMap<String , String > notificationData = new HashMap<String, String>();
                                        notificationData.put("form", mCurrentUser.getUid());
                                        notificationData.put("type", "request");

                                        mNotificationDatabase.child(user_id).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mProfielSendRequestBtn.setEnabled(true);
                                                mProfielSendRequestBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                                mCurrent_state = "req_sent";
                                                mProfielSendRequestBtn.setText("Cancel Friend Request");
                                            }
                                        });


                                        Toast.makeText(ProfileActivity.this, "Request Send", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }else{
                                Toast.makeText(ProfileActivity.this, "Failed to send Request", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                /*----------------Cancel Friend Req------------------*/
                if (mCurrent_state.equals("req_sent")){
                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfielSendRequestBtn.setEnabled(true);
                                    mProfielSendRequestBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                    mCurrent_state = "not_friend";
                                    mProfielSendRequestBtn.setText("Send Friend Request");
                                }
                            });
                        }
                    });

                }

                /*----------------Receive Friend Req/Accepting req------------------*/
                if (mCurrent_state.equals("req_received")){

                    final String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    mFriendDatabase.child(mCurrentUser.getUid()).child(user_id).setValue(currentDate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendDatabase.child(user_id).child(mCurrentUser.getUid()).setValue(currentDate)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            mFriendReqDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    mProfielSendRequestBtn.setEnabled(true);
                                                                    mProfielSendRequestBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                                                    mCurrent_state = "friends";
                                                                    mProfielSendRequestBtn.setText("Un-friend");
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                }
                            });
                }

                /*----------------Un-Friend Req------------------*/



            }
        });


    }
}
