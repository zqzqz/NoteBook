package com.wulee.notebook.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.wulee.notebook.R;
import com.wulee.notebook.adapter.EventDecorator;
import com.wulee.notebook.bean.Note;
import com.wulee.notebook.bean.UserInfo;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobUser;




public class UserActivity extends BaseActivity {

    private TextView tv_user_name;
    private MaterialCalendarView mcv;
    private CalendarDay currentdate;
    private List<CalendarDay> calendarDays;
    private List<Note> noteList;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        initView();
    }

    private void initView() {
        Toolbar toolbarUser;
        toolbarUser = (Toolbar) findViewById(R.id.toolbar_user);
        setSupportActionBar(toolbarUser);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarUser.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setTitle("用户信息");
        UserInfo user = BmobUser.getCurrentUser(UserInfo.class);
        String myUserName = user.getUsername();
        tv_user_name =  findViewById(R.id.tv_user_name);//标题
        tv_user_name.setTextIsSelectable(true);
        tv_user_name.setText(myUserName);

        calendarDays = new ArrayList<CalendarDay> ();
        Intent intent = this.getIntent();
        noteList = (List<Note>)intent.getSerializableExtra("noteList");
        for(int i = 0;i < noteList.size();i++){
            String date = noteList.get(i).getUpdatedAt();
            CalendarDay day = CalendarDay.from(Integer.parseInt(date.substring(0,4)),
                    Integer.parseInt(date.substring(5,7))-1,
                    Integer.parseInt(date.substring(8,10)));
            calendarDays.add(day);

        }

        mcv = (MaterialCalendarView)findViewById(R.id.calendarView);
        mcv.state().edit()
                .setCalendarDisplayMode(CalendarMode.MONTHS)//设置显示模式，显示月的模式
                .commit();// 返回对象并保存
        mcv.addDecorator(new EventDecorator(Color.RED,calendarDays));
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
