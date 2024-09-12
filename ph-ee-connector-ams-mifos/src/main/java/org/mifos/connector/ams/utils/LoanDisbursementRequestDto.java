package org.mifos.connector.ams.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoanDisbursementRequestDto {

    String actualDisbursementDate;
    String dateFormat;
    String locale;
}
