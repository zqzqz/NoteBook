package com.wulee.notebook.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton;

import com.wulee.notebook.R;
import com.wulee.notebook.bean.Note;
import com.wulee.notebook.bean.UserInfo;
import com.wulee.notebook.db.NoteDao;
import com.wulee.notebook.utils.CommonUtil;
import com.wulee.notebook.utils.DateUtils;
import com.wulee.notebook.utils.ImageUtils;
import com.wulee.notebook.utils.SDCardUtil;
import com.wulee.notebook.utils.ScreenUtils;
import com.wulee.notebook.utils.StringUtils;
import com.wulee.notebook.xrichtext.RichTextEditor;
import com.wulee.notebook.utils.CryptoUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import me.iwf.photopicker.PhotoPicker;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 新建笔记
 */
public class NewActivity extends BaseActivity {

    private EditText et_new_title;
    private RichTextEditor et_new_content;
    private TextView tv_new_time;
    private Switch encrypt_flag;
    private EditText encrypt_key;

    private NoteDao noteDao;
    private Note note;//笔记对象
    private String myTitle;
    private String myContent;

    private String myNoteTime;
    private int flag;//区分是新建笔记还是编辑笔记

    private static final int cutTitleLength = 20;//截取的标题长度

    private ProgressDialog loadingDialog;
    private ProgressDialog insertDialog;
    private int screenWidth;
    private int screenHeight;
    private Subscription subsLoading;
    private Subscription subsInsert;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);
        initView();
    }

    private void initView() {
        Toolbar toolbar =  findViewById(R.id.toolbar_new);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //toolbar.setNavigationIcon(R.drawable.ic_dialog_info);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dealwithExit();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_new);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        noteDao = new NoteDao(this);
        note = new Note();

        screenWidth = ScreenUtils.getScreenWidth(this);
        screenHeight = ScreenUtils.getScreenHeight(this);

        insertDialog = new ProgressDialog(this);
        insertDialog.setMessage("正在插入图片...");
        insertDialog.setCanceledOnTouchOutside(false);

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("图片解析中...");
        loadingDialog.setCanceledOnTouchOutside(false);

        et_new_title = findViewById(R.id.et_new_title);
        et_new_content = findViewById(R.id.et_new_content);
        tv_new_time = findViewById(R.id.tv_new_time);
        encrypt_flag = findViewById(R.id.encrypt);
        encrypt_key = findViewById(R.id.encryptKey);



        Intent intent = getIntent();
        flag = intent.getIntExtra("flag", 0);//0新建，1编辑
        if (flag == 1) {//编辑
            Bundle bundle = intent.getBundleExtra("data");
            note = (Note) bundle.getSerializable("note");

            myTitle = note.getTitle();
            myContent = note.getContent();
            myNoteTime = note.getUpdatedAt();


            setTitle("编辑笔记");
            tv_new_time.setText(note.getUpdatedAt());
            et_new_title.setText(note.getTitle());
            encrypt_flag.setVisibility(View.INVISIBLE);
            encrypt_key.setVisibility(View.INVISIBLE);
            et_new_content.post(new Runnable() {
                @Override
                public void run() {
                    //showEditData(note.getContent());
                    et_new_content.clearAllLayout();
                    showDataSync(note.getContent());
                }
            });
        } else {
            setTitle("新建笔记");
            myNoteTime = DateUtils.date2string(new Date());
            tv_new_time.setText(myNoteTime);
            if (note.getIsEncrypt() > 0) {
                encrypt_flag.setChecked(true);
                encrypt_key.setVisibility(View.VISIBLE);
            } else {
                encrypt_flag.setChecked(false);
                encrypt_key.setVisibility(View.INVISIBLE);
            }

            encrypt_flag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        encrypt_key.setVisibility(View.VISIBLE);
                    } else {
                        encrypt_key.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }
    }

    /**
     * 异步方式显示数据
     *
     * @param html
     */
    private void showDataSync(final String html) {
        loadingDialog.show();

        subsLoading = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                showEditData(subscriber, html);
            }
        })
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())//生产事件在io
                .observeOn(AndroidSchedulers.mainThread())//消费事件在UI线程
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                        loadingDialog.dismiss();
                        showToast("解析错误：图片不存在或已损坏");
                    }

                    @Override
                    public void onNext(String text) {
                        if (text.contains(SDCardUtil.getPictureDir())) {
                            et_new_content.addImageViewAtIndex(et_new_content.getLastIndex(), text);
                        } else {
                            et_new_content.addEditTextAtIndex(et_new_content.getLastIndex(), text);
                        }
                    }
                });
    }

    /**
     * 显示数据
     */
    protected void showEditData(Subscriber<? super String> subscriber, String html) {
        try {
            List<String> textList = StringUtils.cutStringByImgTag(html);
            for (int i = 0; i < textList.size(); i++) {
                String text = textList.get(i);
                if (text.contains("<img")) {
                    String imagePath = StringUtils.getImgSrc(text);
                    if (new File(imagePath).exists()) {
                        subscriber.onNext(imagePath);
                    } else {
                        showToast("图片" + i + "已丢失，请重新插入！");
                    }
                } else {
                    subscriber.onNext(text);
                }

            }
            subscriber.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
            subscriber.onError(e);
        }
    }

    /**
     * 负责处理编辑数据提交等事宜，请自行实现
     */
    private String getEditData() {
        List<RichTextEditor.EditData> editList = et_new_content.buildEditData();
        StringBuffer content = new StringBuffer();
        for (RichTextEditor.EditData itemData : editList) {
            if (itemData.inputStr != null) {
                content.append(itemData.inputStr);
                //Log.d("RichEditor", "commit inputStr=" + itemData.inputStr);
            } else if (itemData.imagePath != null) {
                content.append("<img src=\"").append(itemData.imagePath).append("\"/>");
                //Log.d("RichEditor", "commit imgePath=" + itemData.imagePath);
                //imageList.add(itemData.imagePath);
            }
        }
        return content.toString();
    }

    /**
     * 保存数据,=0销毁当前界面，=1不销毁界面，为了防止在后台时保存笔记并销毁，应该只保存笔记
     */
    private void saveNoteData(final boolean isBackground) {
        String noteTitle = et_new_title.getText().toString();
        String noteContent = getEditData();
        String noteTime = tv_new_time.getText().toString();


        if (noteTitle.length() == 0) {//如果标题为空，则截取内容为标题
            if (noteContent.length() > cutTitleLength) {
                noteTitle = noteContent.substring(0, cutTitleLength);
            } else if (noteContent.length() > 0 && noteContent.length() <= cutTitleLength) {
                noteTitle = noteContent;
            }
        }

        // add tone analysis here
        //
        note.setUpdatedAt(noteTime);
        note.setTitle(noteTitle);
        note.setType(2);
        // edit background here
        note.setBgColor("#FFFFFF");
        // config encryption status here
        if (encrypt_flag.isChecked()) {
            note.setIsEncrypt(1);
            String password = encrypt_key.getText().toString();
            //UserInfo user = BmobUser.getCurrentUser(UserInfo.class);
            CryptoUtils crypto = new CryptoUtils();
            String code = "";
            try {
                code = crypto.encrypt(noteContent, password);
                noteContent = code;
            } catch (Exception e) {
                Logger.getLogger(CryptoUtils.class.getName()).log(Level.SEVERE, null, e);
                note.setIsEncrypt(0);
            }
        } else note.setIsEncrypt(0);
        note.setContent(noteContent);

        if (flag == 0) { //新建笔记
            if (noteTitle.length() == 0 && noteContent.length() == 0) {
                if (!isBackground) {
                    Toast.makeText(NewActivity.this, "请输入内容", Toast.LENGTH_SHORT).show();
                }
            } else {
                UserInfo user = BmobUser.getCurrentUser(UserInfo.class);
                note.user = user;
                showProgressBar("正在保存");
                note.save(new SaveListener<String>() {
                    @Override
                    public void done(String objectId, BmobException e) {
                        hideProgressBar();
                        if (e == null) {
                            //note.setId(objectId);
                            noteDao.insertNote(note);
                            flag = 1;//插入以后只能是编辑
                            if (!isBackground) {
                                Intent intent = new Intent();
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        } else {
                            showToast(e.getMessage());
                        }
                    }
                });
            }
        } else if (flag == 1) { //编辑笔记
            if (!noteTitle.equals(myTitle) || !noteContent.equals(myContent)
                    || !noteTime.equals(myNoteTime)) {
                noteDao.updateNote(note);
            }
            if (!isBackground) {
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_image:
                callGallery();
                break;
            case R.id.action_new_save:
                saveNoteData(false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 调用图库选择
     */
    private void callGallery() {
        //调用第三方图库选择
        PhotoPicker.builder()
                .setPhotoCount(5)//可选择图片数量
                .setShowCamera(true)//是否显示拍照按钮
                .setShowGif(true)//是否显示动态图
                .setPreviewEnabled(true)//是否可以预览
                .start(this, PhotoPicker.REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (data != null) {
                if (requestCode == 1) {
                    //处理调用系统图库
                } else if (requestCode == PhotoPicker.REQUEST_CODE) {
                    //异步方式插入图片
                    insertImagesSync(data);
                }
            }
        }
    }

    /**
     * 异步方式插入图片
     *
     * @param data
     */
    private void insertImagesSync(final Intent data) {
        insertDialog.show();

        subsInsert = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    et_new_content.measure(0, 0);
                    int width = ScreenUtils.getScreenWidth(NewActivity.this);
                    int height = ScreenUtils.getScreenHeight(NewActivity.this);
                    ArrayList<String> photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
                    //可以同时插入多张图片
                    for (String imagePath : photos) {
                        //Log.i("NewActivity", "###path=" + imagePath);
                        Bitmap bitmap = ImageUtils.getSmallBitmap(imagePath, width, height);//压缩图片
                        //bitmap = BitmapFactory.decodeFile(imagePath);
                        imagePath = SDCardUtil.saveToSdCard(bitmap);
                        //Log.i("NewActivity", "###imagePath="+imagePath);
                        subscriber.onNext(imagePath);
                    }
                    subscriber.onCompleted();
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        })
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())//生产事件在io
                .observeOn(AndroidSchedulers.mainThread())//消费事件在UI线程
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                        insertDialog.dismiss();
                        et_new_content.addEditTextAtIndex(et_new_content.getLastIndex(), " ");
                        showToast("图片插入成功");
                    }

                    @Override
                    public void onError(Throwable e) {
                        insertDialog.dismiss();
                        showToast("图片插入失败:" + e.getMessage());
                    }

                    @Override
                    public void onNext(String imagePath) {
                        et_new_content.insertImage(imagePath, et_new_content.getMeasuredWidth());
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //如果APP处于后台，或者手机锁屏，则启用密码锁
        if (CommonUtil.isAppOnBackground(getApplicationContext()) ||
                CommonUtil.isLockScreeen(getApplicationContext())) {
            saveNoteData(true);//处于后台时保存数据
        }
    }

    /**
     * 退出处理
     */
    private void dealwithExit() {
        String noteTitle = et_new_title.getText().toString();
        String noteContent = getEditData();
        String noteTime = tv_new_time.getText().toString();
        if (flag == 0) {//新建笔记
            if (noteTitle.length() > 0 || noteContent.length() > 0) {
                saveNoteData(false);
            }
        } else if (flag == 1) {//编辑笔记
            if (!noteTitle.equals(myTitle) || !noteContent.equals(myContent)
                     || !noteTime.equals(myNoteTime)) {
                saveNoteData(false);
            }
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        dealwithExit();
    }
}
