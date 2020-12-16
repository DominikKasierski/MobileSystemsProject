package pl.ks.dk.covidapp.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pl.ks.dk.covidapp.R;

public class DecisionTreeFragment extends Fragment {

    private List<Integer> answers;
    private EditText answer;
    private Button btn_submit;
    private boolean ifListContainsNullOrEmptyStrings;
    private boolean ifListContainsNumbersFromRange;
    private final int min = 1;
    private final int max = 5;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_decision_tree, container, false);

        btn_submit = view.findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer[] ids = {R.id.answer1, R.id.answer2, R.id.answer3, R.id.answer4, R.id.answer5, R.id.answer6, R.id.answer7, R.id.answer8, R.id.answer9, R.id.answer10};
                answers = new ArrayList<>();

                for (Integer id : ids) {
                    answer = (EditText) view.findViewById(id);
                    String tmp = answer.getText().toString();
                    if (tmp.equals("")) {
                        Toast.makeText(getContext(), R.string.required_fields, Toast.LENGTH_SHORT).show();
                    } else {
                        answers.add(Integer.parseInt(tmp));
                    }
                }

                ifListContainsNumbersFromRange = answers.stream().allMatch(i -> i >= min && i <= max);

                if (!ifListContainsNumbersFromRange) {
                    Toast.makeText(getContext(), R.string.number_range, Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }
}