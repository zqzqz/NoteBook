package ENote_v1;

public class UserClass {
    String userName;
    int age;
    int gender;
    String salt; //加随机的盐,防止彩虹表
    String passwordHash; //仅存储哈希,不存储原口令
    public UserClass(String UN,int age,int gender,String password ){
        this.userName = UN;
        this.age = age;
        this.gender = gender;
        RandomString rs = new RandomString();
        this.salt = rs.getRandomString(20); //盐为一个长20的随机串
        EncryptSHA sha256er = new EncryptSHA();
        this.passwordHash = sha256er.SHA256(password + this.salt); //SHA256获得哈希
    }
    //用以探测用户输入的密码是对是错
    public boolean adminPermit(String password){
        EncryptSHA sha256er = new EncryptSHA();
        String passInputHash = sha256er.SHA256(password + this.salt);
        return (passInputHash.equals(this.passwordHash));
    }
    //用于获得或修改年龄性别和用户名
    public int getAge(){
        return this.age;
    }
    public int getGender(){
        return this.gender;
    }
    public String getUserName(){
        return this.userName;
    }
    public void reviseAge(int age){
        this.age = age;
    }
    public void reviseGender(int gender){
        this.gender = gender;
    }
    public void reviseUserName(String UN){
        this.userName = UN;
    }

    public boolean revisePassWord(String oriPass,String newPass){
        if (!adminPermit(oriPass)) {return false;}  //首先验证原来输入的密码是否正确
        else {
            RandomString rs = new RandomString();
            this.salt = rs.getRandomString(20); //盐为一个长20的随机串
            EncryptSHA sha256er = new EncryptSHA();
            this.passwordHash = sha256er.SHA256(newPass + this.salt); //SHA256获得哈希
            return true;
        }
    }

}
