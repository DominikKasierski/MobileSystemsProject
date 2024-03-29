package pl.ks.dk.covidapp.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import pl.ks.dk.covidapp.Model.User;
import pl.ks.dk.covidapp.R;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    CircleImageView image_profile;
    TextView username, role_value;
    EditText name_value, surname_value, date_of_birth_value, pesel_profile_value, phone_number_profile_value;
    Button save_button, edit_button;
    User user;

    DatePickerDialog pickerDialog;


    DatabaseReference reference;
    FirebaseUser firebaseUser;

    StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        image_profile = view.findViewById(R.id.profile_image);
        username = view.findViewById(R.id.username);
        role_value = view.findViewById(R.id.role);
        name_value = view.findViewById(R.id.name_value);
        surname_value = view.findViewById(R.id.surname_value);
        date_of_birth_value = view.findViewById(R.id.date_of_birth_value);
        pesel_profile_value = view.findViewById(R.id.pesel_profile_value);
        phone_number_profile_value = view.findViewById(R.id.phone_number_profile_value);
        save_button = view.findViewById(R.id.save_button);
        edit_button = view.findViewById(R.id.edit_button);

        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(isAdded()) {
                    user = dataSnapshot.getValue(User.class);
                    if (user.getImageURL().equals("default")) {
                        image_profile.setImageResource(R.mipmap.ic_launcher);
                    } else {
                        Glide.with(getContext()).load(user.getImageURL()).into(image_profile);
                    }
                    setEditTexts();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        date_of_birth_value.setInputType(InputType.TYPE_NULL);
        date_of_birth_value.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                pickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String date = dayOfMonth + "/" + (month + 1) + "/" + year;
                        date_of_birth_value.setText(date);
                    }
                }, year, month, day);

                long maxTime = new Date().getTime();
                pickerDialog.getDatePicker().setMaxDate(maxTime);
                pickerDialog.show();
            }
        });

        edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editMode();
            }
        });

        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("name", name_value.getText().toString());
                                hashMap.put("surname", surname_value.getText().toString());
                                hashMap.put("dateOfBirth", date_of_birth_value.getText().toString());
                                hashMap.put("pesel", pesel_profile_value.getText().toString());
                                hashMap.put("phoneNumber", phone_number_profile_value.getText().toString());
                                reference.updateChildren(hashMap);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                setEditTexts();
                                break;
                        }
                        notEditMode();
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getResources().getString(R.string.confirm))
                        .setMessage(getResources().getString(R.string.are_you_sure))
                        .setPositiveButton(getResources().getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getResources().getString(R.string.no), dialogClickListener).show();
            }
        });

        image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });
        return view;
    }

    private void editMode() {
        save_button.setVisibility(View.VISIBLE);
        edit_button.setVisibility(View.INVISIBLE);
        name_value.setEnabled(true);
        surname_value.setEnabled(true);
        date_of_birth_value.setEnabled(true);
        pesel_profile_value.setEnabled(true);
        phone_number_profile_value.setEnabled(true);
    }

    private void notEditMode() {
        save_button.setVisibility(View.INVISIBLE);
        edit_button.setVisibility(View.VISIBLE);
        name_value.setEnabled(false);
        surname_value.setEnabled(false);
        date_of_birth_value.setEnabled(false);
        pesel_profile_value.setEnabled(false);
        phone_number_profile_value.setEnabled(false);
    }

    private void setEditTexts() {
        username.setText(user.getUsername());
        role_value.setText(user.getRole().toUpperCase());
        name_value.setText(user.getName());
        surname_value.setText(user.getSurname());
        date_of_birth_value.setText(user.getDateOfBirth());
        pesel_profile_value.setText(user.getPesel());
        phone_number_profile_value.setText(user.getPhoneNumber());
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Uploading");
        progressDialog.show();

        if (imageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw  task.getException();
                    }

                    return  fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("imageURL", mUri);
                        reference.updateChildren(map);

                        progressDialog.dismiss();
                    } else {
                        Toast.makeText(getContext(), "Failed!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        } else {
            Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        setEditTexts();
        notEditMode();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()) {
               Toast.makeText(getContext(), "Upload in progress", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        }
    }
}

//TODO: numer telefonu inaczej wyświetlać