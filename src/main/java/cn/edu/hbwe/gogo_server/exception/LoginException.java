package cn.edu.hbwe.gogo_server.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

/**
 * @author Photite
 */
@Getter
@Setter
public class LoginException extends RuntimeException {
    private String code;
    private String msg;

    public LoginException(String code, String message) {
        super(message);
        this.code = code;
        this.msg = message;
    }
    public LoginException(String message) {
        super(message);
        this.msg = message;
    }
}
