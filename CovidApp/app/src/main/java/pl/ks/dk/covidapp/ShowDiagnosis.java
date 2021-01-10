package pl.ks.dk.covidapp;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ShowDiagnosis extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_diagnosis);

        ArrayList<String> answers = getIntent().getStringArrayListExtra("ANSWERS");
        ArrayList<String> questions = getIntent().getStringArrayListExtra("QUESTIONS");

        String patientName = getIntent().getStringExtra("NAME");
        String patientSurname = getIntent().getStringExtra("SURNAME");

        Integer[] questionsId = {R.id.question1, R.id.question2, R.id.question3, R.id.question4, R.id.question5, R.id.question6, R.id.question7, R.id.question8, R.id.question9, R.id.question10};
        Integer[] answersId = {R.id.answer1, R.id.answer2, R.id.answer3, R.id.answer4, R.id.answer5, R.id.answer6, R.id.answer7, R.id.answer8, R.id.answer9, R.id.answer10};

        for (int i = 0; i < answers.size(); i++) {
            TextView question = findViewById(questionsId[i]);
            question.setText(questions.get(i));
        }

        for (int i = 0; i < answers.size(); i++) {
            TextView answer = findViewById(answersId[i]);
            answer.setText(answers.get(i));
        }

        TextView patient = findViewById(R.id.patient_info);
        patient.setText(patientName.concat(" ").concat(patientSurname));
        patient.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
