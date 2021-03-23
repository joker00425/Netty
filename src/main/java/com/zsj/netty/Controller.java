package com.zsj.netty;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
    @RequestMapping("/netty")
    public void hello(){
        System.out.println(
                "hello netty"
        );
    }
}
