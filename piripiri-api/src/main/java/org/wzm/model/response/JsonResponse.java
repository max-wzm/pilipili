package org.wzm.model.response;

import lombok.Data;

@Data
public class JsonResponse<T> {

    private String code;
    private String msg;
    private  T data;

    public JsonResponse(String code,String msg){
        this.code = code;
        this.msg = msg;
    }

    public JsonResponse(T data){
        this.data = data;
        msg = "成功";
        code = "0";
    }

    public static JsonResponse<String> success(){
        return new JsonResponse<String>(null);
    }

    public static JsonResponse<String> success(String data){
        return new JsonResponse<String>(data);
    }

    public static JsonResponse<String> fail(){
        return new JsonResponse<>("1","失败");
    }

    public static JsonResponse<String> fail(String code,String msg){
        return new JsonResponse<>(code, msg);
    }
}
