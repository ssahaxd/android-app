package ssaha.hey;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class ProfileDataActivity extends AppCompatActivity {
    private TextInputLayout mDisplayName;
    private Button mSubmitBtn;
    private ProgressDialog mRegProgress;

    //Firebase DataBase
    private DatabaseReference mDatabase;

    // Firebase Auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_data);


        mDisplayName = (TextInputLayout) findViewById(R.id.reg_display_name);
        mSubmitBtn = (Button) findViewById(R.id.reg_submit_btn);
        mRegProgress = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String display_name = mDisplayName.getEditText().getText().toString();

                if(!TextUtils.isEmpty(display_name)){
                    mRegProgress.setTitle("Saving Data");
                    mRegProgress.setMessage("Please wait ...");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                    store_db(display_name);
                }
            }
        });
    }

    private void store_db (final String display_name){
        FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = current_user.getUid();
        String deviceToken = FirebaseInstanceId.getInstance().getToken();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        HashMap<String, String> userMap= new HashMap<>();
        userMap.put("name", display_name);
        userMap.put("device_token", deviceToken);
        userMap.put("status", "Hi there I'm Using Buddys");
        userMap.put("image", "default");
        userMap.put("thumb_image", "default");

        mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    mRegProgress.dismiss();

                    Intent mainIntent = new Intent(ProfileDataActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                }
            }
        });

    }
}
