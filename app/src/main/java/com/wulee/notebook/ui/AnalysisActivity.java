package com.wulee.notebook.ui;


import android.content.Intent;
import android.os.Bundle;

import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.wulee.notebook.R;
import com.wulee.notebook.bean.Note;



/**
 * Created by dell on 2018/4/7.
 */

public class AnalysisActivity extends BaseActivity implements View.OnClickListener{

    private Note note;
    private TextView analysis_sensi;
    private TextView analysis_content;
    private double sentiment;
    private Button recombt;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        initView();
        addListener();
    }

    private void initView() {
        Toolbar toolbarUser;
        toolbarUser = (Toolbar) findViewById(R.id.toolbar_analysis);
        setSupportActionBar(toolbarUser);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarUser.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setTitle("内容分析");
        recombt = findViewById(R.id.analysis_recom);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("data");
        note = (Note) bundle.getSerializable("note");

        sentiment = note.getPositiveSentiment();
        String positive = String.valueOf(sentiment);
        String negative = String.valueOf(1 - sentiment);

        analysis_sensi = findViewById(R.id.analysis_sensi);
        analysis_sensi.setText("positive:" + positive + "\n" + "negative:" + negative);

        analysis_content = findViewById(R.id.analysis_content);
        analysis_content.setText(note.contentAbstract);
    }

    private void addListener() {
        recombt.setOnClickListener((View.OnClickListener) this);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.analysis_recom:
                final Intent intent = new Intent(AnalysisActivity.this, RecommendActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("note", note);
                intent.putExtra("data", bundle);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
