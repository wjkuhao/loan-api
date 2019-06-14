package com.mod.loan.service;

import javax.servlet.http.HttpServletRequest;

/**
 * @author NIELIN
 * @version $Id: HuichaoRepayCallBackService.java, v 0.1 2019/1/17 16:08 NIELIN Exp $
 */
public interface HuichaoRepayCallBackService {

    /**
     * app端支付宝还款/微信扫码支付异步回调-
     *
     * @param request
     * @return
     */
    void aliAppH5OrWxScanRepayCallback(HttpServletRequest request);
}
