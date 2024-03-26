package com.example.projectandroid;

public class Question {

    private String Questions;
    private String Option_1;
    private String Option_2;
    private String Option_3;
    private String Option_4;
    private String Timer;
    private int Answer;
    private int selectedAnswer;

    public String getQuestions() {
        return Questions;
    }

    public void setQuestions(String questions) {
        Questions = questions;
    }

    public String getOption_1() {
        return Option_1;
    }

    public void setOption_1(String option_1) {
        Option_1 = option_1;
    }

    public String getOption_2() {
        return Option_2;
    }

    public void setOption_2(String option_2) {
        Option_2 = option_2;
    }

    public String getOption_3() {
        return Option_3;
    }

    public void setOption_3(String option_3) {
        Option_3 = option_3;
    }

    public String getOption_4() {
        return Option_4;
    }

    public void setOption_4(String option_4) {
        Option_4 = option_4;
    }

    public String getTimer() {
        return Timer;
    }

    public void setTimer(String timer) {
        Timer = timer;
    }

    public int getAnswer() {
        return Answer;
    }

    public void setAnswer(int answer) {
        Answer = answer;
    }

    public int getSelectedAnswer() {
        return selectedAnswer;
    }

    public void setSelectedAnswer(int selectedAnswer) {
        this.selectedAnswer = selectedAnswer;
    }
}
