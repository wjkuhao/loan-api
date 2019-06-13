package com.mod.loan.service;

public interface SmsService {

    void send(String alias, String templateKey, String phone, String param, String origin);

}
