package Enote_v1.qcloudapi.src;
import java.util.TreeMap;

import com.qcloud.QcloudApiModuleCenter;
import com.qcloud.Module.Cvm;
import com.qcloud.Utilities.Json.JSONObject;


public class TextAbstract {
    public static JSONObject CTabstract(String content, String title){
        /* 如果是循环调用下面举例的接口，需要从此处开始你的循环语句。切记！ */
        TreeMap<String, Object> config = new TreeMap<String, Object>();
        config.put("SecretId", "AKIDpbDkAToqtpPPK5wU6c1JQL5e2RmwqjM6");
        config.put("SecretKey", "mNn9Wiprir5B8xfCXimeQvKj2ZxCJSVZ");
        /* 请求方法类型 POST、GET */
        config.put("RequestMethod", "GET");
        /* 区域参数，可选: gz:广州; sh:上海; hk:香港; ca:北美;等。 */
        config.put("DefaultRegion", "sh");

        /*
         * 你将要使用接口所在的模块，可以从 官网->云api文档->XXXX接口->接口描述->域名
         * 中获取，比如域名：cvm.api.qcloud.com，module就是 new Cvm()。
         */
        /*
         * 示例：DescribeInstances
         * 的api文档地址：http://www.qcloud.com/wiki/v2/DescribeInstances
         */
        QcloudApiModuleCenter module = new QcloudApiModuleCenter(new com.qcloud.Module.Wenzhi(), config);

        TreeMap<String, Object> params = new TreeMap<String, Object>();
        /* 将需要输入的参数都放入 params 里面，必选参数是必填的。 */
        /* DescribeInstances 接口的部分可选参数如下 */
        params.put("content", content);
        params.put("title", title);
        /*在这里指定所要用的签名算法，不指定默认为HmacSHA1*/
        //params.put("SignatureMethod", "HmacSHA256");

        /* generateUrl方法生成请求串,可用于调试使用 */
        //System.out.println(module.generateUrl("DescribeInstances", params));
        String result = null;
        try {
            /* call 方法正式向指定的接口名发送请求，并把请求参数params传入，返回即是接口的请求结果。 */
            result = module.call("TextClassify", params);
            JSONObject json_result = new JSONObject(result);
            //System.out.println(str);
            //System.out.println(json_result);
            return json_result;
        } catch (Exception e) {
            System.out.println("error..." + e.getMessage());
            return null;
        }

    }
}