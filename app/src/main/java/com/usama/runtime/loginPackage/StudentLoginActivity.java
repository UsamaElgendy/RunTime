package com.usama.runtime.loginPackage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rey.material.widget.CheckBox;
import com.usama.runtime.Prevalent.Prevalent;
import com.usama.runtime.R;
import com.usama.runtime.makeNavigationBar.HomeActivity;
import com.usama.runtime.model.Student;

import io.paperdb.Paper;

public class StudentLoginActivity extends AppCompatActivity {
    public static Student studentData;
    private EditText login_nationalId, login_sitting_number;
    private Button LoginButton;
    private ProgressDialog loadingBar;
    private CheckBox chkBoxRememberMe;

    // variable to use in shared preference
    public static final String MT_NATIONAL_ID = "MyNationalId";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_login);

        LoginButton = findViewById(R.id.login_btn);
        login_nationalId = findViewById(R.id.login_nationalId);
        login_sitting_number = findViewById(R.id.login_sitting_number);

        loadingBar = new ProgressDialog(this);
        chkBoxRememberMe = findViewById(R.id.remember_me_chkb);
        // paper library
        Paper.init(this);

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginStudent();
            }
        });

        String StudentNationalIdKey = Paper.book().read(Prevalent.StudentNationalIdKey);
        String StudentSittingNumberKey = Paper.book().read(Prevalent.StudentSittingNumberKey);

        if (StudentNationalIdKey != "" && StudentSittingNumberKey != "") {
            if (!TextUtils.isEmpty(StudentNationalIdKey) && !TextUtils.isEmpty(StudentSittingNumberKey)) {
                AllowAccess(StudentNationalIdKey, StudentSittingNumberKey);

                loadingBar.setTitle("Already Login");
                loadingBar.setMessage("please wait .. ");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

            }
        }


    }

    private void AllowAccess(final String studentNationalIdKey, final String studentSittingNumberKey) {
        final DatabaseReference RootRef;

        RootRef = FirebaseDatabase.getInstance().getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // here child phone is a unique object
                if (dataSnapshot.child("students").child(studentNationalIdKey).exists()) {
                    Student usersData = dataSnapshot.child("students").child(studentNationalIdKey).getValue(Student.class);

                    // retrieve the user data
                    if (usersData.getNational_id().equals(studentNationalIdKey)) {
                        if (usersData.getNt_ID().equals(studentSittingNumberKey)) {
                            Toast.makeText(StudentLoginActivity.this, "you are already login  ... ", Toast.LENGTH_LONG).show();
                            loadingBar.dismiss();

                            Intent intent = new Intent(StudentLoginActivity.this, HomeActivity.class);


                            // this line to make sure the app doesn't crash when cloth it and open again
                            // because we use paper library
                            Prevalent.CurrentOnlineStudent = usersData;
                            startActivity(intent);
                        } else {
                            loadingBar.dismiss();
                            Toast.makeText(StudentLoginActivity.this, "Password is incorrect ", Toast.LENGTH_SHORT).show();
                        }
                    }

                } else {
                    Toast.makeText(StudentLoginActivity.this, "Account with this " + studentNationalIdKey + " number do not exist ", Toast.LENGTH_LONG).show();
                    loadingBar.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void LoginStudent() {
        String nationalID = login_nationalId.getText().toString();
        String sittingNumber = login_sitting_number.getText().toString();

        if (TextUtils.isEmpty(nationalID)) {
            Toast.makeText(this, "Please write your National ID....", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(sittingNumber)) {
            Toast.makeText(this, "Please write your sitting Number....", Toast.LENGTH_SHORT).show();
        } else {
            // wait to check is phone number is available in database
            loadingBar.setTitle("Login Account");
            loadingBar.setMessage("please wait , while we are checking the credentials .");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            AllowAccessToAccount(nationalID, sittingNumber);

        }
    }

    private void AllowAccessToAccount(final String national_id, final String nt_ID) {
        if (chkBoxRememberMe.isChecked()) {
            // check box return two values true or false :)
            // method write take two parameter is key and value
            Paper.book().write(Prevalent.StudentNationalIdKey, national_id);
            Paper.book().write(Prevalent.StudentSittingNumberKey, nt_ID);
        }


        // make database by a Reference
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();


        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("students").child(national_id).exists()) {


                    studentData = dataSnapshot.child("students").child(national_id).getValue(Student.class);
                    Log.d("TAG", "Data" + studentData);
                    if (studentData.getNational_id().equals(national_id)) {
                        if (studentData.getNt_ID().equals(nt_ID)) {

                            Toast.makeText(StudentLoginActivity.this, "logged in Successfully...", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();

                            Intent intent = new Intent(StudentLoginActivity.this, HomeActivity.class);


                            // MY_PREFS_NAME - a static String variable like:
                            //public static final String MY_PREFS_NAME = "MyPrefsFile";
                            SharedPreferences.Editor editor = getSharedPreferences(MT_NATIONAL_ID, MODE_PRIVATE).edit();
                            editor.putString("nationalId", national_id);
                            editor.apply();

                            //make this to make the user data public in all classes to use it
                            Prevalent.CurrentOnlineStudent = studentData;
                            startActivity(intent);
                        } else {
                            loadingBar.dismiss();
                            Toast.makeText(StudentLoginActivity.this, "Password is incorrect.", Toast.LENGTH_SHORT).show();
                        }
                    }

                } else {
                    Toast.makeText(StudentLoginActivity.this, "Account with this " + national_id + " number do not exists.", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}