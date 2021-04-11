package com.example.EnglishApp.ui.main;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.EnglishApp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Tab1Rule#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Tab1Rule extends Fragment {


    private static final String LESSON_LEVEL_PARAM = "lessonLevel";
    private static final String LESSON_NUMBER_PARAM = "lessonNumber";

    private String lessonLevel;
    private int lessonNumber;

    public Tab1Rule() {
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
    public static Tab1Rule newInstance(String lessonLevelParam, int lessonNumberParam) {
        Tab1Rule fragment = new Tab1Rule();
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
        // create ContextThemeWrapper from the original Activity Context with the custom theme
        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.AppThemeBeginner);

        // clone the inflater using the ContextThemeWrapper
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        View root = localInflater.inflate(R.layout.tab1_rule, container, false);


        final TextView tvTab1Text = root.findViewById(R.id.tvTab1Text);

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ruleRef = db.getReference("Lessons/"+lessonLevel+"/Lesson "+lessonNumber);
        Log.i("rulref",String.valueOf(ruleRef));
        ruleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Log.i("snapshot",snapshot.getKey());
                    if(snapshot.getKey().equals("Rule")){
                        tvTab1Text.setText(snapshot.getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        return root;
    }
}