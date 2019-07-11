package com.mod.loan.controller.order;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.annotation.LoginRequired;
import com.mod.loan.common.enums.OrderEnum;
import com.mod.loan.common.enums.ResponseEnum;
import com.mod.loan.common.model.RequestThread;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.config.rabbitmq.RabbitConst;
import com.mod.loan.config.redis.RedisConst;
import com.mod.loan.config.redis.RedisMapper;
import com.mod.loan.model.*;
import com.mod.loan.service.*;
import com.mod.loan.util.MoneyUtil;
import com.mod.loan.util.StringUtil;
import com.mod.loan.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单申请详细展示，及下单确认 回收和贷款文案通用
 *
 * @author yhx 2018年4月26日 下午13:42:19
 */
@CrossOrigin("*")
@RestController
@RequestMapping("order")
public class OrderApplyController {

    private static Logger logger = LoggerFactory.getLogger(OrderApplyController.class);

    @Autowired
    private UserIdentService userIdentService;
    @Autowired
    private UserBankService userBankService;
    @Autowired
    private MerchantRateService merchantRateService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private BlacklistService blacklistService;
    @Autowired
    private RedisMapper redisMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private UserService userService;
    @Autowired
    private MerchantConfigService merchantConfigService;
    @Autowired
    MerchantQuotaConfigService merchantQuotaConfigService;
    @Autowired
    DataCenterService dataCenterService;
    @Autowired
    MerchantService merchantService;

    /**
     * h5 借款确认 获取费用明细
     */
    @LoginRequired()
    @RequestMapping(value = "order_confirm")
    public ResultMessage order_confirm() {
        Map<String, Object> map = new HashMap<>();
        Long uid = RequestThread.getUid();
        UserBank userBank = userBankService.selectUserCurrentBankCard(uid);
        if (null == userBank) {
            return new ResultMessage(ResponseEnum.M4000, "未查到银行卡信息");
        }
        Integer borrowType = orderService.countPaySuccessByUid(uid);
        MerchantRate merchantRate = merchantRateService.findByMerchantAndBorrowType(RequestThread.getClientAlias(),
                borrowType);
        if (null == merchantRate) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "未查到产品信息");
        }
        map.put("productId", merchantRate.getId());
        map.put("productDay", merchantRate.getProductDay());

        BigDecimal maxQuota = merchantQuotaConfigService.computeQuota(RequestThread.getClientAlias(), uid,
                merchantRate.getProductMoney(), merchantRate.getBorrowType());
        //  自选额度暂时关闭
        // map.put("totalRate", merchantRate.getTotalRate());
        //  map.put("productMoneyRange", merchantRate.getProductMoney().intValue()+ "~" + maxQuota.intValue());
        map.put("productMoney", maxQuota);

        BigDecimal totalFee = MoneyUtil.totalFee(maxQuota, merchantRate.getTotalRate());// 综合费用
        map.put("totalFee", totalFee);

        BigDecimal actualMoney = MoneyUtil.actualMoney(maxQuota, totalFee);// 实际到账
        map.put("actualMoney", actualMoney);


        map.put("cardName", userBank.getCardName());
        map.put("cardNo", StringUtil.bankTailNo(userBank.getCardNo()));

        return new ResultMessage(ResponseEnum.M2000, map);
    }

    /**
     * h5 借款确认 提交订单
     */
    @LoginRequired
    @RequestMapping(value = "order_submit")
    public ResultMessage order_submit(@RequestParam(required = true) Long productId,
                                      @RequestParam(required = true) Integer productDay, @RequestParam(required = true) BigDecimal productMoney,
                                      @RequestParam(required = false) String phoneType, @RequestParam(required = false) String paramValue,
                                      @RequestParam(required = false) String phoneModel, @RequestParam(required = false) Integer phoneMemory) {
        Long uid = RequestThread.getUid();
        // 加锁: 防止忘记释放 给个ttl, 3个小时
        if (!redisMapper.lock(RedisConst.lock_user_order + uid, 3 * 60 * 60)) {
            return new ResultMessage(ResponseEnum.M4005);
        }

        // 检查当前商户下是否有未完成订单, 只要有未完成的直接返回
        if (orderService.countLoaningOrderByUid(uid) > 0) {
            return new ResultMessage(ResponseEnum.M4005);
        }

        //
        MerchantRate merchantRate = merchantRateService.selectByPrimaryKey(productId);
        if (null == merchantRate) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "未查到规则");
        }

        UserIdent userIdent = userIdentService.selectByPrimaryKey(uid);
        if (2 != userIdent.getRealName() || 2 != userIdent.getUserDetails() || 2 != userIdent.getBindbank()
                || 2 != userIdent.getMobile() || 2 != userIdent.getLiveness() || 2 != userIdent.getAlipay()
                || 2 != userIdent.getTaobao()) {
            // 提示认证未完成
            return new ResultMessage(ResponseEnum.M4000.getCode(), "认证未完成");
        }

        User user = userService.selectByPrimaryKey(uid);
        Blacklist blacklist = blacklistService.getByPhone(user.getUserPhone());
        if (null != blacklist) {
            // 黑名单
            if (2 == blacklist.getType()) {
                addOrder(uid, productId,
                        productMoney, phoneType, paramValue, phoneModel, phoneMemory, OrderEnum.AUTO_AUDIT_REFUSE.getCode(), new Date());
                return new ResultMessage(ResponseEnum.M2000);
            }
        }

        //公司、住宅地址是否包含拒绝关键字
        UserInfo userInfo = userService.selectUserInfo(user.getId());
        if (merchantConfigService.includeRejectKeyword(user.getMerchant(), userInfo)) {
            Blacklist blacklistInsert = new Blacklist();
            blacklistInsert.setUid(user.getId());
            blacklistInsert.setMerchant(user.getMerchant());
            blacklistInsert.setTel(user.getUserPhone());
            blacklistInsert.setCertNo(user.getUserCertNo());
            blacklistInsert.setName(user.getUserName());
            blacklistInsert.setType(2); //类型 1:灰名单(失效时间动态化） 2:永久黑名单  0:正常
            blacklistInsert.setTag(4);  //4-特殊行业
            blacklistInsert.setRemark(userInfo.getWorkCompany());
            blacklistInsert.setCreateTime(new Date());
            blacklistService.insert(blacklistInsert);
            addOrder(uid, productId,
                    productMoney, phoneType, paramValue, phoneModel, phoneMemory, OrderEnum.AUTO_AUDIT_REFUSE.getCode(), new Date());
            return new ResultMessage(ResponseEnum.M2000);
        }

        //灰名单客户直接进入人审
        if (blacklist != null && 1 == blacklist.getType()) {
            addOrder(uid, productId,
                    productMoney, phoneType, paramValue, phoneModel, phoneMemory, OrderEnum.WAIT_AUDIT.getCode(), new Date());
            return new ResultMessage(ResponseEnum.M2000);
        }

        MerchantConfig merchantConfig = merchantConfigService.selectByMerchant(user.getMerchant());
        if (merchantConfig != null) {
            //是否开启老客走风控
            if(merchantConfig.getOldCustomerRisk()!=null && merchantConfig.getOldCustomerRisk()==0){
                Integer borrowType = orderService.countPaySuccessByUid(uid);
                if (borrowType != null && borrowType > 0) {
                    // 老客户如果设置了老客静默天数，如果超过该天数则仍然需要风控
                    if (merchantConfig.getOldCustomerRiskRenewDay()==null){
                        addOrder(uid, productId, productMoney, phoneType, paramValue,
                                phoneModel, phoneMemory, OrderEnum.WAIT_LOAN.getCode(), new Date());
                        return new ResultMessage(ResponseEnum.M2000);
                    }else {
                        //设置了老客静默天数
                        Order userLatestOrder = orderService.findUserLatestOrder(uid);
                        //若在指定静默天数内，直接进入放款
                        if (TimeUtils.compareDate(userLatestOrder.getRealRepayTime(), merchantConfig.getOldCustomerRiskRenewDay())){
                            addOrder(uid, productId, productMoney, phoneType, paramValue,
                                    phoneModel, phoneMemory, OrderEnum.WAIT_LOAN.getCode(), new Date());
                            return new ResultMessage(ResponseEnum.M2000);
                        }
                    }
                }
            }
        }

        // 是否在整个系统有正在进行的订单(查询所有商户)
        String certNo = user.getUserCertNo();
        // 检查是否存在多头借贷
        if (dataCenterService.isMultiLoan(null, certNo, user.getMerchant())) {
            logger.info("存在多头借贷，无法提单， certNo={}", certNo);
            addOrder(uid, productId,
                    productMoney, phoneType, paramValue, phoneModel, phoneMemory, OrderEnum.AUTO_AUDIT_REFUSE.getCode(), new Date());
            return new ResultMessage(ResponseEnum.M2000);
        }

        Order order = addOrder(uid, productId,
                productMoney, phoneType, paramValue, phoneModel, phoneMemory, OrderEnum.DAI_FUKUAN.getCode(), null);
        // 发送消息，等待请求风控
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("orderId", order.getId());
        jsonObject.put("merchant", order.getMerchant());

        rabbitTemplate.convertAndSend(RabbitConst.queue_risk_order_notify, jsonObject);

        // 释放锁
        redisMapper.unlock(RedisConst.lock_user_order + uid);

        return new ResultMessage(ResponseEnum.M2000);
    }

    private Order addOrder(Long uid, Long productId, BigDecimal productMoney,
                           String phoneType, String paramValue,
                           String phoneModel, Integer phoneMemory, Integer status, Date auditTime) {
        Order order = new Order();
        MerchantRate merchantRate = merchantRateService.selectByPrimaryKey(productId);
        BigDecimal totalFee = MoneyUtil.totalFee(productMoney, merchantRate.getTotalRate());// 综合费用
        BigDecimal interestFee = MoneyUtil.interestFee(productMoney, merchantRate.getProductDay(),
                merchantRate.getProductRate());// 利息
        BigDecimal actualMoney = MoneyUtil.actualMoney(productMoney, totalFee);// 实际到账
        BigDecimal shouldRepay = MoneyUtil.shouldrepay(productMoney, interestFee, new BigDecimal(0),
                new BigDecimal(0));// 应还金额
        // 判断客群
        Integer userType = orderService.judgeUserTypeByUid(uid);
        order.setOrderNo(StringUtil.getOrderNumber("b"));
        order.setUid(uid);
        order.setBorrowDay(merchantRate.getProductDay());
        order.setBorrowMoney(productMoney);
        order.setActualMoney(actualMoney);
        order.setTotalRate(merchantRate.getTotalRate());
        order.setTotalFee(totalFee);
        order.setInterestRate(merchantRate.getProductRate());
        order.setOverdueDay(0);
        order.setOverdueFee(new BigDecimal(0));
        order.setOverdueRate(merchantRate.getOverdueRate());
        order.setInterestFee(interestFee);
        order.setShouldRepay(shouldRepay);
        order.setHadRepay(new BigDecimal(0));
        order.setReduceMoney(new BigDecimal(0));
        order.setCreateTime(new Date());
        order.setMerchant(RequestThread.getClientAlias());
        order.setProductId(productId);
        order.setUserType(userType);
        if (auditTime != null) {
            order.setAuditTime(new Date());
        }
        order.setStatus(status);
        OrderPhone orderPhone = new OrderPhone();
        orderPhone.setParamValue(paramValue);
        orderPhone.setPhoneModel(phoneModel + "|" + phoneMemory);
        orderPhone.setPhoneType(phoneType);
        orderService.addOrder(order, orderPhone);
        return order;
    }

    @LoginRequired
    @RequestMapping("/auto_apply_order")
    public ResultMessage autoApplyOrder() {
        String merchantAlias = RequestThread.getClientAlias();
        MerchantConfig merchantConfig = merchantConfigService.selectByMerchant(merchantAlias);
        int status = 1;
        if (null != merchantConfig && null != merchantConfig.getAutoApplyOrder()) {
            status = merchantConfig.getAutoApplyOrder();
        }
        JSONObject data = new JSONObject();
        data.put("status", status);
        return new ResultMessage(ResponseEnum.M2000, data);
    }

    @LoginRequired
    @RequestMapping("/compute_quota")
    public ResultMessage compute_quota(String productMoney, String totalRate) {
        BigDecimal totalFee = MoneyUtil.totalFee(new BigDecimal(productMoney), new BigDecimal(totalRate));// 综合费用
        BigDecimal actualMoney = MoneyUtil.actualMoney(new BigDecimal(productMoney), totalFee);// 实际到账

        JSONObject data = new JSONObject();
        data.put("productMoney", productMoney);
        data.put("totalFee", totalFee);
        data.put("actualMoney", actualMoney);
        return new ResultMessage(ResponseEnum.M2000, data);
    }

    @LoginRequired
    @RequestMapping("/check_pay")
    public ResultMessage check_pay() {
        Long uid = RequestThread.getUid();

        Merchant merchant = merchantService.findMerchantByAlias(RequestThread.getClientAlias());
        UserBank userBank = userBankService.selectUserMerchantBankCard(uid, merchant.getBindType());
        if (null == userBank) {
            return new ResultMessage(ResponseEnum.M4000.getCode(), "支付通道异常，请重新绑卡");
        }
        return new ResultMessage(ResponseEnum.M2000);
    }

}
