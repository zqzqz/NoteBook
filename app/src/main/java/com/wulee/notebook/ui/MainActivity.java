package com.wulee.notebook.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Context;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.wulee.notebook.R;
import com.wulee.notebook.adapter.NoteListAdapter;
import com.wulee.notebook.bean.Note;
import com.wulee.notebook.bean.UserInfo;
import com.wulee.notebook.db.NoteDao;
import com.wulee.notebook.utils.AppUtils;
import com.wulee.notebook.view.SpacesItemDecoration;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;


import java.io.Serializable;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.BmobUpdateListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.update.BmobUpdateAgent;
import cn.bmob.v3.update.UpdateResponse;
import cn.bmob.v3.update.UpdateStatus;

import static com.wulee.notebook.App.aCache;


/**
 * 描述：主界面
 */

public class MainActivity extends BaseActivity implements View.OnClickListener{

    final Context context = this;
    private String key;
    private static final String TAG = "MainActivity";
    private SwipeRefreshLayout swipeLayout;
    private RecyclerView rv_list_main;
    private TextView tvNodata;
    private FloatingActionMenu fabMenu;
    private FloatingActionButton fabCheckUpdate;
    private FloatingActionButton fabLogout;
    private FloatingActionButton fabUser;
    private NoteListAdapter mNoteListAdapter;
    private List<Note> noteList;
    private NoteDao noteDao;
    private String searchKeyword;
    private String searchDate;
    private int searchSentiment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        BmobUpdateAgent.forceUpdate(this);
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        noteDao = new NoteDao(this);

        swipeLayout = findViewById(R.id.swipeLayout);
        rv_list_main =  findViewById(R.id.rv_list_main);
        tvNodata =  findViewById(R.id.tv_nodata);
        fabMenu =  findViewById(R.id.fab_menu);
        fabCheckUpdate =  findViewById(R.id.fab_check_update);
        fabLogout =  findViewById(R.id.fab_logout);
        fabUser = findViewById(R.id.fab_user);
        rv_list_main.addItemDecoration(new SpacesItemDecoration(0));//设置item间距
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);//竖向列表
        rv_list_main.setLayoutManager(layoutManager);

        Intent intent = getIntent();
        try {
            Bundle bundle = intent.getBundleExtra("data");
            searchKeyword = (String) bundle.getSerializable("keyword");
            searchDate = (String) bundle.getSerializable("date");
            searchSentiment = (int) bundle.getSerializable("sentiment");
        } catch (Exception ex) {
            // pass
        }


        mNoteListAdapter = new NoteListAdapter(R.layout.list_item_note,noteList);
        rv_list_main.setAdapter(mNoteListAdapter);


        mNoteListAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
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
                                            Intent intent = new Intent(MainActivity.this, NoteActivity.class);
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
                    Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("note", note);
                    intent.putExtra("data", bundle);
                    startActivity(intent);
                }
            }
        });

        mNoteListAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, final int position) {
                final Note noteObj = (Note) adapter.getData().get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("提示");
                builder.setMessage("确定删除笔记？");
                builder.setCancelable(false);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String objectId = null;
                        if(noteList != null && noteList.size()>0){
                            Note noteInfo = noteList.get(position);
                            objectId = noteInfo.getId();
                        }
                        final Note note = new Note();
                        note.setObjectId(objectId);
                        final String finalObjectId = objectId;
                        note.delete(objectId,new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if(e == null){
                                    List<Note> list =  noteList;
                                    Iterator<Note> iter = list.iterator();
                                    while(iter.hasNext()){
                                        Note noteBean = iter.next();
                                        if(noteBean.equals(finalObjectId)){
                                            iter.remove();
                                            break;
                                        }
                                    }
                                    int ret = noteDao.deleteNote(noteObj.getId());
                                    if (ret > 0){
                                        showToast("删除成功");
                                        refreshNoteList(false);
                                    }
                                }else{
                                    showToast("删除失败："+e.getMessage()+","+e.getErrorCode());
                                }
                            }
                        });
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.create().show();
                return false;
            }
        });

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshNoteList(true);
            }
        });

        fabCheckUpdate.setOnClickListener(this);
        fabLogout.setOnClickListener(this);
        fabUser.setOnClickListener(this);
    }

    //刷新笔记列表
    private void refreshNoteList(final boolean isRefresh){

        BmobQuery<Note> query = generateQuery();

        if(!isRefresh){
            showProgressBar("正在加载...");
        }

        query.findObjects(new FindListener<Note>() {
            @Override
            public void done(List<Note> list, BmobException e) {
                hideProgressBar();
                if(isRefresh){
                    swipeLayout.setRefreshing(false);
                }
                if(e == null){
                    if (true) {
                        noteDao.deleteAllNote();
                        if (list != null && list.size() > 0) {
                            tvNodata.setVisibility(View.GONE);
                            for (Note note : list) {
                                noteDao.insertNote(note);
                            }
                        }
                    }
                    if (list == null || list.size() <= 0){
                            tvNodata.setVisibility(View.VISIBLE);
                    }

                    noteList = noteDao.queryNotesAll();
                    mNoteListAdapter.setNewData(noteList);
                    clearSearch();
                }else{
                    showToast(e.getErrorCode()+ ","+e.getMessage());
                }
            }
        });
    }

    /*
        generate bmob query is search is enabled: fuck! bmob does not provide unpaid user with addWhereContains !!!
     */
    private BmobQuery<Note> generateQuery() {
        UserInfo user = BmobUser.getCurrentUser(UserInfo.class);
        BmobQuery<Note> query = new BmobQuery<>();
        query.addWhereEqualTo("user", user);


        if (isSearch()) {
            BmobQuery<Note> queryDate = new BmobQuery<>();
            queryDate.addWhereEqualTo("user", user);
            BmobQuery<Note> queryTitle = new BmobQuery<>();
            queryTitle.addWhereEqualTo("user", user);
            BmobQuery<Note> queryContent = new BmobQuery<>();
            queryContent.addWhereEqualTo("user", user);
            BmobQuery<Note> querySentiment = new BmobQuery<>();
            querySentiment.addWhereEqualTo("user", user);

            if (searchKeyword.length() > 0) {
                //queryTitle.addWhereContains("title", searchKeyword);
                //queryContent.addWhereContains("content", searchKeyword);
                //queryTitle = queryTitle.or(Arrays.asList(queryTitle, queryContent));
                //query = query.and(Arrays.asList(query, queryTitle));
                queryTitle.addWhereEqualTo("title", searchKeyword);
            }

            if (searchDate.length() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                BmobQuery<Note> queryAfter = new BmobQuery<>();
                queryAfter.addWhereEqualTo("user", user);
                BmobQuery<Note> queryBefore = new BmobQuery<>();
                queryBefore.addWhereEqualTo("user", user);

                Date date  = null;

                try {
                    date = sdf.parse(searchDate + " 23:59:59");
                } catch (Exception ex) {
                    showToast("日期错误");
                }
                queryBefore.addWhereLessThanOrEqualTo("createdAt",new BmobDate(date));

                try {
                    date = sdf.parse(searchDate + " 00:00:00");
                } catch (Exception ex) {
                    showToast("日期错误");
                }
                queryAfter.addWhereGreaterThanOrEqualTo("createdAt",new BmobDate(date));

                queryDate.and(Arrays.asList(queryBefore, queryAfter));
            }

            switch (searchSentiment) {
                case 0:
                case 3:
                    break;
                case 1:
                    querySentiment.addWhereLessThan("sentiment_score", 0.5);
                    break;
                case 2:
                    querySentiment.addWhereGreaterThanOrEqualTo("sentiment_score", 0.5);
                    break;
                default:
                    break;
            }

            query.and(Arrays.asList(queryDate, queryTitle, querySentiment));
        }

        query.include("user");
        query.order("-createdAt");
        return query;
    }

    private void clearSearch() {
        searchKeyword = null;
        searchDate = null;
    }

    private boolean isSearch() {
        return  (searchKeyword != null || searchDate != null);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new_note:
                Intent intent = new Intent(MainActivity.this, NewActivity.class);
                intent.putExtra("flag", 0);
                startActivity(intent);
                break;
            case R.id.action_search:
                Intent intentSearch = new Intent(MainActivity.this, SearchActivity.class);
                intentSearch.putExtra("flag", 0);
                startActivity(intentSearch);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("确定要退出吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                aCache.put("has_login","no");

                AppUtils.AppExit(MainActivity.this);
                UserInfo.logOut();
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    /**
     * 检查更新
     */
    private void checkUpdate() {
        AndPermission.with(this)
                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE)
                .callback(new PermissionListener() {
                    @Override
                    public void onSucceed(int requestCode, List<String> grantedPermissions) {
                        BmobUpdateAgent.setUpdateListener(new BmobUpdateListener() {
                            @Override
                            public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                                // TODO Auto-generated method stub
                                if (updateStatus == UpdateStatus.Yes) {//版本有更新

                                }else if(updateStatus == UpdateStatus.No){
                                    showToast("版本无更新");
                                }else if(updateStatus==UpdateStatus.EmptyField){//此提示只是提醒开发者关注那些必填项，测试成功后，无需对用户提示
                                    showToast("请检查你AppVersion表的必填项，1、target_size（文件大小）是否填写；2、path或者android_url两者必填其中一项。");
                                }else if(updateStatus==UpdateStatus.IGNORED){
                                    showToast("该版本已被忽略更新");
                                }else if(updateStatus==UpdateStatus.ErrorSizeFormat){
                                    showToast("请检查target_size填写的格式，请使用file.length()方法获取apk大小。");
                                }else if(updateStatus==UpdateStatus.TimeOut){
                                    showToast("查询出错或查询超时");
                                }
                            }
                        });
                        BmobUpdateAgent.forceUpdate(MainActivity.this);
                    }
                    @Override
                    public void onFailed(int requestCode, List<String> deniedPermissions) {
                        if(AndPermission.hasAlwaysDeniedPermission(MainActivity.this,deniedPermissions))
                            AndPermission.defaultSettingDialog(MainActivity.this).show();
                    }
                })
                .start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_check_update:
                checkUpdate();
                break;
            case R.id.fab_logout:
                showLogoutDialog();
                break;
            case R.id.fab_user:
                Intent intentUser = new Intent(MainActivity.this, UserActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("noteList", (Serializable)noteList);
                intentUser.putExtras(bundle);
                startActivity(intentUser);
                break;
        }
    }

    private long mLastClickReturnTime = 0l; // 记录上一次点击返回按钮的时间
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                if(System.currentTimeMillis() - mLastClickReturnTime > 1000L) {
                    mLastClickReturnTime = System.currentTimeMillis();
                    showToast("再按一次退出程序");
                    return true;
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

}
