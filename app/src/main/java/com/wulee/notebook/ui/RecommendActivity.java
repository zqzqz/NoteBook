package com.wulee.notebook.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.wulee.notebook.R;
import com.wulee.notebook.adapter.NoteListAdapter;
import com.wulee.notebook.bean.Note;
import com.wulee.notebook.bean.UserInfo;
import com.wulee.notebook.db.NoteDao;
import com.wulee.notebook.view.SpacesItemDecoration;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class RecommendActivity extends BaseActivity {

    final Context context = this;
    private String key;
    private Note note;
    private SwipeRefreshLayout swipe_recom1;
    private SwipeRefreshLayout swipe_recom2;
    private RecyclerView rv_list_recom1;
    private RecyclerView rv_list_recom2;
    private TextView tvNodata1;
    private TextView tvNodata2;
    private NoteListAdapter mNoteListAdapter1;
    private NoteListAdapter mNoteListAdapter2;
    private List<Note> sennoteList = new ArrayList<>();
    private List<Note> connoteList = new ArrayList<>();
    private NoteDao noteDao;
    private List<Note> noteList;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend);

        initView();
    }

    private void initView(){
        Toolbar toolbarUser;
        toolbarUser = (Toolbar) findViewById(R.id.toolbar_recommend);
        setSupportActionBar(toolbarUser);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarUser.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setTitle("推荐结果");
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("data");
        note = (Note) bundle.getSerializable("note");
        noteDao = new NoteDao(this);

        swipe_recom1 = findViewById(R.id.recom1_swipeLayout);
        swipe_recom2 = findViewById(R.id.recom2_swipeLayout);
        rv_list_recom1 =  findViewById(R.id.rv_list_recom1);
        rv_list_recom2 =  findViewById(R.id.rv_list_recom2);
        tvNodata1 =  findViewById(R.id.tv_nodata_recom1);
        tvNodata2 =  findViewById(R.id.tv_nodata_recom2);

        rv_list_recom1.addItemDecoration(new SpacesItemDecoration(0));//设置item间距
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this);
        layoutManager1.setOrientation(LinearLayoutManager.VERTICAL);//竖向列表
        rv_list_recom1.setLayoutManager(layoutManager1);

        rv_list_recom2.addItemDecoration(new SpacesItemDecoration(0));//设置item间距
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this);
        layoutManager2.setOrientation(LinearLayoutManager.VERTICAL);//竖向列表
        rv_list_recom2.setLayoutManager(layoutManager2);

        mNoteListAdapter1 = new NoteListAdapter(R.layout.list_item_note,sennoteList);
        rv_list_recom1.setAdapter(mNoteListAdapter1);
        mNoteListAdapter2 = new NoteListAdapter(R.layout.list_item_note,connoteList);
        rv_list_recom2.setAdapter(mNoteListAdapter2);


        swipe_recom1.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshNoteList(true);
            }
        });
        swipe_recom2.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshNoteList(true);
            }
        });


        mNoteListAdapter1.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                final Note note = (Note) adapter.getData().get(position);


                if (note.getIsEncrypt() > 0) {
                    // get prompts.xml view
                    LayoutInflater li = LayoutInflater.from(context);
                    View promptsView = li.inflate(R.layout.pass_alert, null);

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            context);

                    // set prompts.xml to alertdialog builder
                    alertDialogBuilder.setView(promptsView);

                    final EditText userInput = (EditText) promptsView
                            .findViewById(R.id.key);

                    // set dialog message
                    alertDialogBuilder
                            .setCancelable(false)
                            .setNegativeButton("确认",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,int id) {
                                            /** DO THE METHOD HERE WHEN PROCEED IS CLICKED*/
                                            key = (userInput.getText()).toString();

                                            /** CHECK FOR USER'S INPUT **/
                                            // check password here
                                            Intent intent = new Intent(RecommendActivity.this, NoteActivity.class);
                                            Bundle bundle = new Bundle();
                                            bundle.putSerializable("key", key);
                                            bundle.putSerializable("note", note);
                                            intent.putExtra("data", bundle);
                                            startActivity(intent);
                                        }
                                    })
                            .setPositiveButton("取消",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,int id) {
                                            dialog.dismiss();
                                        }

                                    }

                            );

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();


                } else {
                    Intent intent = new Intent(RecommendActivity.this, NoteActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("note", note);
                    intent.putExtra("data", bundle);
                    startActivity(intent);
                }
            }
        });

        mNoteListAdapter2.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                final Note note = (Note) adapter.getData().get(position);


                if (note.getIsEncrypt() > 0) {
                    // get prompts.xml view
                    LayoutInflater li = LayoutInflater.from(context);
                    View promptsView = li.inflate(R.layout.pass_alert, null);

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            context);

                    // set prompts.xml to alertdialog builder
                    alertDialogBuilder.setView(promptsView);

                    final EditText userInput = (EditText) promptsView
                            .findViewById(R.id.key);

                    // set dialog message
                    alertDialogBuilder
                            .setCancelable(false)
                            .setNegativeButton("确认",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,int id) {
                                            /** DO THE METHOD HERE WHEN PROCEED IS CLICKED*/
                                            key = (userInput.getText()).toString();

                                            /** CHECK FOR USER'S INPUT **/
                                            // check password here
                                            Intent intent = new Intent(RecommendActivity.this, NoteActivity.class);
                                            Bundle bundle = new Bundle();
                                            bundle.putSerializable("key", key);
                                            bundle.putSerializable("note", note);
                                            intent.putExtra("data", bundle);
                                            startActivity(intent);
                                        }
                                    })
                            .setPositiveButton("取消",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,int id) {
                                            dialog.dismiss();
                                        }

                                    }

                            );

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();


                } else {
                    Intent intent = new Intent(RecommendActivity.this, NoteActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("note", note);
                    intent.putExtra("data", bundle);
                    startActivity(intent);
                }
            }
        });

    }

    private void refreshNoteList(final boolean isRefresh){
        UserInfo user = BmobUser.getCurrentUser(UserInfo.class);
        BmobQuery<Note> query = new BmobQuery<>();
        query.addWhereEqualTo("user",user);
        query.include("user");
        query.order("-createdAt");
        if(!isRefresh){
            showProgressBar("正在加载...");
        }

        query.findObjects(new FindListener<Note>() {
            @Override
            public void done(List<Note> list, BmobException e) {
                hideProgressBar();
                if(isRefresh){
                    swipe_recom1.setRefreshing(false);
                    swipe_recom2.setRefreshing(false);
                }
                if(e == null){
                    if(list != null && list.size()>0){
                        tvNodata1.setVisibility(View.GONE);
                        tvNodata2.setVisibility(View.GONE);
                    }else{
                        tvNodata1.setVisibility(View.VISIBLE);
                        tvNodata2.setVisibility(View.VISIBLE);
                    }

                    //推荐选取
                    int len = list.size();
                    if(len <= 1){
                        sennoteList.clear();
                        connoteList.clear();
                    }
                    else{
                        int maxindex1 = 0;
                        int maxindex2 = 0;
                        double similarity1;
                        double similarity2;
                        double maxsimilarity1 = 2;
                        double maxsimilarity2 = 0;
                        for(int i=0;i<len;i++){
                            Note noteone = list.get(i);
                            if(note.getContent().equals(noteone.getContent())){continue;}
                            similarity1 = note.sentimentSimilarity(noteone);
                            similarity2 = note.contentSimilarity(noteone);
                            if(similarity1<maxsimilarity1){
                                maxsimilarity1 = similarity1;
                                maxindex1 = i;
                            }
                            if(similarity2>maxsimilarity2){
                                maxsimilarity2 = similarity2;
                                maxindex2 = i;
                            }
                        }

                        sennoteList.clear();
                        sennoteList.add(list.get(maxindex1));
                        connoteList.clear();
                        connoteList.add(list.get(maxindex2));

                    }

                    mNoteListAdapter1.setNewData(sennoteList);
                    mNoteListAdapter2.setNewData(connoteList);

                }else{
                    showToast(e.getErrorCode()+ ","+e.getMessage());
                }
            }
        });
    }

    @Override
    protected void onResume() {

        super.onResume();
        refreshNoteList(false);
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

