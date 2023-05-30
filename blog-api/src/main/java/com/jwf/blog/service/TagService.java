package com.jwf.blog.service;

import com.jwf.blog.vo.Result;
import com.jwf.blog.vo.TagVo;

import java.util.List;

public interface TagService {

    List<TagVo> findTagsByArticleId(Long id);

    List<TagVo> hot(int limit);

    Result findAll();

    Result findAllDetail();

    Result TagDetailById(Long id);
}
