package com.example.EnglishApp.ui.main;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.example.EnglishApp.models.Questions;
import com.example.EnglishApp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Tab1Rule#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Tab2Test extends Fragment// implements View.OnClickListener
{

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String LESSON_LEVEL_PARAM = "lessonLevel";
    private static final String LESSON_NUMBER_PARAM = "lessonNumber";

    private String lessonLevel;
    private int lessonNumber;

    DatabaseReference questionsRef;
    FirebaseDatabase db;
    ArrayList<Questions> questions = new ArrayList<>();
    Button v1,v2,v3,v4,start;
    TextView questionText,qn1;
    int number_of_question = 0;
    int rightAnswers = 0;
    private Animation animation = null;
    int quantity = 0;

    public Tab2Test() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param lessonLevelParam Parameter 1.
     * @param lessonNumberParam Parameter 2.
     * @return A new instance of fragment Tab1Rule.
     */
    // TODO: Rename and change types and number of parameters
    public static Tab2Test newInstance(String lessonLevelParam, int lessonNumberParam) {
        Tab2Test fragment = new Tab2Test();
        Bundle args = new Bundle();
        args.putString(LESSON_LEVEL_PARAM, lessonLevelParam);
        args.putInt(LESSON_NUMBER_PARAM, lessonNumberParam);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            lessonLevel = getArguments().getString(LESSON_LEVEL_PARAM);
            lessonNumber = getArguments().getInt(LESSON_NUMBER_PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       final View root = inflater.inflate(R.layout.tab2_test, container, false);

        v1 = (Button) root.findViewById(R.id.v_1);
        v2 = (Button) root.findViewById(R.id.v_2);
        v3 = (Button) root.findViewById(R.id.v_3);
        v4 = (Button) root.findViewById(R.id.v_4);
        questionText = (TextView) root.findViewById(R.id.question_text);
        qn1 =(TextView) root.findViewById(R.id.question_number);
        start = (Button) root.findViewById(R.id.start);
        //по нажатию по кнопку старт, начнется тест
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextQuestion();
                root.findViewById(R.id.grid_view).setVisibility(View.VISIBLE);
                start.setVisibility(View.GONE);
                qn1.setVisibility(View.VISIBLE);
                questionText.setVisibility(View.VISIBLE);
                animation = AnimationUtils.loadAnimation(
                        getContext(), android.R.anim.fade_in);
                root.findViewById(R.id.grid_view).startAnimation(animation);
                qn1.startAnimation(animation);
                questionText.startAnimation(animation);
            }
        });

        //слушатели для кнопок ответов
        v1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pressedAnim(view);
            }
        });
        v2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pressedAnim(view);
            }
        });
        v3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pressedAnim(view);
            }
        });
        v4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pressedAnim(view);
            }
        });

        db=FirebaseDatabase.getInstance();
        questionsRef = db.getReference("Lessons").child(lessonLevel).child("Lesson "+lessonNumber).child("Test");
        // достаем вопросы из бд
        questionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                quantity = (int)dataSnapshot.getChildrenCount();
                Log.i("Quantity of quest",String.valueOf(quantity));
                for (int i=0;i<quantity;i++) {
                    Questions question = dataSnapshot.child("Question " + (i+1)).getValue(Questions.class);
                    questions.add(question);
                    Log.i("Question "+(i+1),String.valueOf(questions.size()));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
            });
        return root;
    }

    public void pressedAnim (View view){
        int id = view.getId();
        Handler handler = new Handler();
        Runnable r=new Runnable() {
            public void run() {
                nextQuestion();
            }
        };
        Button btn = view.findViewById(id);
        //все кнопки отключаются
        v1.setClickable(false);
        v2.setClickable(false);
        v3.setClickable(false);
        v4.setClickable(false);
        if ( questions.get(number_of_question).getRight().equals(btn.getText()) )
        {
            number_of_question++;
            rightAnswers++;
            view.findViewById(id).setBackgroundResource(R.drawable.btn_succes);
        }
        else {
            number_of_question++;
            view.findViewById(id).setBackgroundResource(R.drawable.btn_failed);
        }
        handler.postDelayed(r,1500);
    }

    public void nextQuestion()
    {
        if(number_of_question<questions.size())
        {
            v1.setBackgroundResource(R.drawable.btn_norm);
            v2.setBackgroundResource(R.drawable.btn_norm);
            v3.setBackgroundResource(R.drawable.btn_norm);
            v4.setBackgroundResource(R.drawable.btn_norm);

            qn1.setText(String.format("Question  %d  of  %d",number_of_question+1,questions.size()));

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
                    getContext(), android.R.anim.fade_out);
            getView().findViewById(R.id.grid_view).setVisibility(View.GONE);
            getView().findViewById(R.id.grid_view).startAnimation(animation);
            questionText.startAnimation(animation);
            showFinishTestWindow();

        }
        v1.setClickable(true);
        v2.setClickable(true);
        v3.setClickable(true);
        v4.setClickable(true);
    }

    private void showFinishTestWindow() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setTitle("Завершение теста");
        alertDialog.setMessage("Ваш результат:\n");

        //Объект для получение шаблона окна
        LayoutInflater inflater = LayoutInflater.from(getContext());
        //Получаем шаблон окна register_window в переменную registerWindow
        View finishTestWindow =  inflater.inflate(R.layout.finish_test_window,null);
        //Устанавливаем шаблон для всплывающего окна
        alertDialog.setView(finishTestWindow);

        TextView tvResult = finishTestWindow.findViewById(R.id.tvResult);

        float percentage =(float) rightAnswers/number_of_question *100;
        String resultString = String.format("%d/ %d (%d%%)",rightAnswers,number_of_question,(int)percentage);

        tvResult.setText(resultString);
        alertDialog.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getActivity().finish();
            }
        });
        alertDialog.show();
    }
}