package cn.edu.hbwe.gogo_server.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Photite
 */
@Data
public class LoginAuthorization {

    private String jwxtUsername;
    private String jwxtPassword;
    private String captcha;
    private Map<String, String> cookies;
    private Map<String, String> publicKey;
    private String csrf;

    public LoginAuthorization() {
        this.cookies = new HashMap<>();
        this.publicKey = new HashMap<>();
    }


}
