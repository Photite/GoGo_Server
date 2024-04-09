package cn.edu.hbwe.gogo_server.dto;

import lombok.*;

/**
 * @author Photite
 */
@Setter
@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result {
    private String msg;
    private String code;
    private Object data;


    public Result(String msg, String code) {
        this.msg = msg;
        this.code = code;
    }
}
