package com.jwf.blog.controller;

import com.jwf.blog.service.TagService;
import com.jwf.blog.vo.Result;
import com.jwf.blog.vo.TagVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("tags")
public class TagController {

    @Autowired
    private TagService tagService;

    @GetMapping("/hot")
    public Result listHotTags() {
        int limit = 3;
        List<TagVo> tagVoList = tagService.hot(limit);
        return Result.success(tagVoList);
    }

    @GetMapping
    public Result findAll(){
        return tagService.findAll();
    }

    @GetMapping("detail")
    public Result findAllDetail(){
        return tagService.findAllDetail();
    }

    @GetMapping("detail/{id}")
    public Result TagDetailById(@PathVariable("id") Long id){
        return tagService.TagDetailById(id);
    }
}
