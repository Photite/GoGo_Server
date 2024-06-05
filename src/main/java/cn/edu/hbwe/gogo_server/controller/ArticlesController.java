package cn.edu.hbwe.gogo_server.controller;

import cn.edu.hbwe.gogo_server.dto.Result;
import cn.edu.hbwe.gogo_server.entity.Articles;
import cn.edu.hbwe.gogo_server.service.ArticlesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/articles")
public class ArticlesController {

    @Autowired
    private ArticlesService articlesService;

    // 定义一个添加文章的请求
    @PostMapping("/add")
    public ResponseEntity<Result> addArticles(@RequestBody Articles articles) {
        return new ResponseEntity<Result>(articlesService.addArticles(articles), HttpStatus.OK);
    }

    // 定义一个删除文章的请求
    @PostMapping("/delete")
    public ResponseEntity<Result> deleteArticles(@RequestBody Articles articles) {
        return new ResponseEntity<Result>(articlesService.deleteArticles(articles), HttpStatus.OK);
    }

    // 定义一个修改文章的请求
    @PostMapping("/update")
    public ResponseEntity<Result> updateArticles(@RequestBody Articles articles) {
        return new ResponseEntity<Result>(articlesService.updateArticles(articles), HttpStatus.OK);
    }

    // 定义一个查询文章的请求
    @PostMapping("/query")
    public ResponseEntity<Result> queryArticles(@RequestBody Articles articles) {
        return new ResponseEntity<Result>(articlesService.queryArticles(articles), HttpStatus.OK);
    }

    // 定义一个查询所有文章的请求
    @PostMapping("/queryAll")
    public ResponseEntity<Result> queryAllArticles() {
        return new ResponseEntity<Result>(articlesService.queryAllArticles(), HttpStatus.OK);
    }
}
