package com.zerobase.dividend.exception.impl;

import com.zerobase.dividend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class NoExistCompanyTickerException  extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "존재하지 않는 회사 ticker 입니다.";
    }
}
