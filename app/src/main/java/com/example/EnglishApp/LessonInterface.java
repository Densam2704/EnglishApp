package com.example.EnglishApp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.example.EnglishApp.models.VocabularyElement;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.EnglishApp.ui.main.SectionsPagerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

public class LessonInterface extends AppCompatActivity {
    DatabaseReference currUserVocabulary;
    FirebaseDatabase db;
    FirebaseAuth auth;


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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this,
                getSupportFragmentManager(),intent.getStringExtra("lessonLevel"),
                intent.getIntExtra("lessonNumber",0));
        String lessonLevel = intent.getStringExtra("lessonLevel");

        //в зависимости от выбранного уровня, устанавливается своя цветовая тема
        if (lessonLevel.equals("Beginner"))
        {
            setTheme(R.style.AppThemeBeginner);
        }
        if (lessonLevel.equals("Intermediate"))
        {
            setTheme(R.style.AppThemeIntermediate);
        }
        if (lessonLevel.equals("Advanced"))
        {
            setTheme(R.style.AppThemeAdvanced);
        }
        setContentView(R.layout.activity_lesson_interface);
        ViewPager viewPager = findViewById(R.id.view_pager);
        //в зависимости от выбранного уровня, устанавливается своя цветовая тема
        if (lessonLevel.equals("Beginner"))
        {
            viewPager.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBackgroundBeginner));
        }
        if (lessonLevel.equals("Intermediate"))
        {
            viewPager.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBackgroundIntermediate));
        }
        if (lessonLevel.equals("Advanced"))
        {
            viewPager.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBackgroundAdvanced));
        }
        auth = FirebaseAuth.getInstance();
        db= FirebaseDatabase.getInstance();
        currUserVocabulary = db.getReference("Vocabularies").child(auth.getCurrentUser().getUid());
        getSupportActionBar().setTitle(intent.getStringExtra("lessonTitle"));

        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.right_in,R.anim.right_out);
    }
}