package com.one.learn.exceptionhandler.resolver;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义 HandlerExceptionResolver
 *
 * @author One
 */
@Component
public class RestResponseStatusExceptionResolver extends AbstractHandlerExceptionResolver {

    @Override
    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response,
                                              Object handler, Exception ex) {
        try {
            if (ex instanceof IllegalArgumentException) {
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
        } catch (Exception handlerException) {
            logger.warn("Handling of [" + ex.getClass().getName() + "] resulted in Exception", handlerException);
        }
        return null;
    }

}