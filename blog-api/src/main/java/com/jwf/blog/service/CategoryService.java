package com.jwf.blog.service;

import com.jwf.blog.vo.CategoryVo;
import com.jwf.blog.vo.Result;

public interface CategoryService {

    CategoryVo findCategoryById(Long id);

    Result findAll();

    Result findAllDetail();

    Result categoryDetailById(Long id);
}
