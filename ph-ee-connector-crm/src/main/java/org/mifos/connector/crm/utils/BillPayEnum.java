package org.mifos.connector.crm.utils;

public enum BillPayEnum {

    SUCCESS_RESPONSE_CODE("00"), FAILED_RESPONSE_CODE("01"), SUCCESS_STATUS("ACK"), SUCCESS_RESPONSE_MESSAGE(
            "TRANSACTION SUCCESSFUL"), FAILED_RESPONSE_MESSAGE("Server Link Down");

    private final String value;

    BillPayEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
