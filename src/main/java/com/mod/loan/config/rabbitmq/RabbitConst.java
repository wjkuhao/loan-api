package com.mod.loan.config.rabbitmq;

public class RabbitConst {

    public final static String queue_sms = "queue_sms";  //短信队列
    public final static String queue_risk_order_notify = "queue_risk_order_notify";  //风控订单审核通知

    public final static String QUEUE_RECYCLE_REPAY_STAT = "queue_recycle_repay_stat";  //催收还款消息

    /**
     * 同盾通讯录mq队列名
     */
    public final static String QUEUE_TONGDUN_ADDRESS_LIST = "queue_tongdun_address_list";

}
