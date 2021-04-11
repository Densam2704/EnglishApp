package com.example.EnglishApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LessonActivity extends AppCompatActivity {

    DatabaseReference lessonsRef;
    FirebaseDatabase db;

    ListView list_lessons;
    ConstraintLayout l1;

    int quantity = 0;
    String lessonLevel;
    String lessons[] = new String[quantity];
    String title[] = new String[quantity];

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //в зависимости от выбранного уровня устанавливается своя цветовая тема
        lessonLevel = getIntent().getStringExtra("LessonLevel");
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
        getSupportActionBar().setTitle(lessonLevel);
        db=FirebaseDatabase.getInstance();
        setContentView(R.layout.activity_lesson);
        l1 = findViewById(R.id.lessonActivityLayout);
        list_lessons = (ListView) findViewById(R.id.listLessons) ;
        lessonsRef = db.getReference("Lessons").child(lessonLevel);
        //выводим с бд уроки по данному уровню
        lessonsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                quantity = (int)dataSnapshot.getChildrenCount();
                Log.i("qu",String.valueOf(quantity));
                lessons = new String[(int)quantity];
                title = new String[(int)quantity];
                for(int i=0;i<quantity;i++) {
                    lessons[i]="Lesson "+(i+1);
                    Log.i("Quantity of lessons ",lessons[i]);
                    Log.i("TrueorFalse",String.valueOf(dataSnapshot.hasChild("Title")));

                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        Log.i("g",String.valueOf(snapshot.getKey()));
                        if (String.valueOf(snapshot.getKey()).equals(lessons[i]))
                        {Log.i("b",String.valueOf(snapshot.getKey()));
                            for (DataSnapshot deeperSnapshot : snapshot.getChildren())
                            {
                                Log.i("b",String.valueOf(deeperSnapshot.getKey()));
                                title[i] = deeperSnapshot.getValue().toString();
                                Log.i("Children of Lesson"+(i+1), title[i]);
                            }
                        }
                    }

                    if (title[i]==null)
                        title[i]="empty";
                    lessons[i]="Lesson "+(i+1)+"\n "+title[i];
                }
                ArrayAdapter<String> adapter = new ArrayAdapter <> (LessonActivity.this,
                        android.R.layout.simple_list_item_1, lessons);
                list_lessons.setAdapter(adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        list_lessons.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(LessonActivity.this,LessonInterface.class);
                intent.putExtra("lessonLevel",lessonLevel);
                intent.putExtra("lessonNumber",i+1);
                Log.i("Stroka title",title[i]);
                intent.putExtra("lessonTitle",title[i]);
                startActivity(intent);
                overridePendingTransition(R.anim.left_in,R.anim.left_out);
            }
        });
        //здесь тоже цветовая тема устанавливется для списка
        if (lessonLevel.equals("Beginner"))
        {
            list_lessons.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBackgroundBeginner));
            l1.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBackgroundBeginner));

        }
        if (lessonLevel.equals("Intermediate"))
        {
            list_lessons.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBackgroundIntermediate));
             l1.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBackgroundIntermediate));
        }
        if (lessonLevel.equals("Advanced"))
        {
            list_lessons.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBackgroundAdvanced));
            l1.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBackgroundAdvanced));
        }
    }
}