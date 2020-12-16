package pl.ks.dk.covidapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.stream.Stream;

public class RegisterActivity extends AppCompatActivity {

    MaterialEditText name, surname, email, password, pesel, phoneNumber, dateOfBirth;
    Button btn_register;
    DatePickerDialog pickerDialog;

    FirebaseAuth auth;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        name = findViewById(R.id.name);
        surname = findViewById(R.id.surname);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        pesel = findViewById(R.id.pesel);
        phoneNumber = findViewById(R.id.phone_number);
        dateOfBirth = findViewById(R.id.birth_date);
        btn_register = findViewById(R.id.btn_register);

        auth = FirebaseAuth.getInstance();

        dateOfBirth.setInputType(InputType.TYPE_NULL);
        dateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                pickerDialog = new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String date = dayOfMonth + "/" + (month + 1) + "/" + year;
                        dateOfBirth.setText(date);
                    }
                }, year, month, day);
                pickerDialog.show();
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt_name = name.getText().toString();
                String txt_surname = surname.getText().toString();
                String txt_username = txt_name.concat(txt_surname);
                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();
                String txt_pesel = pesel.getText().toString();
                String txt_phone_number = phoneNumber.getText().toString();
                String txt_date_of_birth = dateOfBirth.getText().toString();

                boolean valid =  Stream.of(txt_name, txt_surname, txt_username, txt_email, txt_password, txt_pesel, txt_phone_number, txt_date_of_birth)
                        .allMatch(StringUtils::isNotBlank);

                if (!valid) {
                    Toast.makeText(RegisterActivity.this, R.string.required_fields, Toast.LENGTH_SHORT).show();
                } else if (txt_password.length() < 8) {
                    Toast.makeText(RegisterActivity.this, R.string.required_length_password, Toast.LENGTH_SHORT).show();
                } else {
                    register(txt_username, txt_email, txt_password, txt_pesel, txt_phone_number, txt_date_of_birth);
                }
            }
        });
    }

    private void register(String username, String email, String password, String pesel, String phoneNumber, String dateOfBirth) {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        String userId = firebaseUser.getUid();

                        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("id", userId);
                        hashMap.put("username", username);
                        hashMap.put("pesel", pesel);
                        hashMap.put("phoneNumber", phoneNumber);
                        hashMap.put("dateOfBirth", dateOfBirth);
                        hashMap.put("imageURL", "default");
                        hashMap.put("status", "offline");
                        hashMap.put("search", username.toLowerCase());

                        reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(RegisterActivity.this, R.string.register_error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }
}