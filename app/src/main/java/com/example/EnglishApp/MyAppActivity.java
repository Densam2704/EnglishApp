package com.example.EnglishApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyAppActivity extends AppCompatActivity
{
    FloatingActionButton chat,vocabulary;
    Button generalTest,beginner,intermediate,advanced;
    TextView tvProgress;

    FirebaseAuth auth;
    DatabaseReference users;
    DatabaseReference currentUserRef;
    SharedPreferences sharedPrefs;

    //процент прохождения общего теста
    int progressInDB;
    //константы для определения уровня владения языка
    private static final int REQUIRED_PROGRESS_FOR_INTERMEDIATE=45;
    private static final int REQUIRED_PROGRESS_FOR_ADVANCED=80;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_app);
        getSupportActionBar().hide();

        chat = (FloatingActionButton) findViewById(R.id.btn_chat);
        vocabulary = (FloatingActionButton) findViewById(R.id.btn_vocabular_fl);
        generalTest = (Button) findViewById(R.id.btn_general_test);
        beginner = (Button) findViewById(R.id.btn_beginner_lessons);
        intermediate = (Button) findViewById(R.id.btn_intermediate_lessons);
        advanced = (Button) findViewById(R.id.btn_advanced_lessons);
        tvProgress=(TextView)findViewById(R.id.tvProgress);

        auth=FirebaseAuth.getInstance();
        users= FirebaseDatabase.getInstance().getReference("Users");
        sharedPrefs = getSharedPreferences("com.example.EnglishApp",Context.MODE_PRIVATE);

        View.OnClickListener onClickListener = new ClickListener();
        chat.setOnClickListener(onClickListener);
        vocabulary.setOnClickListener(onClickListener);
        generalTest.setOnClickListener(onClickListener);
        beginner.setOnClickListener(onClickListener);
        intermediate.setOnClickListener(onClickListener);
        advanced.setOnClickListener(onClickListener);
    }

    @Override
    protected void onStart() {
        super.onStart();

        final String email = sharedPrefs.getString("email","empty");
        Log.i("email from mem",email);
        String password = sharedPrefs.getString("password","0");
        currentUserRef=users.child(auth.getCurrentUser().getUid());
        //получение уровня владения английским из бд и синхронизация с sharedprefs
        Log.i("current user ref",currentUserRef.toString());
        currentUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String userKey=dataSnapshot.getKey();
                Log.i("userKey",userKey);
                progressInDB=Integer.parseInt(dataSnapshot.child("progress").getValue().toString());
                Log.i("progressInDB",String.valueOf(progressInDB));
                int progress=0;
                //Если прогресс есть в памяти телефона
                if(sharedPrefs.contains(email+" Level percentages")){
                    Log.i("Progress from storage",String.valueOf(sharedPrefs.getInt(" Level percentages",-1)));
                    int progressInStorage = sharedPrefs.getInt(email+" Level percentages", -1);
                    //Если прогресс в памяти телефона юольш, чем в бд
                    if (progressInStorage>progressInDB){
                        progressInDB=progressInStorage;
                        progress=progressInDB;
                        currentUserRef.child("progress").setValue(String.valueOf(progressInDB));
                    }
                    //Если прогресс в памяти телефона юольш, чем в бд
                    else {
                        progress=progressInDB;
                    }

                }//Если прогресса нет в памяти телефона
                else {
                    progress=progressInDB;
                    sharedPrefs.edit().putInt(email+" Level percentages",progressInDB).apply();
                }
                //Вывод уровень владения языка в соответствии с прогрессом
                if (progress<=REQUIRED_PROGRESS_FOR_INTERMEDIATE)
                {
                    tvProgress.setText(String.format("Ваш уровень - Beginner (%d%%)",progress));
                }
                else{
                    if (progress<=REQUIRED_PROGRESS_FOR_ADVANCED)
                    {
                        tvProgress.setText(String.format("Ваш уровень - Intermediate (%d%%)",progress));
                    }
                    else{
                        if (progress>REQUIRED_PROGRESS_FOR_ADVANCED)
                        {
                            tvProgress.setText(String.format("Ваш уровень - Advanced (%d%%)",progress));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private class ClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id==R.id.btn_general_test)
            {
                startActivity(new Intent(MyAppActivity.this, AdaptiveTest.class));
            }
            else if(id==R.id.btn_beginner_lessons)
            {
                Intent intent = new Intent(MyAppActivity.this,LessonActivity.class);
                intent.putExtra("LessonLevel","Beginner");
                startActivity(intent);
            }
            else if (id==R.id.btn_intermediate_lessons)
            {
                Intent intent = new Intent(MyAppActivity.this,LessonActivity.class);
                intent.putExtra("LessonLevel","Intermediate");
                startActivity(intent);
            }
            else if (id==R.id.btn_advanced_lessons)
            {
                Intent intent = new Intent(MyAppActivity.this,LessonActivity.class);
                intent.putExtra("LessonLevel","Advanced");
                startActivity(intent);
            }
            else if (id==R.id.btn_vocabular_fl)
            {
                startActivity(new Intent(MyAppActivity.this, VocabularyActivity.class));
            }
            else if (id==R.id.btn_chat)
            {
                startActivity(new Intent(MyAppActivity.this,AvailableChats.class));
            }
        }
    }
}