package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.enums.OrderEnum;
import com.mod.loan.common.enums.OrderRepayStatusEnum;
import com.mod.loan.model.Merchant;
import com.mod.loan.model.Order;
import com.mod.loan.model.OrderRepay;
import com.mod.loan.service.*;
import com.mod.loan.util.huichao.HuichaoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author NIELIN
 * @version $Id: HuichaoRepayCallBackServiceImpl.java, v 0.1 2019/1/17 16:08 NIELIN Exp $
 */
@Service
public class HuichaoRepayCallBackServiceImpl implements HuichaoRepayCallBackService {
    private static Logger logger = LoggerFactory.getLogger(HuichaoRepayCallBackServiceImpl.class);

    @Autowired
    MerchantService merchantService;
    @Autowired
    OrderRepayService orderRepayService;
    @Autowired
    OrderService orderService;
    @Autowired
    UserService userService;

    @Override
    public void aliAppH5OrWxScanRepayCallback(HttpServletRequest request) throws Exception {
        //订单号
        String merchantOutOrderNo = request.getParameter("merchantOutOrderNo");
        //商户号
        String merid = request.getParameter("merid");
        //订单详情
        String msg = request.getParameter("msg");
        //随机字符串，和商户下单时传的一致
        String noncestr = request.getParameter("noncestr");
        //第三方平台订单号
        String orderNo = request.getParameter("orderNo");
        //支付结果
        String payResult = request.getParameter("payResult");
        //签名
        String sign = request.getParameter("sign");
        //和下单时所填id字段一致，下单时未传则为空
        String id = request.getParameter("id");
        //支付宝订单支付宝渠道才会有
        String aliNo = request.getParameter("aliNo");
        logger.info("#[app端支付宝还款异步回调-还款订单流水号、状态]-merchantOutOrderNo={},payResult={}", merchantOutOrderNo, payResult);
        //根据流水号查询还款订单信息
        OrderRepay orderRepay = orderRepayService.selectByPrimaryKey(merchantOutOrderNo);
        logger.info("#[根据流水号查询还款订单信息]-orderRepay={}", JSONObject.toJSON(orderRepay));
        if (null == orderRepay) {
            logger.info("根据流水号查询还款订单信息为空");
            return;
        }
        //幂等
        if (1 != orderRepay.getRepayStatus()) {
            logger.info("该笔订单的还款流水状态不是受理成功");
            return;
        }
        //根据订单号获取商户别名
        Order order = orderService.selectByPrimaryKey(orderRepay.getOrderId());
        logger.info("#[根据订单号获去订单信息]-order={}", JSONObject.toJSON(order));
        if (null == order) {
            logger.info("根据订单号获去订单信息为空");
            return;
        }
        //幂等--还款中30～40
        if (order.getStatus() >= OrderEnum.REPAYING.getCode() && order.getStatus() < OrderEnum.NORMAL_REPAY.getCode()) {
            //获取商户信息
            Merchant merchant = merchantService.findMerchantByAlias(order.getMerchant());
            if (null == merchant || StringUtils.isEmpty(merchant.getHuichaoMerid()) || StringUtils.isEmpty(merchant.getHuichaoPublicKey()) || StringUtils.isEmpty(merchant.getHuichaoMerchantPayPrivateKey()) || StringUtils.isEmpty(merchant.getHuichaoMerchantRepayPrivateKey())) {
                logger.info("#[该商户信息异常]-merchant={}", JSONObject.toJSON(merchant));
                return;
            }
            //拼接签名参数
            Map<String, String> signParamMap = new HashMap<String, String>();
            signParamMap.put("merchantOutOrderNo", merchantOutOrderNo);
            signParamMap.put("merid", merid);
            signParamMap.put("msg", msg);
            signParamMap.put("noncestr", noncestr);
            signParamMap.put("orderNo", orderNo);
            signParamMap.put("payResult", payResult);
            //转换为key=value模式
            String signParam = HuichaoUtil.formatUrlMap(signParamMap, false, false);
            //生成签名
            String signLocal = HuichaoUtil.getMD5(signParam + "&key=" + merchant.getHuichaoMerchantRepayPrivateKey());
            //验签
            if (signLocal.equals(sign)) {
                //成功
                if ("1".equals(payResult)) {
                    //更新repay
                    orderRepay.setRepayStatus(OrderRepayStatusEnum.REPAY_SUCCESS.getCode());
                    orderRepay.setRemark("汇潮还款成功");
                    orderRepay.setUpdateTime(new Date());

                    //更新order
                    order.setRealRepayTime(new Date());
                    order.setHadRepay(orderRepay.getRepayMoney());
                    order.setStatus(orderService.setRepaySuccStatusByCurrStatus(order.getStatus()));
                    orderRepayService.updateOrderRepayInfo(orderRepay, order);
                }
            }
        }
    }
}
