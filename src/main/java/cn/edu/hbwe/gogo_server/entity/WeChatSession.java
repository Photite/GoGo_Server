package cn.edu.hbwe.gogo_server.entity;

import lombok.Data;

@Data
public class WeChatSession {

    /**
     * 微信服务器上辨识用户的唯一id
     */
    private String openid;

    /**
     * 身份凭证
     */
    private String session_key;

    /**
     * 错误代码
     */
    private String errcode;

    /**
     * 错误信息
     */
    private String errmsg;

}
