package pl.ks.dk.covidapp.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pl.ks.dk.covidapp.R;

public class DecisionTreeFragment extends Fragment {

    private List<Integer> answers;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private Button btn_submit;
    private boolean ifListContainsNumbersFromRange;
    private final int min = 1;
    private final int max = 5;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //pobrac zmienna z usera, jeśli 0 to wyswietla się całe okno diagnozy, jesli 1 to wyswietla sie tylko napis "Poczekaj az lekarz sie skontaktuje"
        View view = inflater.inflate(R.layout.fragment_decision_tree, container, false);
        btn_submit = view.findViewById(R.id.btn_submit);
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
                                //Ustawic zmienna jakas w userze na 1
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage(getMessage(score)).setPositiveButton(getResources().getString(R.string.contact_with_doctor), dialogClickListener)
                        .setNegativeButton(getResources().getString(R.string.back_to_main), dialogClickListener).show();
            }
        });
        return view;
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
}