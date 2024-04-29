package cn.edu.hbwe.gogo_server.utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Photite
 */
@Component
public class HTTPUtil {

    private static final Logger logger = Logger.getLogger(HTTPUtil.class.getName());
    private static String eduSystemUrl;

    @Value("${edu-system.url}")
    public void setEduSystemUrl(String eduSystemUrl) {
        HTTPUtil.eduSystemUrl = eduSystemUrl;
    }

    static {
        init();
    }

    public static Connection newSession(Object... url) {
        return Jsoup.newSession()
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .url(compile(url))
                .timeout(30000);
    }

    public static String compile(Object... p) {
        StringBuilder builder = new StringBuilder(eduSystemUrl);
        Arrays.stream(p).forEach(builder::append);
        return builder.toString();
    }

    public static Connection.Response sendPostRequest(String url, Map<String, String> headers, Map<String, String> data, Map<String, String> cookies) throws Exception {
        return sendRequest(Connection.Method.POST, url, headers, data, cookies);
    }

    public static Connection.Response sendGetRequest(String url, Map<String, String> headers, Map<String, String> cookies) throws Exception {
        return sendRequest(Connection.Method.GET, url, headers, null, cookies);
    }

    private static Connection.Response sendRequest(Connection.Method method, String url, Map<String, String> headers, Map<String, String> data, Map<String, String> cookies) throws Exception {
        Connection connection = newSession(url);
        if (headers != null) {
            connection.headers(headers);
        }
        if (data != null) {
            connection.data(data);
        }
        if (cookies != null) {
            connection.cookies(cookies);
        }
        return connection.method(method).execute();
    }

    // 定义一个创建公共请求头的方法，返回一个Map<String, String>对象
    public static Map<String, String> createCommonHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
        return headers;
    }

    static public void init() {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType){
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType){
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            logger.severe("无法初始化 SSL 上下文：" + e.getMessage());
        }
    }
}
