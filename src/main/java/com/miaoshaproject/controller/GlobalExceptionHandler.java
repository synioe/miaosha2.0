package com.miaoshaproject.controller;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EnumBussinessError;
import com.miaoshaproject.response.CommonReturnType;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局通用异常处理器 ControllerAdvice切面
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public CommonReturnType doError(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Exception e) {
        e.printStackTrace();

        Map<String, Object> responseData = new HashMap<>();
        if (e instanceof BusinessException) {
            BusinessException businessException = (BusinessException) e;
            responseData.put("errCode", businessException.getErrorCode());
            responseData.put("errMsg", businessException.getErrMsg());
        } else if (e instanceof ServletRequestBindingException) {
            responseData.put("errCode", EnumBussinessError.UNKNOW_ERROR.getErrorCode());
            responseData.put("errMsg", "url绑定路由问题"); // 必传的参数没有传
        } else if (e instanceof NoHandlerFoundException) {
            responseData.put("errCode", EnumBussinessError.UNKNOW_ERROR.getErrorCode());
            responseData.put("errMsg", "没有对应的访问路径");//访问错误的静态资源路径
        } else {
            responseData.put("errCode", EnumBussinessError.UNKNOW_ERROR.getErrorCode());
            responseData.put("errMsg", EnumBussinessError.UNKNOW_ERROR.getErrorCode());
        }
        return CommonReturnType.create(responseData, "fail");
    }
}
