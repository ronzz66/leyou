package com.leyou.controller;

import com.leyou.service.UserService;
import com.leyou.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Controller
public class UserController {

    @Autowired
    public UserService userService;
    //注册校验：验证用户名或手机号是否存在是否存在
    @GetMapping("check/{data}/{type}")  //type 为1 校验用户名 2为校验手机号
    public ResponseEntity<Boolean> checkUser(@PathVariable("data")String data,
                                             @PathVariable("type")Integer type){
        Boolean flag = userService.checkUser(data,type);

        if (flag==null){
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(flag);
    }

    //发送验证码
    @PostMapping("code")
    public ResponseEntity<Void> sendVerifyCode(@RequestParam("phone")String phone){
        userService.sendVerifyCode(phone);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    //注册
    @PostMapping("register")
    public ResponseEntity<Void> register(@Valid User user, @RequestParam("code")String code){
        userService.register(user,code);

        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    //登录校验
    @GetMapping("query")
    public ResponseEntity<User> queryUser(@RequestParam("username")String username,
                                          @RequestParam("password")String password){
        User user = this.userService.queryUser(username,password);

        if (user==null){
            return  ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.ok(user);
    }


}
