package com.example.EnglishApp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.EnglishApp.models.VocabularyElement;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;


public class VocabularyActivity extends AppCompatActivity {

    private final static int MAX_LINES_COLLAPSED = 3;

    FirebaseDatabase db;
    DatabaseReference currUserVocabulary;
    FirebaseListAdapter<VocabularyElement> adapter;
    FirebaseAuth auth;
    ListView listVocabulary;
    private String context = "";
    private final String translateSite = "https://www.multitran.com/m.exe?l1=1&l2=2&s=";
    List<String> arrayTranslations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary);
        getSupportActionBar().setTitle("Словарик");

        db=FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        currUserVocabulary = db.getReference("Vocabularies").child(auth.getCurrentUser().getUid());
        listVocabulary =findViewById(R.id.listVocabulary);

        //по клику на итем расширяем его, чтобы видеть все варианты перевода
        listVocabulary.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView translation = view.findViewById(R.id.tvTranslationInRussian);
                if (translation.getMaxLines()==MAX_LINES_COLLAPSED) {
                    translation.setMaxLines(Integer.MAX_VALUE);
                } else {
                    translation.setMaxLines(MAX_LINES_COLLAPSED);
                }
            }
        });
        //по долгому клику на итем можно удалить слово
        listVocabulary.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final DatabaseReference currentField = adapter.getRef(i);
                AlertDialog.Builder dialog = new AlertDialog.Builder(VocabularyActivity.this);
                dialog.setTitle("Удалить");
                dialog.setMessage("Вы уверены?");
                dialog.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                    }
                });
                dialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override   //Удаляем из БД сообщение
                    public void onClick(DialogInterface dialogInterface, int i) {
                        currentField.removeValue();
                    }
                });
                dialog.show();
                return true;
            }
        });
        displayVocabulary();
    }

    //Вывод слов с бд
    private void displayVocabulary() {
        adapter = new FirebaseListAdapter<VocabularyElement>(VocabularyActivity.this,
                VocabularyElement.class,R.layout.vocabulary_item,currUserVocabulary) {
            @Override
            protected void populateView(View v, VocabularyElement model, int position) {
                TextView vocab_word,vocab_translation;
                vocab_word=v.findViewById(R.id.tvWordInEnglish);
                vocab_translation=v.findViewById(R.id.tvTranslationInRussian);

                final DatabaseReference currentField = adapter.getRef(position);

                vocab_word.setText(model.getWordInEnglish());
                Log.i("Translate word"+model.getWordInEnglish(),model.getTranslationInRussian());
                if(model.getTranslationInRussian().equals("empty")) {
                    DownloadTask task = new DownloadTask();
                    try {
                        context = task.execute(translateSite + model.getWordInEnglish()).get();
                        getResources(context);
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    StringBuilder translationRussian = new StringBuilder();
                    for (int i = 0; i < arrayTranslations.size(); i++){
                        translationRussian.append(arrayTranslations.get(i)).append("; ");}


                    vocab_translation.setText(translationRussian.toString());
                    currentField.//Заносим в БД перевод
                                        setValue(
                                        new VocabularyElement(
                                                model.getWordInEnglish(),
                                                translationRussian.toString()
                                        )
                                );
                }
                else {
                    vocab_translation.setText(model.getTranslationInRussian());
                }
            }
        };
        listVocabulary.setAdapter(adapter);
    }

    //по кнопке можно добавить новое слово
    public void onBtnAdd(View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("Новое слово");
        dialog.setMessage("Введите новое слово и перевод");

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
                    Toast logon = Toast.makeText(VocabularyActivity.this,"Введите слово",Toast.LENGTH_SHORT);
                    logon.setGravity(Gravity.TOP,0,250);
                    logon.show();
                    return;
                }

                DownloadTask task = new DownloadTask();
                try
                {
                    context = task.execute(translateSite +word.getText().toString()).get();
                    getResources(context);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                String array1="";
                for(int j = 0; j< arrayTranslations.size(); j++)
                    array1=array1+ arrayTranslations.get(j)+"; ";

                currUserVocabulary.push().
                        //Заносим в БД новое сообщение
                                setValue(
                        new VocabularyElement(
                                word.getText().toString(),
                                array1
                        )
                );
            }
        });
        dialog.show();
    }
    protected  void getResources(String cont)
    {//ищем перевод слова
        String  start = "l1=2&amp;l2=1\">";
        String finish = "</a>";

        Pattern pattern = Pattern.compile(start + "(.*?)" + finish);
        Matcher matcher = pattern.matcher(cont);
        String splitContent = "";
        int i =0;
        arrayTranslations.clear();
        while(matcher.find())
        {
            splitContent = matcher.group(1);
            arrayTranslations.add(splitContent);
            i++;
        }
    }

    public static class DownloadTask extends AsyncTask<String,Void, String>
    {
        @Override
        protected String doInBackground(String... strings)
        {
            StringBuilder result = new StringBuilder();
            URL url = null;
            HttpsURLConnection urlConnection = null;

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpsURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line = bufferedReader.readLine();
                while (line!=null)
                {
                    result.append(line);
                    line = bufferedReader.readLine();
                }

            } catch (IOException e)
            {
                e.printStackTrace();
            } finally
            {
                if (urlConnection!=null)
                    urlConnection.disconnect();
            }
            return  result.toString();
        }
    }
}