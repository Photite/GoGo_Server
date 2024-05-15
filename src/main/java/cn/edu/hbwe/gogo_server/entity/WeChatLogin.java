package cn.edu.hbwe.gogo_server.entity;

import lombok.Data;

@Data
public class WeChatLogin {

    /**
     * 临时登录凭证
     */
    private String code;

    /**
     * 微信服务器上的唯一id
     */
    private String openId;

}
