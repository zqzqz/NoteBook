package com.wulee.notebook.bean;

import android.graphics.Color;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import cn.bmob.v3.BmobObject;
import com.qcloud.Utilities.Json.JSONObject;
import com.qcloud.Utilities.Json.JSONArray;
import com.wulee.notebook.utils.CryptoUtils;
/**
 * 描述：笔记实体类
 */

public class Note extends BmobObject implements Serializable{

    private String id;//笔记ID
    private String title;//笔记标题
    private String content;//笔记内容
    private int type;//笔记类型，1纯文本，2Html，3Markdown
    private String bgColor;//背景颜色，存储颜色代码
    private int isEncrypt ;//是否加密，0未加密，1加密
    public UserInfo user;
    public String contentAbstract;
    public String sentiment;
    private double sentiment_score;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() { return this.content; }

    public String decodeContent(String password) {
        if (this.getIsEncrypt() > 0) {
            String plain = "";
            CryptoUtils crypto = new CryptoUtils();
            try {
                plain = crypto.decrypt(this.content, password);
                return plain;
            } catch (Exception e) {
                return "";
            }
        } else return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getBgColor() {
        return bgColor;
    }

    public void setBgColor(String bgColor) {
        try {
            this.sentiment_score = getPositiveSentiment();
            this.bgColor = generateBgColor();
        } catch (Exception e) {
            this.bgColor = bgColor;
        }
    }

    public int getIsEncrypt() {
        return isEncrypt;
    }

    public void setIsEncrypt(int isEncrypt) {
        this.isEncrypt = isEncrypt;
    }

    public void reviseContent(String ct, String password){
        if (this.isEncrypt > 0) {
            CryptoUtils crypto = new CryptoUtils();
            String code = "";
            try {
                code = crypto.encrypt(ct, password);
                this.content = code;
            } catch (Exception e) {
                this.setIsEncrypt(0);
                this.content = ct;
            }
        } // Encrypt if private note
        else {this.content = ct;} // Not encrypt if not a private Note
        this.generateAbstract(ct);
        this.generateSentiment(ct);
    }

    // 产生情感分析
    private void generateSentiment(String ct){
        final String cont = ct;
        new Thread(new Runnable() {
            public void run() {
                sentiment = com.wulee.notebook.utils.TextSentiment.sentiment(cont).toString();
            }
        }).start();
    }

    //产生文本分类（摘要）
    private void generateAbstract(String ct){
        final String cont = ct;
        new Thread(new Runnable() {
            public void run() {
                contentAbstract = com.wulee.notebook.utils.TextAbstract.CTabstract(cont,title).toString();
            }
        }).start();
    }

    //获得两篇文章的相似程度
    public double contentSimilarity(Note nc){
        JSONObject jb2 = new JSONObject(nc.contentAbstract);
        JSONObject jb1 = new JSONObject(contentAbstract);
        double similar = 0;
        JSONArray ja1 = jb1.getJSONArray("classes");
        JSONArray ja2 = jb2.getJSONArray("classes");
        int i,j;
        int num;
        double conf;
        JSONObject tmp,tmp2;

        for (i=0;i<ja1.length();i++){
            tmp = ja1.getJSONObject(i);
            num = tmp.getInt("class_num");
            if (num == 0) {continue;}
            conf = tmp.getDouble("conf");
            for (j=0;j<ja2.length();j++){
                tmp2 = ja2.getJSONObject(j);
                if (num == tmp2.getInt("class_num")){
                    similar += tmp2.getDouble("conf");
                }
            }
        }
        return similar;
    }

    //获得正面情感的分数，正面+负面=1
    public double getPositiveSentiment(){
        JSONObject senti = new JSONObject(this.sentiment);
        return senti.getDouble("positive");
    }

    public double sentimentSimilarity(Note nc){
        double t1 = nc.getPositiveSentiment();
        double t2 = this.getPositiveSentiment();
        return Math.abs(t1-t2);
    }

    public double getSentiment_score() {
        return this.sentiment_score;
    }

    public String generateBgColor() {
        if (this.sentiment_score >= 0 && this.sentiment_score <= 1) {
            int[] startColor = {255, 100, 100};
            int[] endColor = {100, 100, 255};
            int[] targetColor = new int[3];
            for (int i=0; i<3; i++) {
                targetColor[i] = (int) (endColor[i] + this.sentiment_score * (startColor[i] - endColor[i]));
            }

            int alpha = 100;
            int colorint = (alpha & 0xff) << 24 | (targetColor[0] & 0xff) << 16 | (targetColor[1] & 0xff) << 8 | (targetColor[2] & 0xff);
            return String.format("#%06X", 0xFFFFFFFF & colorint);
        } else {
            return "#FFFFFF";
        }
    }

    public void setSentiment(String s) {
        this.sentiment = s;
    }

    public void setContentAbstract(String s) {
        this.contentAbstract = s;
    }

    public String getSentiment() {
        return this.sentiment;
    }

    public String getContentAbstract() {
        return this.contentAbstract;
    }
}
