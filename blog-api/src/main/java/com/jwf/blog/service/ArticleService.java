package com.jwf.blog.service;

import com.jwf.blog.dao.dos.Archives;
import com.jwf.blog.vo.ArticleVo;
import com.jwf.blog.vo.Result;
import com.jwf.blog.vo.params.ArticleParam;
import com.jwf.blog.vo.params.PageParams;

import java.util.List;

public interface ArticleService {

    //分页查询 文章列表
    Result listArticlesPage(PageParams pageParams);

    //最热文章
    List<ArticleVo> hotArticles(int limit);

    //最新文章
    List<ArticleVo> newArticles(int limit);

    List<Archives> listArchives();

    ArticleVo findArticleById(Long id);

    Result publish(ArticleParam articleParam);

    Result searchArticle(String search);
}
