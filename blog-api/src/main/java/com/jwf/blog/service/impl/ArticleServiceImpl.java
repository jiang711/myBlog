package com.jwf.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jwf.blog.dao.dos.Archives;
import com.jwf.blog.dao.mapper.ArticleBodyMapper;
import com.jwf.blog.dao.mapper.ArticleMapper;
import com.jwf.blog.dao.mapper.ArticleTagMapper;
import com.jwf.blog.dao.pojo.Article;
import com.jwf.blog.dao.pojo.ArticleBody;
import com.jwf.blog.dao.pojo.ArticleTag;
import com.jwf.blog.dao.pojo.SysUser;
import com.jwf.blog.service.*;
import com.jwf.blog.utils.UserThreadLocal;
import com.jwf.blog.vo.*;
import com.jwf.blog.vo.params.ArticleParam;
import com.jwf.blog.vo.params.PageParams;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private TagService tagService;

    @Autowired
    private SysUserService sysUserService;

    @Override
    public Result listArticlesPage(PageParams pageParams) {
        //直接使用sql语句实现
        Page<Article> page = new Page<>(pageParams.getPage(),pageParams.getPageSize());
        IPage<Article> articleIPage = this.articleMapper.listArticle(page,pageParams.getCategoryId(),pageParams.getTagId(),pageParams.getYear(),pageParams.getMonth());
        return Result.success(copyList(articleIPage.getRecords(),true,true));
    }

//    @Override
//    public List<ArticleVo> listArticlesPage(PageParams pageParams) {
//        //1.分页查询 article数据库表
//        Page<Article> page = new Page<>(pageParams.getPage(), pageParams.getPageSize());
//        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
//
//        //查询文章的参数 加上分类id，判断不为空 加上分类条件
//        if (pageParams.getCategoryId() != null) {
//            queryWrapper.eq(Article::getCategoryId, pageParams.getCategoryId());
//        }
//
//        //article表中没有tag字段，因为一篇article可以有多个tag，这两者的关系放在了article_tag表中
//        if (pageParams.getTagId() != null){
//            LambdaQueryWrapper<ArticleTag> articleTagLambdaQueryWrapper = new LambdaQueryWrapper<>();
//            articleTagLambdaQueryWrapper.eq(ArticleTag::getTagId,pageParams.getTagId());
//            List<ArticleTag> articleTags = articleTagMapper.selectList(articleTagLambdaQueryWrapper);
//            List<Long> articleIdList = new ArrayList<>();
//            for (ArticleTag articleTag : articleTags) {
//                articleIdList.add(articleTag.getArticleId());
//            }
//            if (articleIdList.size() > 0){
//                queryWrapper.in(Article::getId,articleIdList);
//            }
//        }
//
//        //是否置顶进行排序
//        //order by create_date desc
//        queryWrapper.orderByDesc(Article::getWeight, Article::getCreateDate);
//        Page<Article> articlePage = articleMapper.selectPage(page, queryWrapper);
//        List<Article> records = articlePage.getRecords();
//        //先要转成vo(view object)
//        List<ArticleVo> articleVoList = copyList(records, true, true);
//        return articleVoList;
//    }

    @Override
    public List<ArticleVo> hotArticles(int limit) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getViewCounts);
        queryWrapper.select(Article::getId,Article::getTitle);
        queryWrapper.last("limit " + limit);
        List<Article> articles = articleMapper.selectList(queryWrapper);
        List<ArticleVo> articleVoList = copyList(articles,false,false);
        return articleVoList;
    }

    @Override
    public List<ArticleVo> newArticles(int limit) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getCreateDate);
        queryWrapper.select(Article::getId,Article::getTitle);
        queryWrapper.last("limit " + limit);
        List<Article> articles = articleMapper.selectList(queryWrapper);
        List<ArticleVo> articleVoList = copyList(articles,false,false);
        return articleVoList;
    }

    @Override
    public List<Archives> listArchives() {
        List<Archives> archivesList = articleMapper.listArchives();
        return archivesList;
    }

    @Autowired
    private ThreadService threadService;

    @Override
    public ArticleVo findArticleById(Long id) {
        Article article = articleMapper.selectById(id);
        threadService.updateViewCount(articleMapper,article);
        return copy(article,true,true,true,true);
    }

    @Autowired
    private ArticleTagMapper articleTagMapper;

    @Override
    @Transactional
    public Result publish(ArticleParam articleParam) {
        //用UserThreadLocal的前提是此接口要加入到登录拦截器中！！！
        SysUser sysUser = UserThreadLocal.get();
        //分别对article表、article_tag表、article_body表插入记录
        //article
        Article article = new Article();
        boolean isEdit = false;
        if (articleParam.getId() != null) {
            article.setId(articleParam.getId());
            article.setTitle(articleParam.getTitle());
            article.setSummary(articleParam.getSummary());
            article.setCategoryId(articleParam.getCategory().getId());
            article.setCreateDate(System.currentTimeMillis());
            articleMapper.updateById(article);
            isEdit = true;
        }
        else{
            article.setAuthorId(sysUser.getId());
            article.setCategoryId(articleParam.getCategory().getId());
            article.setCreateDate(System.currentTimeMillis());
            article.setCommentCounts(0);
            article.setSummary(articleParam.getSummary());
            article.setTitle(articleParam.getTitle());
            article.setViewCounts(0);
            article.setWeight(Article.Article_Common);
            //先插入为了获得article_id，给body_id赋临时值，之后再更新article表里的body_id
            article.setBodyId(-1L);
            articleMapper.insert(article);
        }

        //article_tag
        List<TagVo> tags = articleParam.getTags();
        if (tags != null) {
            for (TagVo tag : tags) {
                Long articleId = article.getId();
                if(isEdit){
                    //先删除
                    LambdaQueryWrapper<ArticleTag> queryWrapper = Wrappers.lambdaQuery();
                    queryWrapper.eq(ArticleTag::getArticleId, articleId);
                    articleTagMapper.delete(queryWrapper);
                }
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(article.getId());
                articleTag.setTagId(tag.getId());
                articleTagMapper.insert(articleTag);
            }
        }

        //article_body
        ArticleBody articleBody = new ArticleBody();
        articleBody.setArticleId(article.getId());
        articleBody.setContent(articleParam.getBody().getContent());
        articleBody.setContentHtml(articleParam.getBody().getContentHtml());
        if(isEdit) {
            LambdaUpdateWrapper<ArticleBody> updateWrapper = Wrappers.lambdaUpdate();
            updateWrapper.eq(ArticleBody::getArticleId,article.getId());
            articleBodyMapper.update(articleBody, updateWrapper);
        }
        else {
            articleBodyMapper.insert(articleBody);

            article.setBodyId(articleBody.getId());
            articleMapper.updateById(article);
        }
        ArticleVo articleVo = new ArticleVo();
        articleVo.setId(article.getId());
        return Result.success(articleVo);
    }

    @Override
    public Result searchArticle(String search) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getViewCounts);
        queryWrapper.select(Article::getId,Article::getTitle);
        queryWrapper.like(Article::getTitle,search);
        List<Article> articles = articleMapper.selectList(queryWrapper);
        return Result.success(copyList(articles,false,false));
    }

    private List<ArticleVo> copyList(List<Article> records, boolean isTag, boolean isAuthor){
        List<ArticleVo> articleVoList = new ArrayList<>();
        for (Article record : records) {
            articleVoList.add(copy(record, isTag, isAuthor, false, false));
        }
        return articleVoList;
    }

    private List<ArticleVo> copyList(List<Article> records, boolean isTag, boolean isAuthor, boolean isBody){
        List<ArticleVo> articleVoList = new ArrayList<>();
        for (Article record : records) {
            articleVoList.add(copy(record, isTag, isAuthor, isBody, false));
        }
        return articleVoList;
    }

    private List<ArticleVo> copyList(List<Article> records, boolean isTag, boolean isAuthor,boolean isBody,boolean isCategory) {
        List<ArticleVo> articleVoList = new ArrayList<>();
        for (Article record : records) {
            articleVoList.add(copy(record,isTag,isAuthor,isBody,isCategory));
        }
        return articleVoList;
    }

    private ArticleVo copy(Article article, boolean isTag, boolean isAuthor, boolean isBody, boolean isCategory){
        ArticleVo articleVo = new ArticleVo();
        BeanUtils.copyProperties(article, articleVo);

        articleVo.setCreateDate(new DateTime(article.getCreateDate()).toString("yyyy-MM-dd HH:mm"));
        //不是所有的接口都需要标签、作者信息
        if (isTag){
            List<TagVo> tags = tagService.findTagsByArticleId(article.getId());
            articleVo.setTags(tags);
        }
        if (isAuthor) {
            SysUser sysUser = sysUserService.findSysUserById(article.getAuthorId());
            UserVo userVo = new UserVo();
            userVo.setAvatar(sysUser.getAvatar());
            userVo.setId(sysUser.getId());
            userVo.setNickname(sysUser.getNickname());
            articleVo.setAuthor(userVo);
        }
        if (isBody){
            ArticleBodyVo articleBody = findArticleBodyById(article.getId());
            articleVo.setBody(articleBody);
        }
        if (isCategory){
            CategoryVo categoryVo = findCategoryById(article.getCategoryId());
            articleVo.setCategory(categoryVo);
        }
        return articleVo;
    }

    @Autowired
    private CategoryService categoryService;

    private CategoryVo findCategoryById(Long categoryId) {
        return categoryService.findCategoryById(categoryId);
    }

    @Autowired
    private ArticleBodyMapper articleBodyMapper;

    private ArticleBodyVo findArticleBodyById(Long articleId) {
        LambdaQueryWrapper<ArticleBody> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleBody::getArticleId, articleId);
        ArticleBody articleBody = articleBodyMapper.selectOne(queryWrapper);
        ArticleBodyVo articleBodyVo = new ArticleBodyVo();
        articleBodyVo.setContent(articleBody.getContent());

//        如果copy函数里面判断isBody用的是article.getBodyId()就用如下代码
//        ArticleBody articleBody = articleBodyMapper.selectById(bodyId);
//        ArticleBodyVo articleBodyVo = new ArticleBodyVo();
//        articleBodyVo.setContent(articleBody.getContent());

        return articleBodyVo;
    }
}
