package cn.edu.hbwe.gogo_server.utils;

import com.alibaba.fastjson.JSONObject;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Component
public class WXUtil {

    @Value("${wx.AppId}")
    private String appid;

    @Value("${wx.appSecret}")
    private String appsecret;

    public String sendPostRequest(String urlStr, Map<String, String> headers, Map<String, Object> body) {
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();
            String bodyJson = JSONObject.toJSONString(body);
            os.write(bodyJson.getBytes());
            os.flush();
            os.close();

            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    public String getAccessTokenFromWX() {
        String result = "";
        String urlStr = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appid + "&secret=" + appsecret;
        try {
            Map<String, String> headers = HTTPUtil.createCommonHeaders();
            Connection.Response response = HTTPUtil.sendGetRequest(urlStr, headers, new HashMap<>());
            result = response.body();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

//    public void sendSubscribeMessage(String openId, String templateId, String page, Map<String, Map<String, String>> data01) {
//        String resultTmp = getAccessTokenFromWX();
//        // 解析JSON字符串并获取access_token字段的值
//        JSONObject jsonObject = JSONObject.parseObject(resultTmp);
//        String accessToken = jsonObject.getString("access_token");
//        String urlStr = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;
//        try {
//            // 创建请求主体
//            Map<String, String> data = new HashMap<>();
//            data.put("touser", openId);
//            data.put("template_id", templateId);
//            data.put("page", page);
//
////            // 将data01转换为Map<String, Object>
////            Map<String, Object> data01Object = new HashMap<>(data01);
////
////            // 将data01Object转换为JSON字符串
////            String data01Json = new JSONObject(data01Object).toString();
////            data.put("data", data01Json);
//
//            // 将data01转换为Map<String, Object>
//            Map<String, Object> data01Object = new HashMap<>(data01);
//            String data01Json = JSONObject.toJSONString(data01Object);
////            Map<String, Object> data01Object = new HashMap<>(data01);
//            data.put("data", data01Json);
//
//            // 发送POST请求
//            Map<String, String> headers = HTTPUtil.createCommonHeaders();
////            headers.put("Content-Type", "application/json");
//            Connection.Response response = HTTPUtil.sendPostRequest(urlStr, headers, data, new HashMap<>());
//
//            // 处理响应
//            String result = response.body();
//            // 这里可以添加处理响应的代码，例如检查响应状态码，解析响应主体等
//            System.out.println(result);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
public void sendSubscribeMessage(String openId, String templateId, String page, Map<String, Map<String, String>> data01) {
    String resultTmp = getAccessTokenFromWX();
    // 解析JSON字符串并获取access_token字段的值
    JSONObject jsonObject = JSONObject.parseObject(resultTmp);
    String accessToken = jsonObject.getString("access_token");
    String urlStr = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;
    try {
        // 创建请求主体
        Map<String, Object> data = new HashMap<>();
        data.put("touser", openId);
        data.put("template_id", templateId);
        data.put("page", page);

        // 将data01转换为Map<String, Object>
//        Map<String, Object> data01Object = new HashMap<>(data01);
//        String data01Json = JSONObject.toJSONString(data01Object);
        data.put("data", data01);

        // 创建请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        // 发送POST请求
        String result = sendPostRequest(urlStr, headers, data);

        // 这里可以添加处理响应的代码，例如检查响应状态码，解析响应主体等
        System.out.println(result);
    } catch (Exception e) {
        e.printStackTrace();
    }
}

}
