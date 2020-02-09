package kr.soen.myapplication;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by esc on 2018-01-11.
 */

public class ThirdActivity extends AppCompatActivity {

    public static int loginnum = 0;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page3);

        TextView textView = (TextView)findViewById(R.id.textid);

        if(loginnum == 1)
            textView.setText("남혁준님 안녕하세요 !");
        else if(loginnum == 2)
            textView.setText("조은지님 안녕하세요 !");
    }

    public void checkID(int num){
        loginnum = num;
        System.out.println("ThirdActivity loginnum : " + loginnum);
    }
}
