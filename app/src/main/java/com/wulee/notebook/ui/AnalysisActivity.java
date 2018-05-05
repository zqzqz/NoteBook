package com.wulee.notebook.ui;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.wulee.notebook.R;
import com.wulee.notebook.bean.Note;

import com.qcloud.Utilities.Json.JSONObject;
import com.qcloud.Utilities.Json.JSONArray;

import java.text.DecimalFormat;
import java.util.ArrayList;


/**
 * Created by dell on 2018/4/7.
 */

public class AnalysisActivity extends BaseActivity implements View.OnClickListener{

    private Note note;
    private HorizontalBarChart mChart;
    private PieChart pie;
    private double sentiment;
    private Button recombt;
    private JSONArray pie_json;
    private TextView tip;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        initView();
        addListener();
        NewActivity.test_a.finish();
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
        tip = findViewById(R.id.tip);
        recombt = findViewById(R.id.analysis_recom);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("data");
        note = (Note) bundle.getSerializable("note");

        sentiment = note.getPositiveSentiment();

        if(sentiment>0.7)tip.setText("小提示：又是元气满满的一天");
        else if(sentiment<0.4)tip.setText("小提示：人生不如意，可还要继续加油哦");
        else tip.setText("小提示：平平淡淡才是真");

        mChart = findViewById(R.id.analysis_sensi);
        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(true);
        mChart.setMaxVisibleValueCount(10);
        mChart.setPinchZoom(false);
        mChart.setDrawGridBackground(false);
        mChart.getDescription().setEnabled(false);
        mChart.setFitBars(true);

        //x轴
        XAxis xl = mChart.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setDrawAxisLine(true);
        xl.setDrawGridLines(false);
        xl.setAxisMaximum(1.3f);
        xl.setAxisMinimum(-0.5f);
        xl.setTextSize(12f);


        //y轴
        YAxis yl = mChart.getAxisLeft();
        yl.setDrawAxisLine(true);
        yl.setDrawGridLines(true);
        yl.setAxisMinimum(0f);

        //y轴
        YAxis yr = mChart.getAxisRight();
        yr.setDrawAxisLine(true);
        yr.setDrawGridLines(true);
        yr.setAxisMinimum(0f);

        setMChart(sentiment);
        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setFormSize(1f);
        l.setXEntrySpace(0.1f);

        pie = findViewById(R.id.piechart);
        pie.setHoleRadius(58f);
        pie.setHoleColor(Color.WHITE);
        pie.setHighlightPerTapEnabled(false);//点击不响应
        pie.setCenterText("");
        pie.setEntryLabelColor(Color.BLACK);
        pie.setEntryLabelTextSize(20f);
        pie.getDescription().setEnabled(false);

        JSONObject jc = new JSONObject(note.contentAbstract);
        pie_json = jc.getJSONArray("classes");
        pie.setData(initData(pie_json));

    }

    private void setMChart(double sentiment){
        float barWidth = 0.5f;
        float spaceForBar = 0.6f;
        final ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

        yVals1.add(new BarEntry(0, (float) sentiment));
        yVals1.add(new BarEntry(spaceForBar, (float) (1-sentiment)));

        BarDataSet set1;
        set1 = new BarDataSet(yVals1, "");
        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(set1);
        BarData data = new BarData(dataSets);
        data.setValueTextSize(12f);
        data.setValueFormatter(new MyValueFormatter());

        data.setBarWidth(barWidth);
        mChart.setData(data);

        XAxis xl = mChart.getXAxis();
        xl.setValueFormatter(new MyCustomXAxisValueFormatter());
    }

    public class MyValueFormatter implements IValueFormatter {

        private DecimalFormat mFormat;

        public MyValueFormatter() {
            mFormat = new DecimalFormat("#0.000"); // use one decimal
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            // write your logic here
            return mFormat.format(value); // e.g. append a dollar-sign
        }
    }

    public class MyCustomXAxisValueFormatter implements IAxisValueFormatter {
        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            if(value==0)return "Positive";
            else if(value==0.6f) return "Negative";
            else return "";
        }
        public int getDecimalDigits() {
            return 0;
        }
    }

    private PieData initData(JSONArray pie_json){
        ArrayList<PieEntry> Vals = new ArrayList<>();
        JSONObject tmp;

        for(int i=0;i<pie_json.length();i++){
            tmp = pie_json.getJSONObject(i);
            Vals.add(new PieEntry((float)tmp.getDouble("conf")*100,tmp.getString("class")));
        }

        PieDataSet pieDataSet = new PieDataSet(Vals, "");

        ArrayList colors =new ArrayList<>();  //控制不同绘制区域的颜色
        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);
        pieDataSet.setColors(colors);

        PieData data =new PieData(pieDataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextColor(Color.BLACK);
        data.setValueTextSize(20f);
        return data;

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
        final Intent intent = new Intent(AnalysisActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
