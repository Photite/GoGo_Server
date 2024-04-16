package cn.edu.hbwe.gogo_server.service.aspect;

import cn.edu.hbwe.gogo_server.dao.UserDao;
import cn.edu.hbwe.gogo_server.entity.User;
import cn.edu.hbwe.gogo_server.exception.LoginException;
import cn.edu.hbwe.gogo_server.utils.EduSystemLoginUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Photite
 */
@Aspect
@Component
public class CookieRefreshAspect {

    @Autowired
    private EduSystemLoginUtil eduSystemLoginUtil;

    @Autowired
    private UserDao userDao;

    @Autowired
    private StringRedisTemplate redisTemplate;

    //    @Before("execution(* cn.edu.hbwe.gogo_server.service.EduService.*(..)) && args(eduUsername,..)")
    @Before("execution(* cn.edu.hbwe.gogo_server.service.EduService.*(..)) && args(eduUsername,..) && !execution(* cn.edu.hbwe.gogo_server.service.EduService.eduLogin(..))")
    public void refreshCookie(String eduUsername) throws Exception {
        try {
            // 检查cookie是否有效，如果无效则刷新
            if (!eduSystemLoginUtil.isCookieValid(eduUsername)) {
                System.out.println("cookie无效，刷新cookie");
                // 从数据库中获取用户信息
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("edu_username", eduUsername);
                User user = userDao.selectOne(queryWrapper);
                Map<String, String> newCookies = eduSystemLoginUtil.loginAndGetCookies(eduUsername, user.getEduPassword());
                // 将新的cookie存储到Redis中
                ObjectMapper mapper = new ObjectMapper();
                String newCookieJson = mapper.writeValueAsString(newCookies);
                redisTemplate.boundValueOps(eduUsername).set(newCookieJson);
                redisTemplate.expire(eduUsername, 7, TimeUnit.HOURS);
            } else {
                System.out.println("cookie有效，无需刷新");
            }
        } catch (NullPointerException e) {
            throw new Exception("登录失败，数据库中找不到你的教务系统用户名！");
        } catch (LoginException e) {
            throw new LoginException(e.getCode(), e.getMsg());
        }
    }
}
