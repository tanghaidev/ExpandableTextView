package com.thd.expandabletextview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button button1;
    private TextView mTv1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button1 = (Button) findViewById(R.id.bt_fun2);
        mTv1 = (TextView) findViewById(R.id.tv_des);

        //change something


        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTv1.setText(button1.getText());
            }
        });
    }
}
