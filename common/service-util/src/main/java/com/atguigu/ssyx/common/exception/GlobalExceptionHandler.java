package com.atguigu.ssyx.common.exception;


import com.atguigu.ssyx.common.result.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


@ControllerAdvice //AOP 面向切面  拦截Controller
public class GlobalExceptionHandler {
    @ResponseBody //把对象转换成Json对象返回
    @ExceptionHandler(Exception.class)//异常处理器  当出现异常会执行这个方法 参数是指定哪个异常出现
    public Result error(Exception e){
        e.printStackTrace();
        return Result.fail(null);
    }


    //自定义异常处理
    @ResponseBody //把对象转换成Json对象返回
    @ExceptionHandler(SsyxException.class)//异常处理器  当出现异常会执行这个方法 参数是指定哪个异常出现
    public Result error(SsyxException e){
        e.printStackTrace();
        return Result.build(null,e.getCode(),e.getMessage());
    }
}
