package com.wulee.notebook.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.wulee.notebook.R;


/**
 * Created by dell on 2018/4/7.
 */

public class SearchActivity extends BaseActivity implements View.OnClickListener{
    private EditText keywordText;
    private EditText dateText;
    private Button submitBtn;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
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
    }

    private void addListener() {
        submitBtn.setOnClickListener(this);
    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.submitBtn:
                String keyword = keywordText.getText().toString();
                String date = dateText.getText().toString();

                Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("keyword", keyword);
                bundle.putSerializable("date", date);
                intent.putExtra("data", bundle);
                startActivity(intent);
                break;
        }
    }
}
