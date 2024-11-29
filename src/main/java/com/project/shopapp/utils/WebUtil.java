package com.project.shopapp.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class WebUtil {
    public static HttpServletRequest getRequest(){
        return ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
    }
}
