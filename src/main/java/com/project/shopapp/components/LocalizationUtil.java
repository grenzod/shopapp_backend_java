package com.project.shopapp.components;

import com.project.shopapp.utils.WebUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@RequiredArgsConstructor
@Component
public class LocalizationUtil implements ApplicationContextAware {
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }

    public static String getLocaleMessage(String key, Object ... params) {
        HttpServletRequest request = WebUtil.getRequest();
        LocaleResolver localeResolver = getBean(LocaleResolver.class);
        MessageSource messageSource = getBean(MessageSource.class);

        Locale locale = localeResolver.resolveLocale(request);
        return messageSource.getMessage(key, params, locale);
    }
}
