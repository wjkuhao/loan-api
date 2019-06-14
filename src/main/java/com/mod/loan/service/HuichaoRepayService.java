package com.mod.loan.service;

import com.mod.loan.model.request.AliAppH5RepayQueryRequest;
import com.mod.loan.model.request.AliAppH5RepayRequest;

/**
 * @author NIELIN
 * @version $Id: HuichaoRepayService.java, v 0.1 2019/1/17 16:08 NIELIN Exp $
 */
public interface HuichaoRepayService {

    /**
     * app端支付宝还款
     *
     * @param request
     * @return
     */
    String aliAppH5RepayUrl(AliAppH5RepayRequest request);

    /**
     * app端支付宝还款/微信扫码支付结果查询
     *
     * @param request
     * @return
     */
    String aliAppH5OrWxScanRepayQuery(AliAppH5RepayQueryRequest request);

    /**
     * 微信扫码支付
     *
     * @param request
     * @return
     */
    String wxScanRepay(AliAppH5RepayRequest request);
}
