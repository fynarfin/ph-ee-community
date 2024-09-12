package org.mifos.connector.ams.utils;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

public class LoanDisbursementRequestDtoHelper {

    public LoanDisbursementRequestDto createLoanDisbursementRequestDto(String originDate) {
        Long timestamp = Long.parseLong(originDate);
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
        String formattedDate = sdf.format(date);
        LoanDisbursementRequestDto loanDisbursementRequestDto = new LoanDisbursementRequestDto();
        loanDisbursementRequestDto.setActualDisbursementDate(formattedDate);
        loanDisbursementRequestDto.setDateFormat("dd MMMM yyyy");
        loanDisbursementRequestDto.setLocale("en");
        return loanDisbursementRequestDto;
    }

    public String getBasicAuthHeader(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }
}
