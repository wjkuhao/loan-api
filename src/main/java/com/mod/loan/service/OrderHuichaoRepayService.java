package com.mod.loan.service;

/**
 * @author NIELIN
 * @version $Id: OrderHuichaoRepayService.java, v 0.1 2019/1/17 16:08 NIELIN Exp $
 */
public interface OrderHuichaoRepayService {

    /**
     * 汇潮支付宝还款
     *
     * @param orderId 订单id
     */
    String huichaoRepay4AliAppH5(Long orderId);

    /**
     * 汇潮支付宝还款/微信扫码支付结果查询
     *
     * @param repayNo 还款流水号
     */
    void huichaoRepay4AliAppH5OrWxScanQuery(String repayNo);

    /**
     * 汇潮微信扫码支付
     *
     * @param orderId 订单id
     */
    String huichaoRepay4WxScan(Long orderId);
}
