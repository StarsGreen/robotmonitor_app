package com.example.greenstar.robotmonitor;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends Activity {

    EditText ed0,ed1;
    Button bt0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ed0=(EditText)findViewById(R.id.editText0);
        ed1=(EditText)findViewById(R.id.editText1);
        bt0=(Button)findViewById(R.id.button0);
            bt0.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(ed0.getText().toString().equals("")&&(ed1.getText().toString().equals("")))
                    {
                        Intent intent = new Intent(MainActivity.this, MonitorActivity.class);
                        startActivity(intent);
                        MainActivity.this.finish();
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "Usr Or Password Error", Toast.LENGTH_LONG).show();
                    }
/*
                    new Thread() {
                        @Override
                        public void run() {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    try
                                    {
                                        if (ed0.getText().toString() != "") {
                                            SocketThread st = new SocketThread();
                                            if (!st.SocketConnect(ed0.getText().toString(), Integer.parseInt(ed1.getText().toString()))) {
                                                Intent intent = new Intent(MainActivity.this, MonitorActivity.class);
                                                startActivity(intent);
                                            } else {
                                                Toast.makeText(MainActivity.this, "Connection Error", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }
                                    catch(Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }.start();*/
                }  });
    }
    public boolean onTouchEvent(MotionEvent event) {
        if(null != this.getCurrentFocus()){
            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            return mInputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
        return super .onTouchEvent(event);
    }
}
