package com.example.EnglishApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AvailableChats extends AppCompatActivity {

    ArrayAdapter<String> adapterChat;
    ArrayAdapter<String> adapterUsers;
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference chatsRef;
    DatabaseReference usersRef;
    ListView listOfUsers;

    ListView listOfChats;


    //id всех пользователей
    ArrayList<String> userIds = new ArrayList<>();
    //id организатора чата
    ArrayList<String> chatOrgIds = new ArrayList<>();
    //id того, с кем начали чат
    ArrayList<String> chatWithIds = new ArrayList<>();
    //email тех, с кем начали чат
    ArrayList<String> emailOpened = new ArrayList<>();
    //email всех пользователей
    ArrayList<String> emails = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_chats);
        getSupportActionBar().setTitle("Доступные чаты");
        listOfChats=findViewById(R.id.chatsList);
        db=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
        usersRef=db.getReference("Users");
        chatsRef=db.getReference("Chats");
        displayChats();
    }

    public void onBtnStartChatting(View view) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Чат с");
        dialog.setMessage("Выберите пользователя");
        final AlertDialog alert = dialog.create();
        //Объект для получение шаблона окна
        LayoutInflater inflater = LayoutInflater.from(this);
        //Получаем шаблон окна register_window в переменную registerWindow
        View addChatWindow = inflater.inflate(R.layout.add_chat_window,null);
        //Устанавливаем шаблон для всплывающего окна
        dialog.setView(addChatWindow);

        listOfUsers = addChatWindow.findViewById(R.id.listUsers);
        displayUsers();
        dialog.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

            }
        });
        listOfUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String str = userIds.get(i);

                Intent intent = new Intent(AvailableChats.this,ChatActivity.class);
                intent.putExtra("chatWith",str);
                intent.putExtra("organizer",auth.getCurrentUser().getUid());
                startActivity(intent);
                finish();

            }
        });
        dialog.show();
    }

    //вывод пользователей, с которыми можно чатиться
    private void displayUsers() {

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userIds.clear();
                emails.clear();
                for(DataSnapshot snapshot1 : dataSnapshot.getChildren())
                {
                    Log.i("UserID",snapshot1.getKey());
                    userIds.add(snapshot1.getKey());
                    for (DataSnapshot snapshot2 : snapshot1.getChildren())
                    {
                        Log.i("Child of snapshot"+snapshot1.getKey(),snapshot2.getKey());
                        if (snapshot2.getKey().equals("email"))
                        {
                            Log.i("email",snapshot2.getValue().toString());
                            emails.add(snapshot2.getValue().toString());
                            Log.i("email added",emails.toString());
                            //emails.add(snapshot2.getValue().toString());
                            if( snapshot2.getValue().toString().equals(auth.getCurrentUser().getEmail()) )
                            {
                                emails.remove(emails.size()-1);
                                userIds.remove(userIds.size()-1);
                                Log.i("After remove emails",emails.toString());
                            }
                            for (int i = 0; i< emailOpened.size(); i++){
                                Log.i("Email opened"+i, emailOpened.get(i));
                                Log.i("userCur email",auth.getCurrentUser().getEmail());
                                if (snapshot2.getValue().toString().equals(emailOpened.get(i)))
                                //( snapshot2.getValue().toString().equals(auth.getCurrentUser().getEmail()) )
                                {
                                    emails.remove(emails.size()-1);
                                    userIds.remove(userIds.size()-1);
                                    Log.i("After remove emails",emails.toString());
                                }

                            }
                        }
                    }
                }
                Log.i("All emails",emails.toString());
                adapterUsers = new ArrayAdapter<String>(getApplicationContext(),
                        android.R.layout.simple_list_item_1, emails);
                adapterUsers.notifyDataSetChanged();
                listOfUsers.setAdapter(adapterUsers);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    private void displayChats() {
        //вывод личных чатов
        chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatOrgIds.clear();
                chatWithIds.clear();
                emailOpened.clear();
                for (DataSnapshot snapshot1:dataSnapshot.getChildren()){
                    Log.i("snapshot output", snapshot1.getKey());
                    for(DataSnapshot snapshot2:snapshot1.getChildren()){
                        Log.i("snapshot2 output",snapshot2.getKey());
                        if(snapshot2.getValue().toString().equals(auth.getCurrentUser().getUid())){
                            if (snapshot2.getKey().equals("chatWith")){
                                chatWithIds.add(snapshot2.getValue().toString());
                                chatOrgIds.add(snapshot1.child("organizer").getValue().toString());
                                Log.i("value of chatWith",snapshot2.getValue().toString());

                                db.getReference()
                                        .child("Users")
                                        .child(snapshot1.child("organizer").getValue().toString())
                                        .child("email").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        emailOpened.add(dataSnapshot.getValue().toString());
                                        adapterChat = new ArrayAdapter<String>(getApplicationContext(),
                                                android.R.layout.simple_list_item_1, emailOpened);
                                        adapterChat.notifyDataSetChanged();
                                        adapterChat.notifyDataSetChanged();
                                        listOfChats.setAdapter(adapterChat);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                            if (snapshot2.getKey().equals("organizer")){
                                chatOrgIds.add(snapshot2.getValue().toString());
                                chatWithIds.add(snapshot1.child("chatWith").getValue().toString());
                                Log.i("value of chatWith",snapshot2.getValue().toString());

                                db.getReference()
                                        .child("Users")
                                        .child(snapshot1.child("chatWith").getValue().toString())
                                        .child("email").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        emailOpened.add(dataSnapshot.getValue().toString());
                                        adapterChat = new ArrayAdapter<String>(getApplicationContext(),
                                                android.R.layout.simple_list_item_1, emailOpened);
                                        adapterChat.notifyDataSetChanged();
                                        adapterChat.notifyDataSetChanged();
                                        listOfChats.setAdapter(adapterChat);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                       }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        listOfChats.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(AvailableChats.this,ChatActivity.class);
                intent.putExtra("chatWith2",chatWithIds.get(position));
                intent.putExtra("organizer2",chatOrgIds.get(position));
                startActivity(intent);
                finish();
            }
        });

    }
    public void onBtnGeneralChat(View view) {
        Intent intent = new Intent(AvailableChats.this,GeneralChat.class);
        startActivity(intent);
    }
}