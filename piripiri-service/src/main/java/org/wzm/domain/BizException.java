package org.wzm.domain;

public class BizException extends RuntimeException {
    public static final long serialVersionUID = 1L;

    private String code;

    public BizException(String code, String name) {
        super(name);
        this.code = code;
    }

    public BizException(String name) {
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
