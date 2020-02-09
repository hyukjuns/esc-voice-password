package kr.soen.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.text.TextWatcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.lang.String;

public class MainActivity extends AppCompatActivity{

    private Button mIdloginBtn;

    public int loginnum = 0;

    private EditText et;

    private SecondActivity sec = new SecondActivity();

    @Override
    // Layout을 연결하고 각 Button의 OnClickListener를 연결
    protected void onCreate(Bundle savedInstanceState) {
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mIdloginBtn = (Button)findViewById(R.id.idlogin);

        mIdloginBtn.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        System.out.println("loginnum : " + loginnum);
                        if(loginnum == 1 || loginnum == 2){
                            Intent intent = new Intent(getApplicationContext(), SecondActivity.class);
                            startActivity(intent);
                            sec.checkID(loginnum);
                        }
                        else if(loginnum == 0)
                            Toast.makeText(MainActivity.this, "아이디를 먼저 등록해 주세요", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        et = (EditText)findViewById(R.id.edittext);
        et.addTextChangedListener(idWatcher);

        mIdloginBtn.setEnabled(false);
    }

    private TextWatcher idWatcher = new TextWatcher() {

        @Override

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(s.length()>0)
                mIdloginBtn.setEnabled(true);
            else
                mIdloginBtn.setEnabled(false);
        }

        @Override

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override

        public void afterTextChanged(Editable s) {
            String inText = s.toString();

            if(inText.equals("nam"))
                loginnum = 1;
            else if(inText.equals("cho"))
                loginnum = 2;
            else
                loginnum = 0;
        }

    };

    //텍스트내용을 경로의 텍스트 파일에 쓰기
    public void WriteTextFile(String foldername, String filename, String contents){
        try{
            File dir = new File (foldername);
            //디렉토리 폴더가 없으면 생성함
            if(!dir.exists()){
                dir.mkdir();
            }
            //파일 output stream 생성
            FileOutputStream fos = new FileOutputStream(foldername+"/"+filename, true);
            //파일쓰기
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
            writer.write(contents);
            writer.flush();

            writer.close();
            fos.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {

            finish();

        }

        return super.onKeyDown(keyCode, event);

    }
}

