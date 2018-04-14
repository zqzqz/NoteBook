package ENote_v1;


public class NoteTest {
    public static void main(String[] args){
        //test for user
        int gender = 1;
        int age = 20;
        String name = "userzjl";
        String password = "password";
        UserClass u = new UserClass(name,age,gender,password);
        u.reviseAge(23);
        u.reviseGender(0);
        u.reviseUserName("usermyd");
        System.out.println(u.adminPermit("pd"));
        u.revisePassWord("password","password2");
        System.out.println(u.adminPermit("password2"));
        System.out.println(u.getAge());
        System.out.println(u.getGender());
        System.out.println(u.getUserName());
        System.out.println(u.passwordHash);

        System.out.println("\n");

        //test for note
        String content1 = "现在信安作业实在是太多了。这个嵌入式的报告大概有一万份，还有这个" +
                "huanfei老师的三分报告还没做，还有这个孟老师的报告也没写，作业实在太多了，服了服了。";
        String title1 = "作业太多了";
        boolean tag1 = true;
        String content2 = "昨天我去美国玩，今天我到英国玩，过两天去新加坡，再几天去日本，然后……然后梦就醒了" +
                "梦里什么都有，表面上很开心，但嵌入式的报告还没写，唉，嵌入式老师的报告是真的多，唉。";
        String title2 = "做梦";
        boolean tag2 = false;
        NoteClass n1 = new NoteClass("1",title1,tag1);
        n1.reviseContent(content1);
        NoteClass n2 = new NoteClass(content2,title2,tag2);
        System.out.println(n1.getContent(u.adminPermit("password2")));
        System.out.println(n1.getContent(u.adminPermit("password")));
        System.out.println(n1.getPositiveSentiment());
        System.out.println(n2.getTitle());
        System.out.println(n2.getContent(false));
        System.out.println(n2.getPositiveSentiment());
        System.out.println(n1.contentAbstract);
        System.out.println(n2.contentAbstract);
        System.out.println(n1.contentSimilarity(n2));
        System.out.println(n1.sentimentSimilarity(n2));
        System.out.println(n1.contentSearch("报告",false));
        System.out.println(n2.contentSearch("报告",false));
    }
}
