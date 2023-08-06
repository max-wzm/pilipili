package org.wzm.model.exception;

/**
 * @author wangzhiming
 */
public class ApiException extends RuntimeException {
    public static final long serialVersionUID = 1L;

    private String code;

    public ApiException(String code, String name) {
        super(name);
        this.code = code;
    }

    public ApiException(String name) {
        super(name);
        code = "500";
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
