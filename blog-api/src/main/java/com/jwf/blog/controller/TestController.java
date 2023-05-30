package com.jwf.blog.controller;

import com.jwf.blog.dao.pojo.SysUser;
import com.jwf.blog.utils.UserThreadLocal;
import com.jwf.blog.vo.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class TestController {

    @RequestMapping
    public Result test(){
        SysUser sysUser = UserThreadLocal.get();
        System.out.println(sysUser);
        return Result.success(null);
    }
}
