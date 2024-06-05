package cn.edu.hbwe.gogo_server.service;

import cn.edu.hbwe.gogo_server.dao.ArticlesDao;
import cn.edu.hbwe.gogo_server.dto.Result;
import cn.edu.hbwe.gogo_server.entity.Articles;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ArticlesService {

    @Autowired
    private ArticlesDao articlesDao;

    public Result addArticles(Articles articles) {
        int i = articlesDao.insert(articles);
        if (i > 0) {
            return new Result("添加成功", "1000", null);
        } else {
            return new Result("添加失败", "2000", null);
        }
    }

    public Result deleteArticles(Articles articles) {
        QueryWrapper<Articles> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", articles.getId());
        int i = articlesDao.delete(queryWrapper);
        if (i > 0) {
            return new Result("删除成功", "1000", null);
        } else {
            return new Result("删除失败", "2000", null);
        }
    }

    public Result updateArticles(Articles articles) {
        QueryWrapper<Articles> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", articles.getId());
        int i = articlesDao.update(articles, queryWrapper);
        if (i > 0) {
            return new Result("修改成功", "1000", null);
        } else {
            return new Result("修改失败", "2000", null);
        }
    }

    public Result queryArticles(Articles articles) {
        QueryWrapper<Articles> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", articles.getId());
        return new Result("查询成功", "1000", articlesDao.selectList(queryWrapper));
    }

    // 生成一个查询所有文章的方法
    public Result queryAllArticles() {
        return new Result("查询成功", "1000", articlesDao.selectList(null));
    }
}
