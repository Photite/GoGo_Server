package cn.edu.hbwe.gogo_server.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Slf4j
public class UserLoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求头，header值为Authorization，承载token
        String token = request.getHeader(JWTUtils.header);
        //token不存在
        if (token == null || token.equals("")) {
            log.info("传入token为空");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token为空!");
            return false;
        }
        //验证token
        String sub = JWTUtils.validateToken(token);
        if (sub == null || sub.equals("")) {
            log.info("token验证失败");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token验证失败!");
            return false;
        }
        //更新token有效时间 (如果需要更新其实就是产生一个新的token)
        if (JWTUtils.isNeedUpdate(token)) {
            String newToken = JWTUtils.createToken(sub);
            response.setHeader(JWTUtils.header, newToken);
        }
        return true;
    }

}
