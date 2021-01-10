package pl.ks.dk.covidapp.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import pl.ks.dk.covidapp.MainActivity;
import pl.ks.dk.covidapp.Model.User;
import pl.ks.dk.covidapp.R;

public class DecisionTreeFragment extends Fragment {

    private RadioGroup radioGroup;
    private RadioButton radioButton;
    FirebaseUser firebaseUser;
    DatabaseReference reference;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_decision_tree, container, false);
        Button btn_submit = view.findViewById(R.id.btn_submit);
            btn_submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Integer[] ids = {R.id.radio_group1, R.id.radio_group2, R.id.radio_group3, R.id.radio_group4, R.id.radio_group5, R.id.radio_group6, R.id.radio_group7, R.id.radio_group8, R.id.radio_group9, R.id.radio_group10};
                    List<Integer> answers = new ArrayList<>();

                    for (Integer id : ids) {
                        radioGroup = (RadioGroup) view.findViewById(id);
                        radioButton = view.findViewById(radioGroup.getCheckedRadioButtonId());
                        String tmp = radioButton.getTag().toString();
                        answers.add(Integer.parseInt(tmp));
                    }

                    double score = calculateWeights(answers);

                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    changeIsWaitingForDiagnosis();
                                    saveDiagnosisInDatabase(answers);
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(getResources().getString(R.string.diagnosis))
                            .setMessage(getMessage(score))
                            .setPositiveButton(getResources().getString(R.string.contact_with_doctor), dialogClickListener)
                            .setNegativeButton(getResources().getString(R.string.back_to_main), dialogClickListener).show();
                }
            });
            return view;
        }

    private void changeIsWaitingForDiagnosis() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("waitingForDiagnosis", "true");
        reference.updateChildren(hashMap);
    }


    private Double calculateWeights(List<Integer> answers) {
        double score = 0;
        List<Double> weights = Arrays.asList(8.8, 6.8, 3.8, 3.3, 2.0, 1.5, 1.4, 1.3, 1.1, 0.4);
        for (int i = 0; i < answers.size(); i++) {
            score += answers.get(i) * weights.get(i);
        }
        return score;
    }

    private String getMessage(double score) {
        if (score < 2) {
            return getResources().getString(R.string.message1);
        } else if (score < 5) {
            return getResources().getString(R.string.message2);
        } else {
            return getResources().getString(R.string.message3);
        }
    }

    private void saveDiagnosisInDatabase(List<Integer> answers) {
//        TODO:OGARNAC KLUCZE
        reference = FirebaseDatabase.getInstance().getReference("Diagnosis").child(firebaseUser.getUid());
        Integer[] ids = {R.string.question1, R.string.question2, R.string.question3, R.string.question4, R.string.question5, R.string.question6, R.string.question7, R.string.question8, R.string.question9, R.string.question10};
        HashMap<String, Object> hashMap = new HashMap<>();

        for (int i = 0; i < 10; i++) {
            hashMap.put(getResources().getString(ids[i]), answers.get(i).toString());
        }
        reference.setValue(hashMap);
    }
}