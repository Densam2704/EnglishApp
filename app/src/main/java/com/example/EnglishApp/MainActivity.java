package com.example.EnglishApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.EnglishApp.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

public class MainActivity extends AppCompatActivity {

    Button btnSignIn,btnRegister;
    RelativeLayout root;
    //Для авторизации
    FirebaseAuth auth;
    //Для подключения к бд
    FirebaseDatabase db;
    //Для работы с таблицами
    DatabaseReference users;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSignIn=findViewById(R.id.btnSignIn);
        btnRegister=findViewById(R.id.btnRegister);
        root=findViewById(R.id.root);

        auth=FirebaseAuth.getInstance();
        db=FirebaseDatabase.getInstance();
        users=db.getReference("Users");

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRegisterWindow();
            }
        });
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSignInWindow();
            }
        });

        getSupportActionBar().hide();
    }



    private void showRegisterWindow() {
        //Внутри этого окна будет отображаться всплывающее окно
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        //Если поставить сюда заголовок, то не влезут кнопки :(
        dialog.setMessage("Введите данные для регистрации");

        //Объект для получение шаблона окна
        LayoutInflater inflater = LayoutInflater.from(this);
        //Получаем шаблон окна register_window в переменную registerWindow
        View registerWindow =  inflater.inflate(R.layout.register_window,null);
        //Устанавливаем шаблон для всплывающего окна
        dialog.setView(registerWindow);

        //Android Studio автоматом ставит ставит final, которая делает переменную
        // константой.
        //Это происходит так как мы используем эти переменные внутри
        // функций-обработчиков кнопок всплывающего окна.
        final MaterialEditText email = registerWindow.findViewById(R.id.emailField);
        final MaterialEditText password = registerWindow.findViewById(R.id.passField);
        final MaterialEditText firstName = registerWindow.findViewById(R.id.firstNameField);
        final MaterialEditText secondName = registerWindow.findViewById(R.id.secondNameField);
        final MaterialEditText login = registerWindow.findViewById(R.id.loginField);

        //Установливаем кнопку отмены
        dialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Скрыли всплывающее окно
                dialogInterface.dismiss();

            }
        });

        //Устанавливаем кнопку подтверждения
        dialog.setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Ошибка при вводе почты
                if(TextUtils.isEmpty(email.getText().toString())){
                    Toast toast =  Toast.makeText(root.getContext(),
                            "Введите вашу почту",Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                //Ошибка при вводе имени
                if(TextUtils.isEmpty(firstName.getText().toString())){
                    Toast toast =  Toast.makeText(root.getContext(),
                            "Введите ваше имя",Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                //Ошибка при вводе фамилии
                if(TextUtils.isEmpty(secondName.getText().toString())){
                    Toast toast =  Toast.makeText(root.getContext(),
                            "Введите вашу фамилию",Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                //Ошибка при вводе логина
                if(TextUtils.isEmpty(login.getText().toString())){
                    Toast toast =  Toast.makeText(root.getContext(),
                            "Введите ваш логин",Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                //Ошибка при вводе пароля
                if(password.getText().toString().length()<5){
                    Toast toast =  Toast.makeText(root.getContext(),
                            "Введите пароль, который содержит более 5 символов",Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

                //Регистрация пользователя
                auth.createUserWithEmailAndPassword(email.getText().toString(),
                        password.getText().toString())
                        //Обработчик события когда пользователь успешно добавлен в БД
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                User user =  new User();
                                user.setFirstName(firstName.getText().toString());
                                user.setSecondName(secondName.getText().toString());
                                user.setEmail(email.getText().toString());
                                user.setLogin(login.getText().toString());
                                user.setPassword(password.getText().toString());
                                user.setProgress("0");

                         // В этом варианте ключ - id
                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                startActivity(new Intent(MainActivity.this,MyAppActivity.class));
                                                //завершает данную сцену
                                                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.example.EnglishApp", Context.MODE_PRIVATE);
                                                sharedPreferences.edit().putString("email",email.getText().toString()).apply();
                                                sharedPreferences.edit().putString("password",password.getText().toString()).apply();

                                                finish();
                                                Toast toast =  Toast.makeText(root.getContext(),
                                                        "Регистрация успешно завершена",Toast.LENGTH_SHORT);
                                                toast.show();

                                            }
                                        });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast toast =  Toast.makeText(root.getContext(),
                                "Ошибка регистрации"+e.getMessage(),Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            }
        });
        dialog.show();
    }


    private void showSignInWindow() {

        //Внутри этого окна будет отображаться всплывающее окно
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        //Если поставить сюда заголовок, то не влезут кнопки :(
        dialog.setMessage("Введите данные для авторизации");

        //Объект для получение шаблона окна
        LayoutInflater inflater = LayoutInflater.from(this);
        //Получаем шаблон окна register_window в переменную registerWindow
        View signInWindow =  inflater.inflate(R.layout.sign_in_window,null);
        //Устанавливаем шаблон для всплывающего окна
        dialog.setView(signInWindow);

        //Android Studio автоматом ставит ставит final, которая делает переменную
        // константой.
        //Это происходит так как мы используем эти переменные внутри
        // функций-обработчиков кнопок всплывающего окна.
        final MaterialEditText email = signInWindow.findViewById(R.id.emailField);
        final MaterialEditText password = signInWindow.findViewById(R.id.passField);
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.example.EnglishApp", Context.MODE_PRIVATE);
        final String buf_email = sharedPreferences.getString("email", null);
        final String buf_password = sharedPreferences.getString("password", null);

        if (!TextUtils.isEmpty(buf_email) && !TextUtils.isEmpty(buf_password))
        {
            email.setText(buf_email);
            password.setText(buf_password);
        }
        //Установливаем кнопку отмены
        dialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Скрыли всплывающее окно
                dialogInterface.dismiss();

            }
        });

        //Устанавливаем кнопку подтверждения
        dialog.setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Ошибка при вводе почты
                if(TextUtils.isEmpty(email.getText().toString())){
                    Toast toast =  Toast.makeText(root.getContext(),
                            "Введите вашу почту",Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

                //Ошибка при вводе пароля
                if(password.getText().toString().length()<5){
                    Toast toast =  Toast.makeText(root.getContext(),
                            "Введите пароль, который содержит более 5 символов",Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

                auth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                startActivity(new Intent(MainActivity.this,MyAppActivity.class));
                                //завершает данную сцену
                                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.example.EnglishApp", Context.MODE_PRIVATE);
                                sharedPreferences.edit().putString("email",email.getText().toString()).apply();
                                sharedPreferences.edit().putString("password",password.getText().toString()).apply();
                                Log.i("Attempt to write email",sharedPreferences.getString("email","empty"));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast toast =  Toast.makeText(root.getContext(),
                                "Ошибка авторизации" + e.getMessage(),Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            }
        });
        dialog.show();
    }
}