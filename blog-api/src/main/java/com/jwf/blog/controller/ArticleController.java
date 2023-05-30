package com.jwf.blog.controller;

import com.jwf.blog.common.aop.LogAnnotation;
import com.jwf.blog.dao.dos.Archives;
import com.jwf.blog.service.ArticleService;
import com.jwf.blog.vo.ArticleVo;
import com.jwf.blog.vo.Result;
import com.jwf.blog.vo.params.ArticleParam;
import com.jwf.blog.vo.params.PageParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//json数据交互
@RestController
@RequestMapping("articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @PostMapping
    @LogAnnotation(module = "文章", operation = "获取文章列表")
    public Result articles(@RequestBody PageParams pageParams) {
        return articleService.listArticlesPage(pageParams);
    }

    //最热文章
    @PostMapping("hot")
    public Result hotArticles(){
        int limit = 5;
        List<ArticleVo> hotArticles = articleService.hotArticles(limit);
        return Result.success(hotArticles);
    }

    //最新文章
    @PostMapping("new")
    public Result newArticles(){
        int limit = 5;
        List<ArticleVo> newArticles = articleService.newArticles(limit);
        return Result.success(newArticles);
    }

    //文章归档
    @PostMapping("listArchives")
    public Result listArchives(){
        List<Archives> archivesList = articleService.listArchives();
        return Result.success(archivesList);
    }

    @PostMapping("view/{id}")
    public Result findArticleById(@PathVariable("id") Long id) {
        ArticleVo articleVo = articleService.findArticleById(id);
        return Result.success(articleVo);
    }

    @PostMapping("publish")
    public Result publish(@RequestBody ArticleParam articleParam){
        return articleService.publish(articleParam);
    }

    @PostMapping("{id}")
    public Result articleById(@PathVariable("id") Long articleId){
        ArticleVo articleVo = articleService.findArticleById(articleId);
        return Result.success(articleVo);
    }

    @PostMapping("search")
    public Result search(@RequestBody ArticleParam articleParam){
        //写一个搜索接口
        String search = articleParam.getSearch();
        return articleService.searchArticle(search);
    }
}
