package com.example.EnglishApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.daasuu.bl.ArrowDirection;
import com.daasuu.bl.BubbleLayout;
import com.example.EnglishApp.models.ChatClass;
import com.example.EnglishApp.models.Message;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

//Здесь писали вручную, т.к. автоматом подключает не то
import android.text.format.DateFormat;
import android.widget.Toast;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private FirebaseListAdapter<Message> adapter;
    private FloatingActionButton sendBtn;
    DatabaseReference messages;
    FirebaseAuth auth;
    DatabaseReference chats;
    FirebaseDatabase db;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ChatActivity.this,AvailableChats.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getSupportActionBar().setTitle("Личный чат");
        Intent intent = getIntent();
        final ArrayList<String> userIds = new ArrayList<>();
        auth=FirebaseAuth.getInstance();
        db=FirebaseDatabase.getInstance();
        chats=db.getReference("Chats");

        //если чат только создан, то отправить его в бд
        if(intent.getStringExtra("chatWith")!=null) {
            userIds.add(intent.getStringExtra("chatWith"));
            userIds.add(intent.getStringExtra("organizer"));

            chats.child("chat " + userIds.get(0) + " " + userIds.get(1)).
                    setValue(
                            new ChatClass(
                                    userIds.get(0), userIds.get(1)
                            ));
        }
        else {
            userIds.add(intent.getStringExtra("chatWith2"));
            userIds.add(intent.getStringExtra("organizer2"));
        }
        //отправка сообщений в бд и вывод их на экран
        chats.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

               messages = chats.child("chat " + userIds.get(0)+" "+userIds.get(1)).child("Messages");
               sendBtn=findViewById(R.id.btnSend);
               //Обработчик события
               sendBtn.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {
                       EditText textField = findViewById(R.id.messageField);
                       //Если сообщение пустое, то не обрабатываем
                       if (textField.getText().toString().equals(""))
                           return;

                       //Обращаемся к БД
                       messages.
                               //Заносим в БД новое сообщение
                                       push().setValue(
                               new Message(
                                       FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                                       textField.getText().toString()
                               )
                       );
                       //Очищаем строку
                       textField.setText("");
                   }
               });
               displayAllMessages();
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {
           }
       });
    }

    //Получает данные из БД и добавляет их в список
    private void displayAllMessages() {
        ListView listOfMessages =findViewById(R.id.listOfMessages);
        //Реализуем объект адаптер. Параметры: Где отображать, класс с которым работаем,
        // дизайн элементов, ссылка на подключение с БД
        adapter = new FirebaseListAdapter<Message>(this,Message.class,R.layout.message_list_item,
                messages) {
            @Override
            //работаем внутри окна v
            protected void populateView(View v, Message model, int position) {
                TextView mess_user, mess_time,mess_text;
                mess_user=v.findViewById(R.id.message_user);
                mess_text=v.findViewById(R.id.message_text);
                mess_time=v.findViewById(R.id.message_time);

                if (model.getUserName().equals(auth.getCurrentUser().getEmail())){
                    BubbleLayout layout = v.findViewById(R.id.bubbleLayout);
                    layout.setBubbleColor(getColor(R.color.colorBackgroundMyMessage));
                    layout.setArrowDirection(ArrowDirection.RIGHT);
                    mess_user.setTextColor(getColor(R.color.colorTextMyMessage));
                    mess_text.setTextColor(getColor(R.color.colorTextMyMessage));
                    mess_time.setTextColor(getColor(R.color.colorTextMyMessage));
                    mess_user.setText(model.getUserName() + " (Вы)");
                }else{
                    BubbleLayout layout = v.findViewById(R.id.bubbleLayout);
                    layout.setBubbleColor(getColor(R.color.colorBackgroundSomeonesMessage));
                    layout.setArrowDirection(ArrowDirection.LEFT);
                    mess_user.setTextColor(getColor(R.color.colorTextSomeonesMessage));
                    mess_text.setTextColor(getColor(R.color.colorTextSomeonesMessage));
                    mess_time.setTextColor(getColor(R.color.colorTextSomeonesMessage));
                    mess_user.setText(model.getUserName());
                }

                mess_text.setText(model.getTextMessage());
                mess_time.setText(DateFormat.format("dd-MM-yyyy HH:mm:ss",
                        model.getMessageTime()));
            }
        };

        //назначаем списку адаптер
        listOfMessages.setAdapter(adapter);
        listOfMessages.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final int itemToDelete = i;

                new AlertDialog.Builder(ChatActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Вы уверены?")
                        .setMessage("Вы действительно хотите удалить эту запись?")
                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.example.workingwithfirebase", Context.MODE_PRIVATE);
                                String buf_email = sharedPreferences.getString("email", null);
                                if(adapter.getItem(itemToDelete).getUserName().equals(buf_email))
                                {
                                    adapter.getRef(itemToDelete).removeValue();
                                    Toast logon = Toast.makeText(ChatActivity.this,"Вы удалили сообщение",Toast.LENGTH_SHORT);
                                    logon.setGravity(Gravity.TOP,0,150);
                                    logon.show();
                                }
                                else {
                                    Toast logon = Toast.makeText(ChatActivity.this,"Нельзя удалять чужие сообщения",Toast.LENGTH_SHORT);
                                    logon.setGravity(Gravity.TOP,0,150);
                                    logon.show();
                                }
                            }
                        })
                        .setNegativeButton("Нет",null)
                        .show();
                return true;
            }
        });
    }
}