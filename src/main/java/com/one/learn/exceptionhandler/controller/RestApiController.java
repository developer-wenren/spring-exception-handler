package com.one.learn.exceptionhandler.controller;

import com.one.learn.exceptionhandler.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.HashMap;
import java.util.Map;

/**
 * API 控制器
 *
 * @author One
 * @date 2019/05/18
 */
@RestController
public class RestApiController {

    /**
     * 用于模拟 405 请求异常
     *
     * @param param
     * @return
     */
    @PostMapping("/hello")
    public ResponseEntity<String> hello(String param) {
        String body = "hello," + param;
        return ResponseEntity.ok(body);
    }

    /**
     * 用于模拟算术异常
     *
     * @param param
     * @return
     */
    @GetMapping("/hello2")
    public ResponseEntity<String> hello2(String param) {
        int i = 1 / 0;
        String body = "hello," + param;
        return ResponseEntity.ok(body);
    }

    /**
     * 用于模拟空指针异常
     *
     * @param param
     * @return
     */
    @GetMapping("/hello3")
    public ResponseEntity<String> hello3(String param) {
        String a = null;
        byte[] bytes = a.getBytes();
        String body = "hello," + param;
        return ResponseEntity.ok(body);
    }

    /**
     * 用于模拟自定义异常
     *
     * @param param
     * @return
     */
    @GetMapping("/hello4")
    public ResponseEntity<String> hello4(String param) {
        throw new CustomException("自定义异常");
    }

    /**
     * 模拟非法参数异常
     *
     * @return
     */
    @GetMapping("/exception1")
    public String exception1() {
        throw new IllegalArgumentException("非法参数");
    }

    /**
     * 模拟非法状态异常
     *
     * @return
     */
    @GetMapping("/exception2")
    public String exception2() {
        throw new IllegalStateException("非法状态异常");
    }

    /**
     * 非法状态异常的处理
     *
     * @param ex
     * @return
     */
    @ExceptionHandler({IllegalStateException.class})
    public ModelAndView handleIllegalStateException(IllegalStateException ex) {
        System.out.println("非法状态处理出现,需要处理 " + ex.getMessage());
        ModelAndView modelAndView = new ModelAndView();
        Map<String, String> maps = new HashMap<>();
        maps.put("data", null);
        maps.put("message", ex.getClass().getName());
        maps.put("code", "400");
        MappingJackson2JsonView mappingJackson2JsonView = new MappingJackson2JsonView();
        mappingJackson2JsonView.setAttributesMap(maps);
        modelAndView.setView(mappingJackson2JsonView);
        return modelAndView;
    }

    /**
     * 模拟运行时异常
     *
     * @return
     */
    @GetMapping("/exception3")
    public String exception3() {
        throw new RuntimeException("全局异常处理");
    }

    /**
     * 模拟 ResponseStatusException 异常
     *
     * @param param
     * @return
     */
    @GetMapping("/exception4")
    public ResponseEntity<String> exception4(String param) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "资源未找到");
    }
}
