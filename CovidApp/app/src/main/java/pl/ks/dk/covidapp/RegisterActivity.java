package pl.ks.dk.covidapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

                long maxTime = new Date().getTime();
                pickerDialog.getDatePicker().setMaxDate(maxTime);
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

                if (dataValidation(txt_name, txt_surname, txt_email, txt_password, txt_pesel, txt_phone_number, txt_date_of_birth)) {
                    register(txt_name, txt_surname, txt_username, txt_email, txt_password, txt_pesel, txt_phone_number, txt_date_of_birth);
                }

            }
        });
    }

    private boolean dataValidation(String name, String surname, String email, String password, String pesel, String phone, String date) {
        if (!checkIfRequired(name, surname, email, password, pesel, phone, date)) {
            Toast.makeText(RegisterActivity.this, R.string.required_fields, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!checkStringsLengths(name, surname)) {
            Toast.makeText(RegisterActivity.this, R.string.required_length, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!checkEmail(email)) {
            Toast.makeText(RegisterActivity.this, R.string.email_error, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!checkPhone(phone)) {
            Toast.makeText(RegisterActivity.this, R.string.phone_error, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!checkPassword(password)) {
            Toast.makeText(RegisterActivity.this, R.string.required_length_password, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!checkPesel(pesel)) {
            Toast.makeText(RegisterActivity.this, R.string.pesel_error, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean checkPhone(String phone) {
        return phone.length() == 9;
    }

    private boolean checkEmail(String email) {
        if (email.length() > 4) {
            return email.contains("@");
        }
        return false;
    }

    private boolean checkStringsLengths(String name, String surname) {
        return Stream.of(name, surname)
                .allMatch(s -> s.length() > 3);
    }

    private boolean checkPesel(String pesel) {
        List<Integer> list = Arrays.asList(1, 3, 7, 9, 1, 3, 7, 9, 1, 3);
        if (pesel.length() == 11) {
            int sum = 0;
            for (int i = 0; i < list.size(); i++) {
                sum += (Integer.parseInt(String.valueOf(pesel.charAt(i))) % 10) * list.get(i);
            }
//            char cd = pesel.charAt(10);
//            int x = 10 - (sum % 10);
            return (10 - (sum % 10)) == (pesel.charAt(10)-'0');
        }
        return false;
    }

    private boolean checkPassword(String password) {
        return password.length() >= 8;
    }

    private boolean checkIfRequired(String name, String surname, String
            email, String password, String pesel, String phone, String date) {
        return Stream.of(name, surname, email, password, pesel, phone, date)
                .allMatch(StringUtils::isNotBlank);
    }

    private void register(String name, String surname, String username, String email, String
            password, String pesel, String phoneNumber, String dateOfBirth) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    String userId = firebaseUser.getUid();

                    reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("id", userId);
                    hashMap.put("name", name);
                    hashMap.put("surname", surname);
                    hashMap.put("username", username);
                    hashMap.put("pesel", pesel);
                    hashMap.put("phoneNumber", phoneNumber);
                    hashMap.put("dateOfBirth", dateOfBirth);
                    hashMap.put("imageURL", "default");
                    hashMap.put("status", "offline");
                    hashMap.put("search", username.toLowerCase());
                    hashMap.put("role", "patient");
                    hashMap.put("waitingForDiagnosis", "false");

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