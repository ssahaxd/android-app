package ssaha.hey;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mProfielSendRequestBtn;

    //Database Reference
    private DatabaseReference mUsersDatabases;
    private DatabaseReference mFriendReqDatabase;

    private String mCurrent_state;

    //Progress Dialog

    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // user id of the users profile
        String user_id = getIntent().getStringExtra("user_id");

        mUsersDatabases = FirebaseDatabase.getInstance().getReference().child("User").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");


        mProfileImage = (ImageView) findViewById(R.id.profile_image);
        mProfileName = (TextView)findViewById(R.id.profile_displayName);
        mProfileStatus = (TextView)findViewById(R.id.profile_status);
        mProfileFriendsCount = (TextView)findViewById(R.id.profile_friends_count);
        mProfielSendRequestBtn = (Button)findViewById(R.id.profile_request_btn);

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
                mProgressDialog.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mPro


    }
}
