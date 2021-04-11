package com.example.EnglishApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.EnglishApp.models.Questions;
import com.example.EnglishApp.models.VocabularyElement;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.Random;

public class AdaptiveTest extends AppCompatActivity {

    DatabaseReference lessonsRef,currUserVocabulary;
    FirebaseDatabase db;
    FirebaseAuth auth;
    //массив для вопросов всех уровней, составляется рандомно
    ArrayList<Questions> questions = new ArrayList<Questions>();
    //массив для вопросов Beginner
    ArrayList<Questions> questionsBeginner = new ArrayList<Questions>();
    //массив для вопросов IAdvanced
    ArrayList<Questions> questionsAdvanced = new ArrayList<Questions>();
    //массив для вопросов Intermediate
    ArrayList<Questions> questionsIntermediate = new ArrayList<Questions>();
    //массив для вопросов массив для выделения правильного ответа
    ArrayList<Integer> rightAnswersList = new ArrayList<Integer>();
    //веса для правильных ответов
    final int weightA = 5, weightI = 3, weightB = 2;
    // число вопросов каждого уровня в общем тесте
    int sizeB = 10;
    int sizeA = 5 , sizeI = 5;

    Button v1,v2,v3,v4,start;
    TextView questionText,questionNumber;
    int number_of_question = 0;
    int rightAnswers = 0;
    private Animation animation = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.vocabulary_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.AddWord)
        {
            android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(this);

            dialog.setTitle("Новое слово");
            dialog.setMessage("Введите новое слово");

            //Объект для получение шаблона окна
            LayoutInflater inflater = LayoutInflater.from(this);
            View addWordWindow =  inflater.inflate(R.layout.add_word_window,null);
            //Устанавливаем шаблон для всплывающего окна
            dialog.setView(addWordWindow);
            final MaterialEditText word = addWordWindow.findViewById(R.id.wordField);
            final MaterialEditText translation = addWordWindow.findViewById(R.id.translationField);
            translation.setVisibility(View.GONE);

            dialog.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            dialog.setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (TextUtils.isEmpty(word.getText().toString())){
                        Toast logon = Toast.makeText(getApplicationContext(),"Введите слово",Toast.LENGTH_SHORT);
                        logon.setGravity(Gravity.TOP,0,250);
                        logon.show();
                        return;
                    }

                    currUserVocabulary.push().
                            //Заносим в БД новое слово
                                    setValue(
                                    new VocabularyElement(
                                            word.getText().toString(),
                                            //translation.getText().toString()
                                            "empty"
                                    )
                            );
                }
            });
            dialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        v1 = (Button) findViewById(R.id.v_1);
        v2 = (Button) findViewById(R.id.v_2);
        v3 = (Button) findViewById(R.id.v_3);
        v4 = (Button) findViewById(R.id.v_4);
        questionText = (TextView) findViewById(R.id.question_text);
        questionNumber =(TextView) findViewById(R.id.question_number);
        start = (Button) findViewById(R.id.start);

        View.OnClickListener onClickListener = new ClickListener();
        v1.setOnClickListener(onClickListener);
        v2.setOnClickListener(onClickListener);
        v3.setOnClickListener(onClickListener);
        v4.setOnClickListener(onClickListener);
        start.setOnClickListener(onClickListener);

        auth = FirebaseAuth.getInstance();
        db=FirebaseDatabase.getInstance();
        currUserVocabulary = db.getReference("Vocabularies").child(auth.getCurrentUser().getUid());
        lessonsRef = db.getReference("Lessons");

        //из бд достаем вопросы по каждому уровню с каждого урока
        // в порядке: Beginner->Intermediate_.Advanced
        lessonsRef.addValueEventListener(new ValueEventListener()
        {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    for(DataSnapshot snapshot1 : dataSnapshot.getChildren())
                    {
                        if (String.valueOf(snapshot1.getKey()).equals("Beginner"))
                        {
                            Log.i("Level ",String.valueOf(snapshot1.getKey()));
                            int quantityOfLessons = (int) snapshot1.getChildrenCount();
                            Log.i("Quantity of Lessons in Level", String.valueOf(quantityOfLessons));
                            for (int i = 0; i < quantityOfLessons; i++)
                            {
                                for (DataSnapshot snapshot : snapshot1.getChildren())
                                {
                                    if (String.valueOf(snapshot.getKey()).equals("Lesson " + (i + 1)))
                                    {
                                        Log.i("", String.valueOf(snapshot.getKey()));
                                        int quantityOfQuestions = (int) snapshot.child("Test").getChildrenCount();
                                        Log.i("Quantity of Questions in Lesson", String.valueOf(quantityOfQuestions));
                                        for (int j = 0; j < quantityOfQuestions; j++)
                                        {
                                            Questions question = snapshot.child("Test").child("Question " + (j + 1)).getValue(Questions.class);
                                            questionsBeginner.add(question);
                                            Log.i("Lesson " + (i + 1) + " Question ", String.valueOf(questionsBeginner.size()));
                                        }
                                    }
                                }
                            }
                        }
                        if (String.valueOf(snapshot1.getKey()).equals("Intermediate"))
                        {
                            Log.i("Level ",String.valueOf(snapshot1.getKey()));
                            int quantityOfLessons = (int) snapshot1.getChildrenCount();
                            Log.i("Quantity of Lessons in Level", String.valueOf(quantityOfLessons));
                            for (int i = 0; i < quantityOfLessons; i++)
                            {
                                for (DataSnapshot snapshot : snapshot1.getChildren())
                                {
                                    if (String.valueOf(snapshot.getKey()).equals("Lesson " + (i + 1)))
                                    {
                                        Log.i("", String.valueOf(snapshot.getKey()));
                                        int quantityOfQuestions = (int) snapshot.child("Test").getChildrenCount();
                                        Log.i("Quantity of Questions in Lesson", String.valueOf(quantityOfQuestions));
                                        for (int j = 0; j < quantityOfQuestions; j++)
                                        {
                                            Questions question = snapshot.child("Test").child("Question " + (j + 1)).getValue(Questions.class);
                                            questionsIntermediate.add(question);
                                            Log.i("Lesson " + (i + 1) + " Question ", String.valueOf(questionsIntermediate.size()));
                                        }
                                    }
                                }
                            }
                        }
                        if (String.valueOf(snapshot1.getKey()).equals("Advanced"))
                        {
                            Log.i("Level ",String.valueOf(snapshot1.getKey()));
                            int quantityOfLessons = (int) snapshot1.getChildrenCount();
                            Log.i("Quantity of Lessons in Level", String.valueOf(quantityOfLessons));
                            for (int i = 0; i < quantityOfLessons; i++)
                            {
                                for (DataSnapshot snapshot : snapshot1.getChildren())
                                {
                                    if (String.valueOf(snapshot.getKey()).equals("Lesson " + (i + 1)))
                                    {
                                        Log.i("", String.valueOf(snapshot.getKey()));
                                        int quantityOfQuestions = (int) snapshot.child("Test").getChildrenCount();
                                        Log.i("Quantity of Questions in Lesson", String.valueOf(quantityOfQuestions));
                                        for (int j = 0; j < quantityOfQuestions; j++)
                                        {
                                            Questions question = snapshot.child("Test").child("Question " + (j + 1)).getValue(Questions.class);
                                            questionsAdvanced.add(question);
                                            Log.i("Lesson " + (i + 1) + " Question ", String.valueOf(questionsAdvanced.size()));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
    }


    public void randomListOfQuestions()
    {
        //первые рандомные вопросы - Beginner
        for (int i = 0; i< sizeB; i++)
        {
           int rnum =new Random().nextInt(questionsBeginner.size());
           questions.add(questionsBeginner.get(rnum));
           questionsBeginner.remove(rnum);
        }
        //вторые рандомные вопросы - Intermediate
        for (int i = 0; i< sizeI; i++)
        {
            int rnum =new Random().nextInt(questionsIntermediate.size());
            questions.add(questionsIntermediate.get(rnum));
            questionsIntermediate.remove(rnum);
        }
        //третьи рандомные вопросы - Advanced
        for (int i = 0; i< sizeA; i++)
        {
            int rnum =new Random().nextInt(questionsAdvanced.size());
            questions.add(questionsAdvanced.get(rnum));
            questionsAdvanced.remove(rnum);
        }
    }

    private class ClickListener implements View.OnClickListener {

        public void onClick(View view) {
            int id = view.getId();
            Handler handler = new Handler();
            //handler для задержки, не нажать несколько ответов
            Runnable r = new Runnable() {
                public void run() {
                    nextQuestion();
                }
            };
            //по кнопке старт начинаем тест
            if (id == R.id.start) {
                randomListOfQuestions();
                nextQuestion();
                findViewById(R.id.grid_view).setVisibility(View.VISIBLE);
                start.setVisibility(View.GONE);
                questionNumber.setVisibility(View.VISIBLE);
                questionText.setVisibility(View.VISIBLE);
                animation = AnimationUtils.loadAnimation(
                        AdaptiveTest.this, android.R.anim.fade_in);
                findViewById(R.id.grid_view).startAnimation(animation);
                questionNumber.startAnimation(animation);
                questionText.startAnimation(animation);
            } else {
                //при нажатии на любую кнопку, все кнопки отключаются, чтобы
                // не нажать несколько ответов
                v1.setClickable(false);
                v2.setClickable(false);
                v3.setClickable(false);
                v4.setClickable(false);
            }
            //нажатие кнопок с ответами
            if (id == R.id.v_1) {
                if (questions.get(number_of_question).getRight().equals(v1.getText())) {
                    number_of_question++;
                    rightAnswers++;
                    v1.setBackgroundResource(R.drawable.btn_succes);
                    rightAnswersList.add(1);
                } else {
                    number_of_question++;
                    v1.setBackgroundResource(R.drawable.btn_failed);
                    rightAnswersList.add(0);
                }
            }
            if (id == R.id.v_2) {
                if (questions.get(number_of_question).getRight().equals(v2.getText())) {
                    number_of_question++;
                    rightAnswers++;
                    v2.setBackgroundResource(R.drawable.btn_succes);
                    rightAnswersList.add(1);
                } else {
                    number_of_question++;
                    v2.setBackgroundResource(R.drawable.btn_failed);
                    rightAnswersList.add(0);
                }
            }
            if (id == R.id.v_3) {
                if (questions.get(number_of_question).getRight().equals(v3.getText())) {
                    number_of_question++;
                    rightAnswers++;
                    v3.setBackgroundResource(R.drawable.btn_succes);
                    rightAnswersList.add(1);
                } else {
                    number_of_question++;
                    v3.setBackgroundResource(R.drawable.btn_failed);
                    rightAnswersList.add(0);
                }
            }
            if (id == R.id.v_4) {
                if (questions.get(number_of_question).getRight().equals(v4.getText())) {
                    number_of_question++;
                    rightAnswers++;
                    v4.setBackgroundResource(R.drawable.btn_succes);
                    rightAnswersList.add(1);
                } else {
                    number_of_question++;
                    v4.setBackgroundResource(R.drawable.btn_failed);
                    rightAnswersList.add(0);
                }
            }
            handler.postDelayed(r, 1500);
        }
    }
    public void nextQuestion()
    {
        if(number_of_question<questions.size())
        {
            v1.setBackgroundResource(R.drawable.btn_norm);
            v2.setBackgroundResource(R.drawable.btn_norm);
            v3.setBackgroundResource(R.drawable.btn_norm);
            v4.setBackgroundResource(R.drawable.btn_norm);

            questionNumber.setText(String.format("Question  %d  of  %d",number_of_question+1,questions.size()));
            v1.setText(questions.get(number_of_question).getV1());
            v2.setText(questions.get(number_of_question).getV2());
            v3.setText(questions.get(number_of_question).getV3());
            v4.setText(questions.get(number_of_question).getV4());
            questionText.setText(questions.get(number_of_question).getQuestionText());

        }
        else
            {
                questionText.setVisibility(View.GONE);
                animation = AnimationUtils.loadAnimation(
                        AdaptiveTest.this, android.R.anim.fade_out);
                findViewById(R.id.grid_view).setVisibility(View.GONE);
                findViewById(R.id.grid_view).startAnimation(animation);
                questionText.startAnimation(animation);
                showFinishTestWindow();

        }
        v1.setClickable(true);
        v2.setClickable(true);
        v3.setClickable(true);
        v4.setClickable(true);
    }

    private void showFinishTestWindow() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(AdaptiveTest.this);
        alertDialog.setTitle("Завершение теста");
        alertDialog.setMessage("Ваш результат:\n");

        //Объект для получение шаблона окна
        LayoutInflater inflater = LayoutInflater.from(this);
        //Получаем шаблон окна register_window в переменную registerWindow
        View finishTestWindow =  inflater.inflate(R.layout.finish_test_window,null);
        //Устанавливаем шаблон для всплывающего окна
        alertDialog.setView(finishTestWindow);

        TextView tvResult = finishTestWindow.findViewById(R.id.tvResult);

        int yourRightAnswers = 0;
        for (int i=0;i<sizeB;i++) {
            yourRightAnswers +=(weightB * rightAnswersList.get(i));
            Log.i("Beginners answers", String.valueOf(yourRightAnswers));
        }
        for (int i=sizeB;i<sizeB+sizeI;i++) {
            yourRightAnswers +=(weightI * rightAnswersList.get(i));
            Log.i("Intermediate answers", String.valueOf(yourRightAnswers));
        }
        for (int i=sizeB+sizeI;i<sizeB+sizeI+sizeA;i++) {
            yourRightAnswers +=(weightA * rightAnswersList.get(i));
            Log.i("Advanced answers", String.valueOf(yourRightAnswers));
        }

        int maxRightAnswer = weightB * sizeB + weightI * sizeI + weightA * sizeA;

        float floatPercentage =(float) yourRightAnswers/maxRightAnswer *100;
        int percentage = (int)floatPercentage;
        int buf = 0;
        String resultString = String.format("%d/ %d (%d %%)",rightAnswers,number_of_question,percentage);

        SharedPreferences percentages = getApplicationContext()
                .getSharedPreferences("com.example.EnglishApp", Context.MODE_PRIVATE);
        String userEmail = percentages.getString("email","empty");

        if (!percentages.contains(userEmail+" Level percentages"))
        {
            percentages.edit().putInt(userEmail+" Level percentages",percentage).apply();
            Log.i(" percentage after completing =",String.valueOf(percentage));
        }
        //иначе получаем значение процентов и сравниваем их
        else
        {
            buf = percentages.getInt(userEmail+" Level percentages",-1);
            Log.i(" percentage after completing =",String.valueOf(percentage));
            Log.i(" buffer =",String.valueOf(buf));
            if (buf<percentage)
            {
                percentages.edit().putInt(userEmail+" Level percentages",percentage).apply();
                Log.i(" placed percentage =",String.valueOf(percentage));
            }
        }
        tvResult.setText(resultString);
        alertDialog.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        alertDialog.show();
    }
}