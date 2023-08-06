package org.wzm.api.handler;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wzm.domain.BizException;
import org.wzm.model.response.JsonResponse;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CommonGlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public JsonResponse<String> commonExceptionHandler(HttpServletRequest request, Exception e){
        String eMessage = e.getMessage();
        if(e instanceof BizException){
            String code = ((BizException) e).getCode();
            return new JsonResponse<>(code,eMessage);
        }else{
            return new JsonResponse<>("500",eMessage);
        }
    }
}
