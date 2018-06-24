package com.medeveloper.ayaz.hostelutility;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.medeveloper.ayaz.hostelutility.Registration.Registration;
import com.medeveloper.ayaz.hostelutility.classes_and_adapters.MyData;
import com.medeveloper.ayaz.hostelutility.classes_and_adapters.OfficialsDetailsClass;
import com.medeveloper.ayaz.hostelutility.classes_and_adapters.StudentDetailsClass;
import com.medeveloper.ayaz.hostelutility.officials.OfficialsHome;
import com.medeveloper.ayaz.hostelutility.student.Home;
import com.medeveloper.ayaz.hostelutility.student.StudentForm;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import tyrantgit.explosionfield.ExplosionField;
import tyrantgit.explosionfield.Utils;

public class LoginAcitivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    EditText Email,Password;
    Button Login;
    TextView Register;
    SweetAlertDialog pDialog;
    private FirebaseAnalytics mFirebaseAnalytics;
    EditText employeeID;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_acitivity);
        initViews();
        ActionBar bar=getSupportActionBar();
        bar.hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        final RadioGroup loginAs=findViewById(R.id.signin_radio);
        RadioButton Official=findViewById(R.id.teacher_radio);
        Official.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                employeeID.setVisibility(View.VISIBLE);
                Email.setVisibility(View.VISIBLE);
                Password.setVisibility(View.VISIBLE);
                Login.setText("Login");
                Log.d("Ayaz","Official Login");
            }
        });
        RadioButton Student=findViewById(R.id.student_radio);
        Student.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*
                employeeID.animate().alpha(1.0f).setDuration(500);
                Email.animate().alpha(0.0f).setDuration(500);
                Password.animate().alpha(0.0f).setDuration(500);
                */
                employeeID.setVisibility(View.GONE);
                Email.setVisibility(View.VISIBLE);
                Password.setVisibility(View.VISIBLE);
                Login.setText("Login");
                Log.d("Ayaz","Student Login");
            }
        });

        RadioButton Guest=findViewById(R.id.guest_radio);
        Guest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*employeeID.animate().alpha(1.0f).setDuration(500);
                Email.animate().alpha(1.0f).setDuration(500);
                Password.animate().alpha(1.0f).setDuration(500);
                */
                employeeID.setVisibility(View.GONE);
                Email.setVisibility(View.GONE);
                Password.setVisibility(View.GONE);
                Login.setText("View as Guest");
                Log.d("Ayaz","Guest Login");
            }
        });


        mAuth= FirebaseAuth.getInstance();

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDialog.show();
                String email=Email.getText().toString();
                String pass=Password.getText().toString();
                int id=loginAs.getCheckedRadioButtonId();
               // tempLogIn(id);

               if (isOkay(email,pass,id))
                   authenticate(email,pass,id);//
                else pDialog.dismiss();




            }
        });
        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDialog.dismiss();
                startActivity(new Intent(LoginAcitivity.this,Registration.class));
            }
        });



    }

    private void initViews() {

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        employeeID=findViewById(R.id.employee_id);
        Email=findViewById(R.id.email_login);
        Password=findViewById(R.id.password_login);
        Login=findViewById(R.id.login_button);
        Register=findViewById(R.id.login_signup);
        TextView forgotPassword=findViewById(R.id.forgot_password);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initForgotPassword();
            }
        });
        //Progressbar
        pDialog=new SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE).setTitleText("Loging in").setContentText("Please wait while we log you in");

    }

    private void initForgotPassword() {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.forgot_pass_dialog, null);
        final SweetAlertDialog progressBar=new SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText("Please wait..");
        progressBar.setCancelable(true);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.forgot_password_email);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Send email",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                String email=userInput.getText().toString();
                                if(email.equals("")||(!email.contains("@"))||(!email.contains(".com")))
                                {
                                    userInput.setError("Invalid email");
                                }
                                else
                                {
                                    progressBar.show();
                                    sendResetEmail(email,progressBar);
                                    dialog.dismiss();
                                }

                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();

    }

    private void sendResetEmail(final String email, final SweetAlertDialog progressBar) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressBar.dismiss();
                            new SweetAlertDialog(LoginAcitivity.this,SweetAlertDialog.SUCCESS_TYPE)
                                    .setTitleText("Email sent")
                                    .setContentText("An email has been sent to your email id: "+email+"\n " +
                                            "Goto to your mailbox and re-authenticate yourself")
                                    .show();
                        }
                        else
                        {
                            progressBar.dismiss();
                            Toast.makeText(LoginAcitivity.this,"Cannot send the email \nError: "+task.getException(),Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    String TAG="OPENED_AS";
    private void tempLogIn(int id)
    {
        pDialog.dismiss();
        Bundle bundle=new Bundle();
        if(id == R.id.student_radio)
        {
            bundle.putString(TAG,"Student");
            mFirebaseAnalytics.logEvent("user_open_event",bundle);
            startActivity(new Intent(this,Home.class));
        }
        else if(id==R.id.teacher_radio)
        {
            bundle.putString(TAG,"Teacher");
            mFirebaseAnalytics.logEvent("user_open_event",bundle);
            startActivity(new Intent(this,OfficialsHome.class));
        }


    }
    private boolean isOkay(String email, String pass,int id)
    {


        Email.setError(null);
        Password.setError(null);
        boolean isOkay=true;
        if(id==R.id.student_radio) {
            Log.d("Ayaz","Student isOkay");
            if (email.equals("")) {
                Email.setError("Required Field");
                Email.requestFocus();
                isOkay = false;
                pDialog.dismiss();
            } else if (email.length() < 6 || !email.contains("@") || !email.contains(".com")) {
                pDialog.dismiss();
                Email.setError("Invalid Email");
                Email.requestFocus();
                isOkay = false;

            } else if (pass.equals("")) {
                pDialog.dismiss();
                Password.setError("Required Field");
                Password.requestFocus();
                isOkay = false;
            } else if (pass.length() < 6) {
                Password.setError("Password length must be at least 6 digit");
                Password.requestFocus();

                isOkay = false;
            }
        }
        //ID==Employee ID
        else if(id==R.id.teacher_radio)
        {

            Log.d("Ayaz","Employee isOkay");
            if(email.equals(""))
            {
                Email.setError("Required Field");
                Email.requestFocus();
                isOkay=false;
                pDialog.dismiss();
            }
            else if(email.length()<6||!email.contains("@")||!email.contains(".com"))
            {
                pDialog.dismiss();
                Email.setError("Invalid Email");
                Email.requestFocus();
                isOkay=false;

            }
            else if(pass.equals(""))
            {
                pDialog.dismiss();
                Password.setError("Required Field");
                Password.requestFocus();
                isOkay=false;
            }
            else if(pass.length()<6)
            {
                Password.setError("Password length must be at least 6 digit");
                Password.requestFocus();


                isOkay=false;
            }
            else if(pass.equals("123456")||pass.equals("000000"))
            {
                //TODO remove comment before launch
                /*
            Password.setError("Password must be complicated");
            Password.requestFocus();
            isOkay=false;
            */
            }
            else if(employeeID.getText().toString().equals(""))
            {
                employeeID.setError("Required");
                isOkay=false;
                employeeID.requestFocus();

            }
        }


        else if(id==R.id.guest_radio)
            {

                Log.d("Ayaz","Guest isOkay");
                isOkay=true;
            }


        return isOkay;
    }



    private void authenticate(final String email, final String pass, int id)
    {


        if(id==R.id.student_radio)
        {
           authenticateStudent(email,pass);
        }
        else if(id==R.id.teacher_radio)
        {
            authenticateOfficial(email,pass,employeeID.getText().toString());
        }
        else if(id==R.id.guest_radio)
        {
            pDialog.dismiss();
            new SweetAlertDialog(this,SweetAlertDialog.NORMAL_TYPE).
                    setTitleText("In progress").
                    setContentText("Guest login is in development process").show();
           //mAuth.signInAnonymously();
        }




    }



    private void authenticateStudent(final String email, final String pass)
    {
        mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    saveStudentPrefs();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),""+task.getException(),Toast.LENGTH_SHORT).show();

                    pDialog.dismiss();
                    final SweetAlertDialog sDialog=new SweetAlertDialog(LoginAcitivity.this,SweetAlertDialog.ERROR_TYPE);
                    sDialog.setCancelable(false);
                    sDialog.setTitleText("Can't log you in").setContentText(""+task.getException().getMessage()).
                            setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sDialog.dismiss();
                                }
                            });
                    sDialog.show();
                }
            }
        });
    }


    private void authenticateOfficial(final String email, final String pass, final String EmployeeID)
    {

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            FirebaseDatabase.getInstance().getReference(getString(R.string.college_id))
                                    .child(getString(R.string.officials_id_ref))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            if(dataSnapshot.exists())
                                            {
                                                OfficialsDetailsClass Id=dataSnapshot.getValue(OfficialsDetailsClass.class);
                                                if(Id.mEmployeeID.equals(EmployeeID))
                                                {
                                                    MyData prefs=new MyData(LoginAcitivity.this);
                                                    prefs.saveTeacherPrefs(Id);
                                                    pDialog.dismiss();
                                                    startActivity(new Intent(LoginAcitivity.this,OfficialsHome.class));
                                                    finish();
                                                }
                                                else
                                                {
                                                    new SweetAlertDialog(LoginAcitivity.this,SweetAlertDialog.ERROR_TYPE)
                                                        .setTitleText(getString(R.string.invalid_credentials))
                                                            .setContentText(getString(R.string.login_error_off))
                                                            .show();
                                                    new MyData(LoginAcitivity.this).clearPreferences();
                                                    FirebaseAuth.getInstance().signOut();
                                                    pDialog.dismiss();
                                                }
                                            }
                                            else
                                            {
                                                new SweetAlertDialog(LoginAcitivity.this,SweetAlertDialog.ERROR_TYPE)
                                                        .setTitleText(getString(R.string.invalid_credentials))
                                                        .setContentText(getString(R.string.login_error_off))
                                                        .show();
                                                new MyData(LoginAcitivity.this).clearPreferences();
                                                FirebaseAuth.getInstance().signOut();
                                                pDialog.dismiss();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            pDialog.dismiss();
                                            FirebaseAuth.getInstance().signOut();
                                        }
                                    });


                        }
                        else
                        {
                            pDialog.dismiss();
                            final SweetAlertDialog sDialog=new SweetAlertDialog(LoginAcitivity.this,SweetAlertDialog.ERROR_TYPE);
                            sDialog.setCancelable(false);
                            sDialog.setTitleText("Can't log you in").setContentText(""+task.getException().getLocalizedMessage()).
                                    setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                            sDialog.dismiss();
                                            // finish();
                                        }
                                    });
                            sDialog.show();
                        }
                    }
                });


    }
    private void saveStudentPrefs()
    {
        FirebaseDatabase.getInstance().getReference(getString(R.string.college_id)).child(getString(R.string.hostel_id))
                .child(getString(R.string.student_list_ref)).child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    StudentDetailsClass student=dataSnapshot.getValue(StudentDetailsClass.class);
                    MyData prefs=new MyData(getApplicationContext());
                    prefs.saveStudentPrefs(student);
                    pDialog.dismiss();
                    startActivity(new Intent(LoginAcitivity.this,Home.class));
                    finish();



                }
                else
                    {
                    {
                        new SweetAlertDialog(LoginAcitivity.this,SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Invalid User").setContentText("Please check your credentials and try again")
                                .show();
                        FirebaseAuth.getInstance().signOut();

                        new MyData(LoginAcitivity.this).clearPreferences();
                        Email.setText("");
                        Password.setText("");
                        pDialog.dismiss();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                FirebaseAuth.getInstance().signOut();
                pDialog.dismiss();
            }
        });

    }



}
