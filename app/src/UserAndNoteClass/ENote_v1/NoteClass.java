package ENote_v1;
import com.qcloud.Utilities.Json.JSONObject;
import javax.crypto.spec.SecretKeySpec;
import com.qcloud.Utilities.Json.JSONArray;

public class NoteClass {
    String content;
    String title;
    JSONObject contentAbstract;
    SecretKeySpec key;
    boolean encryptTag; //1 if secret Note,should be encryped by AES
    JSONObject sentiment;

    public NoteClass(String ct,String tt,boolean encryptTag){
        RandomString rs = new RandomString();
        String seed = rs.getRandomString(4); //seed为一个长4的随机串
        this.key = AESUtil.getSecretKey(seed); // generate AES key
        this.encryptTag = encryptTag; // encrypt tag
        this.reviseTitle(tt); // title
        this.reviseContent(ct); // 该调用包含了文本修改并加密与情感分析与主题分析模块
    }
    public void reviseContent(String ct){
        if (this.encryptTag) {this.content = AESUtil.encrypt(ct,this.key);} // Encrypt if private note
        else {this.content = ct;} // Not encrypt if not a private Note
        this.generateAbstract(ct);
        this.generateSentiment(ct);
    }
    // 产生情感分析
    private void generateSentiment(String ct){
        this.sentiment = ENote_v1.qcloudapi.src.TextSentiment.sentiment(ct);
    }
    //产生文本分类（摘要）
    private void generateAbstract(String ct){
        this.contentAbstract = ENote_v1.qcloudapi.src.TextAbstract.CTabstract(ct,this.title);
    }
    //修改标题
    public void reviseTitle(String tt){
        this.title = tt;
    }
    //获得内容
    public String getContent(boolean adminTag){
        if (encryptTag && adminTag) {return AESUtil.decrypt(this.content,this.key); } //secret note
        else {return this.content;} // not secret note
    }
    //获得标题
    public String getTitle(){
        return this.title;
    }
    //获得正面情感的分数，正面+负面=1
    public double getPositiveSentiment(){
        return this.sentiment.getDouble("positive");
    }
    //获得两篇文章的相似程度
    public double contentSimilarity(NoteClass nc){
        JSONObject jb2 = nc.contentAbstract;
        JSONObject jb1 = this.contentAbstract;
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

    // 搜索内容，包括标题，加密文章需要登陆后才能搜索到
    public boolean contentSearch(String keyword, boolean adminTag){
        if (adminTag && this.encryptTag){
            String dcct = AESUtil.decrypt(this.content,this.key);
            if (dcct.contains(keyword) || this.title.contains(keyword)){return true;}
        }
        else if (this.encryptTag){
            return false;
        }
        else{
            if (this.content.contains(keyword)|| this.title.contains(keyword)){return true;}
        }
        return false;
    }

    public double sentimentSimilarity(NoteClass nc){
        double t1 = nc.getPositiveSentiment();
        return Math.abs(t1 - this.getPositiveSentiment());
    }

    // 是否加密了
    public boolean isEncryptTag(){ return encryptTag;}

}
