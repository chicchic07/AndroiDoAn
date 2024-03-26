package com.example.projectandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.app.GameManager;
import android.app.GameState;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Result extends AppCompatActivity {
    private Question[] data;
    private String uid;
    private String quizID;
    String totalTimeElapsed;
    BottomNavigationItemView nav;
    ImageButton btnBack;
    TextView title;
    ListView listview;
    TextView total;
    public interface OnDataLoadedListener {
        void onDataLoaded(Question[] data);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        quizID = getIntent().getStringExtra("Quiz ID");
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (getIntent().hasExtra("User UID")) uid = getIntent().getStringExtra("User UID");

        initView();
        totalTime();
        showListQuestion();
        onClickEvent();

    }
    @Override
    public boolean onSupportNavigateUp() {
        // Xử lý hành động khi click nút back
        // Quay về trang trước đó
        finish();
        return true;
    }
    private void initView(){
        title = findViewById(R.id.title);
        listview = findViewById(R.id.listview);
        total = findViewById(R.id.total);
    }
    private void totalTime(){
        totalTimeElapsed = getIntent().getStringExtra("Total time");
        if (totalTimeElapsed != null){
            // Display total time elapsed
            AlertDialog.Builder builder = new AlertDialog.Builder(Result.this);
            builder.setMessage("Total time elapsed: " + totalTimeElapsed + " sceconds")
                    .setTitle("Quiz Completed")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }
    private void showListQuestion(){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("Quizzes").hasChild(quizID)) {
                    DataSnapshot ansRef = snapshot.child("Quizzes").child(quizID).child("Answers").child(uid);
                    DataSnapshot qRef = snapshot.child("Quizzes").child(quizID);
                    title.setText(qRef.child("Title").getValue().toString());
                    int num = Integer.parseInt(qRef.child("Total Questions").getValue().toString());
                    data = new Question[num];
                    int correctAns = 0;
                    for (int i=0;i<num;i++) {
                        DataSnapshot qRef2 = qRef.child("Questions").child(String.valueOf(i));
                        Question question = new Question();
                        question.setQuestion(qRef2.child("Question").getValue().toString());
                        question.setOption1(qRef2.child("Option 1").getValue().toString());
                        question.setOption2(qRef2.child("Option 2").getValue().toString());
                        question.setOption3(qRef2.child("Option 3").getValue().toString());
                        question.setOption4(qRef2.child("Option 4").getValue().toString());
                        question.setSelectedAnswer(Integer.parseInt(
                                ansRef.child(String.valueOf((i+1))).getValue().toString()));
                        int ans = Integer.parseInt(qRef2.child("Ans").getValue().toString());
                        if (ans==question.getSelectedAnswer()) correctAns++;
                        question.setCorrectAnswer(ans);
                        data[i] = question;
                    }
                    total.setText("Total "+correctAns+"/"+data.length);
                    ListAdapter listAdapter = new ListAdapter(data);
                    listview.setAdapter(listAdapter);
                } else {
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Result.this, "Can't connect", Toast.LENGTH_SHORT).show();
            }
        };
        database.addValueEventListener(listener);
    }
    private void onClickEvent(){
        // Nút back
        btnBack=findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Result.this, Home.class);
                startActivity(intent);
                finish();
            }
        });

    }
    public class ListAdapter extends BaseAdapter {
        Question[] arr;

        ListAdapter(Question[] arr2) {
            arr = arr2;
        }

        @Override
        public int getCount() {
            return arr.length;
        }

        @Override
        public Object getItem(int i) {
            return arr[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            LayoutInflater inflater = getLayoutInflater();
            View v = inflater.inflate(R.layout.question, null);

            TextView question = v.findViewById(R.id.question);
            RadioButton option1 = v.findViewById(R.id.option1);
            RadioButton option2 = v.findViewById(R.id.option2);
            RadioButton option3 = v.findViewById(R.id.option3);
            RadioButton option4 = v.findViewById(R.id.option4);
            TextView result = v.findViewById(R.id.result);

            question.setText(data[i].getQuestion());
            option1.setText(data[i].getOption1());
            option2.setText(data[i].getOption2());
            option3.setText(data[i].getOption3());
            option4.setText(data[i].getOption4());

            switch (data[i].getSelectedAnswer()) {
                case 1:
                    option1.setChecked(true);
                    break;
                case 2:
                    option2.setChecked(true);
                    break;
                case 3:
                    option3.setChecked(true);
                    break;
                case 4:
                    option4.setChecked(true);
                    break;
            }

            option1.setEnabled(false);
            option2.setEnabled(false);
            option3.setEnabled(false);
            option4.setEnabled(false);

            result.setVisibility(View.VISIBLE);

            if (data[i].getSelectedAnswer()==data[i].getCorrectAnswer()) {
                result.setBackgroundResource(R.drawable.green_background);
                result.setTextColor(ContextCompat.getColor(Result.this, R.color.green_dark));
                result.setText("Correct Answer");
            } else {
                result.setBackgroundResource(R.drawable.red_background);
                result.setTextColor(ContextCompat.getColor(Result.this, R.color.red_dark));
                result.setText("Wrong Answer");

                switch (data[i].getCorrectAnswer()) {
                    case 1:
                        option1.setBackgroundResource(R.drawable.green_background);
                        break;
                    case 2:
                        option2.setBackgroundResource(R.drawable.green_background);
                        break;
                    case 3:
                        option3.setBackgroundResource(R.drawable.green_background);
                        break;
                    case 4:
                        option4.setBackgroundResource(R.drawable.green_background);
                        break;
                }

            }

            return v;
        }
    }
}