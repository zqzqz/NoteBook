package com.wulee.notebook.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;

import com.wulee.notebook.R;

import java.util.Calendar;


/**
 * Created by dell on 2018/4/7.
 */

public class SearchActivity extends BaseActivity implements View.OnClickListener{
    private EditText keywordText;
    private EditText dateText;
    private Button submitBtn;
    private Button dateBtn;
    private RadioButton radio_positive;
    private RadioButton radio_negative;
    private Calendar cal;
    private int year, month, day;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        getDate();

        initView();
        addListener();
    }

    private void initView() {
        Toolbar toolbar =  findViewById(R.id.toolbar_note);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setTitle("搜索");

        keywordText = findViewById(R.id.keywordText);
        dateText = findViewById(R.id.dateText);
        submitBtn = findViewById(R.id.submitBtn);
        dateBtn = findViewById(R.id.dateBtn);
        radio_positive = findViewById(R.id.radio_positive);
        radio_negative = findViewById(R.id.radio_negative);
    }

    private void addListener() {

        submitBtn.setOnClickListener(this);
        dateBtn.setOnClickListener(this);
    }

    private void getDate() {
        cal=Calendar.getInstance();
        year=cal.get(Calendar.YEAR);       //获取年月日时分秒
        month=cal.get(Calendar.MONTH);   //获取到的月份是从0开始计数
        day=cal.get(Calendar.DAY_OF_MONTH);
    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.dateBtn:
                DatePickerDialog.OnDateSetListener listener=new DatePickerDialog.OnDateSetListener() {

                    public void onDateSet(DatePicker arg0, int year, int month, int day) {
                        dateText.setText(String.format("%04d-%02d-%02d", year, ++month, day));      //将选择的日期显示到TextView中,因为之前获取month直接使用，所以不需要+1，这个地方需要显示，所以+1
                    }
                };
                DatePickerDialog dialog = new DatePickerDialog(SearchActivity.this, 0,listener,year,month,day);//后边三个参数为显示dialog时默认的日期，月份从0开始，0-11对应1-12个月
                dialog.show();
                break;

            case R.id.submitBtn:
                String keyword = keywordText.getText().toString();
                String date = dateText.getText().toString();
                int sentiment = 0;
                if( radio_positive.isChecked() ) sentiment = 1;
                sentiment = sentiment * 2;
                if( radio_negative.isChecked() ) sentiment = sentiment + 1;

                Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("keyword", keyword);
                bundle.putSerializable("date", date);
                bundle.putSerializable("sentiment", sentiment);
                intent.putExtra("data", bundle);
                startActivity(intent);
                break;

            default:
                break;
        }
    }
}
