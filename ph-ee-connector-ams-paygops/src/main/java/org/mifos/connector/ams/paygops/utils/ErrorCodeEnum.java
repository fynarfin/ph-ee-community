package org.mifos.connector.ams.paygops.utils;

public enum ErrorCodeEnum {


    RECONCILIATION(12345,  "Reconciliation failed"),
    DEFAULT(123,"Body Parsing Error"),
    ;


    private final Integer code;
    private final String value;

    ErrorCodeEnum(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public Integer getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }
}