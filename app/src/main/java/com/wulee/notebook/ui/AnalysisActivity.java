package com.wulee.notebook.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.wulee.notebook.R;
import com.wulee.notebook.bean.Note;

import java.util.Calendar;


/**
 * Created by dell on 2018/4/7.
 */

public class AnalysisActivity extends BaseActivity implements View.OnClickListener{

    private Note note;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

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
        setTitle("分析结果");

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("data");
        this.note = (Note) bundle.getSerializable("note");
    }

    private void addListener() {


    }


    public void onClick(View view) {
        switch (view.getId()) {
        }
    }
}
