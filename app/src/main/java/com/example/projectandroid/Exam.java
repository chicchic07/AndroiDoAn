package com.example.projectandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Exam extends AppCompatActivity {
    private String quizID;
    private String uid;
    private int oldTotalPoints = 0;
    private int oldTotalQuestions = 0;
    private long timer;
    private DatabaseReference database;
    private TextView tv_timer;   //đồng hồ tính giờ làm bài
    CountDownTimer cdt;
    private long totalTimeElapsed = 0;   //lưu tgian làm bài
    public interface OnDataLoadedListener {
        void onDataLoaded(Question[] data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);

        quizID = getIntent().getStringExtra("Quiz ID");

        tv_timer=findViewById(R.id.tv_timer);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance().getReference();
        loadQuestion(new OnDataLoadedListener() {
            @Override
            public void onDataLoaded(Question[] data) {
                setQuestion(data);
                onClickEvent(data);
                tv_timer(data); //đồng hồ đếm ngược tgian làm bài
            }
        });
    }
    private void loadQuestion(OnDataLoadedListener loadedListener){
        TextView title = findViewById(R.id.title);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("Quizzes").hasChild(quizID)) {
                    DataSnapshot ref = snapshot.child("Quizzes").child(quizID);
                    title.setText(ref.child("Title").getValue().toString());
                    timer = Long.parseLong(ref.child("Timer").getValue().toString());
                    int num = Integer.parseInt(ref.child("Total Questions").getValue().toString());
                    Question[] data = new Question[num];
                    for (int i=0;i<num;i++) {
                        DataSnapshot qRef = ref.child("Questions").child(String.valueOf(i));
                        Question question = new Question();
                        question.setQuestions(qRef.child("Questions").getValue().toString());
                        question.setOption_1(qRef.child("Option_1").getValue().toString());
                        question.setOption_2(qRef.child("Option_2").getValue().toString());
                        question.setOption_3(qRef.child("Option_3").getValue().toString());
                        question.setOption_4(qRef.child("Option_4").getValue().toString());
                        int ans = Integer.parseInt(qRef.child("Answer").getValue().toString());
                        question.setAnswer(ans);
                        data[i] = question;
                    }
                    DataSnapshot ref2 = snapshot.child("Users").child(uid);
                    if (ref2.hasChild("Total Points")) {
                        oldTotalPoints = Integer.parseInt(ref2.child("Total Points").getValue().toString());
                    }
                    if (ref2.hasChild("Total Questions")) {
                        oldTotalQuestions = Integer.parseInt(ref2.child("Total Questions").getValue().toString());
                    }
                    loadedListener.onDataLoaded(data);
                } else {
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Exam.this, "Can't connect", Toast.LENGTH_SHORT).show();
            }
        };
        database.addValueEventListener(listener);
    }
    private void setQuestion(Question[] questions){
        ListView listview = findViewById(R.id.listview);
        ListAdapter adapter = new ListAdapter(questions);
        listview.setAdapter(adapter);
    }
    private void onClickEvent(Question[] data){
        Button submit = findViewById(R.id.submit);
        submit.setOnClickListener(v -> {
            DatabaseReference ref = database.child("Quizzes").child(quizID)
                    .child("Answers").child(uid);
            int totalPoints = oldTotalPoints;
            int points = 0;
            for (int i=0;i<data.length;i++) {
                ref.child(String.valueOf((i+1))).setValue(data[i].getSelectedAnswer());
                if (data[i].getSelectedAnswer()==data[i].getAnswer()) {
                    totalPoints++;
                    points++;
                }
            }
            ref.child("Points").setValue(points);
            int totalquestions = oldTotalQuestions+data.length;
            database.child("Users").child(uid).child("Total Points").setValue(totalPoints);
            database.child("Users").child(uid).child("Total Questions").setValue(totalquestions);
            database.child("Users").child(uid).child("Quizzes Solved").child(quizID).setValue("");

            long secondsRemaining = totalTimeElapsed / 1000;   //đổi tgian còn lại từ  mili giây -> giây
            long minutes = secondsRemaining / 60;
            long seconds = secondsRemaining % 60;
            long hours = minutes / 60;
            minutes = minutes % 60;
            cdt.cancel();
            Intent i = new Intent(Exam.this, Result.class);
            i.putExtra("Total time", String.format("%02d : %02d : %02d", hours, minutes, seconds));
            i.putExtra("Quiz ID", quizID);
            startActivity(i);
            finish();
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

            question.setText(arr[i].getQuestions());
            option1.setText(arr[i].getOption_1());
            option2.setText(arr[i].getOption_2());
            option3.setText(arr[i].getOption_3());
            option4.setText(arr[i].getOption_4());

            option1.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) arr[i].setSelectedAnswer(1);
            });
            option2.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) arr[i].setSelectedAnswer(2);
            });
            option3.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) arr[i].setSelectedAnswer(3);
            });
            option4.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) arr[i].setSelectedAnswer(4);
            });

            switch (arr[i].getSelectedAnswer()) {
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
            return v;

        }
    }

    private void tv_timer(Question[] data)
    {
        cdt = new CountDownTimer(timer, 1000) {   //chỉnh thời gian làm bài
            @Override
            public void onTick(long millisUntilFinished) {
                totalTimeElapsed += 1000; // Lưu tgian sau mỗi giây
                long secondsRemaining = millisUntilFinished / 1000;   //
                long minutes = secondsRemaining / 60;
                long seconds = secondsRemaining % 60;
                long hours = minutes / 60;
                minutes = minutes % 60;

                String formattedTime = String.format("%02d : %02d : %02d", hours, minutes, seconds);   //tạo 1 string để set text trả số vào đồng hồ
                tv_timer.setText(formattedTime);
            }

            @Override
            public void onFinish() {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Quizzes").child(quizID)
                        .child("Answers").child(uid);
                int totalPoints = oldTotalPoints;
                int points = 0;
                for (int i=0;i<data.length;i++) {
                    ref.child(String.valueOf((i+1))).setValue(data[i].getSelectedAnswer());
                    if (data[i].getSelectedAnswer()==data[i].getAnswer()) {
                        totalPoints++;
                        points++;
                    }
                }
                ref.child("Points").setValue(points);
                int totalquestions = oldTotalQuestions+data.length;
                FirebaseDatabase.getInstance().getReference("User").child(uid).child("Total Points").setValue(totalPoints);
                FirebaseDatabase.getInstance().getReference("User").child(uid).child("Total Questions").setValue(totalquestions);
                FirebaseDatabase.getInstance().getReference("User").child(uid).child("Quizzes Solved").child(quizID).setValue("");

                //trường hợp hết tgian làm bà, tính lại tgian, tạo string ms để nhét tgian làm bài vào, putExtra để qua Result gọi lại
                long secondsRemaining = totalTimeElapsed / 1000;   //đổi tgian còn lại từ  mili giây -> giây
                long minutes = secondsRemaining / 60;
                long seconds = secondsRemaining % 60;
                long hours = minutes / 60;
                minutes = minutes % 60;
                Intent i = new Intent(Exam.this, Result.class);
                i.putExtra("Total time", String.format("%02d : %02d : %02d", hours, minutes, seconds));
                i.putExtra("Quiz ID", quizID);
                startActivity(i);
                finish();
            }
        }.start();
    }
}