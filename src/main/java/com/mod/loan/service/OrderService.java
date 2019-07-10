package com.mod.loan.service;

import com.mod.loan.common.mapper.BaseService;
import com.mod.loan.model.Order;
import com.mod.loan.model.OrderPay;
import com.mod.loan.model.OrderPhone;

import java.util.Date;
import java.util.List;

public interface OrderService extends BaseService<Order, Long> {

    /**
     * 查找用户在当前商户下进行中的订单数量
     *
     * @param uid 用户uid
     * @return 进行中的订单数
     */
    int countLoaningOrderByUid(Long uid);

    /**
     * 查找用户最近一张订单
     *
     * @return
     */
    Order findUserLatestOrder(Long uid);

    /**
     * 查找用户历史订单
     */

    List<Order> getByUid(Long uid);


    int addOrder(Order order, OrderPhone orderPhone);


    OrderPhone findOrderPhoneByOrderId(Long orderId);

    /**
     * 查找用户收款成功记录
     */
    OrderPay findOrderPaySuccessRecord(Long orderId);

    Integer judgeUserTypeByUid(Long uid);

    Integer countByUid(Long uid);

    Integer countPaySuccessByUid(Long uid);

    List<Order> findOverdueOrder();

    /**
     * 通过身份证查询是否在系统中存在逾期订单， 提单的时候校验
     *
     * @param certNo 手机号
     * @return 订单信息
     */
    Order findOverdueByCertNo(String certNo);

    /**
     * 通过手机号查询是否在系统中存在逾期订单， 注册的时候校验（渠道开关控制）
     *
     * @param phone 手机号
     * @return 逾期的订单信息
     */
    Order findOverdueByPhone(String phone);

    /**
     * 通过手机号查找用户是否存在未完成的订单
     */
    boolean checkUnfinishOrderByPhone(String phone);

    /**
     * 通过身份证查找用户是否存在未完成的订单
     */
    boolean checkUnfinishOrderByCertNo(String certNo);

    /**
     * 根据当前订单状态更新支付成功后订单状态
     *
     * @param status
     * @return
     */
    int setRepaySuccStatusByCurrStatus(Integer status);

    void updatePayCallbackInfo(Order order, OrderPay orderPay);

    void updatePayConfirmLoan(Long orderId);

    Date findFinalRecordTime(Long uid);
}
