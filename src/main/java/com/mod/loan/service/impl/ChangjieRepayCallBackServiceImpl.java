package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.enums.ChangjiePayCallBackStatusEnum;
import com.mod.loan.common.enums.ChangjieRePayCallBackStatusEnum;
import com.mod.loan.common.enums.OrderEnum;
import com.mod.loan.common.enums.OrderRepayStatusEnum;
import com.mod.loan.model.Merchant;
import com.mod.loan.model.Order;
import com.mod.loan.model.OrderRepay;
import com.mod.loan.service.*;
import com.mod.loan.util.changjie.BaseConstant;
import com.mod.loan.util.changjie.ChanPayUtil;
import com.mod.loan.util.changjie.RSA;
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
 * @version $Id: ChangjieRepayCallBackServiceImpl.java, v 0.1 2019/1/17 16:08 NIELIN Exp $
 */
@Service
public class ChangjieRepayCallBackServiceImpl implements ChangjieRepayCallBackService {
    private static Logger logger = LoggerFactory.getLogger(ChangjieRepayCallBackServiceImpl.class);

    @Autowired
    MerchantService merchantService;
    @Autowired
    OrderRepayService orderRepayService;
    @Autowired
    OrderService orderService;
    @Autowired
    UserService userService;

    @Override
    public void repayCallback(HttpServletRequest request) {
        String outerTradeNo = request.getParameter("outer_trade_no");
        String tradeStatus = request.getParameter("trade_status");
        String sign = request.getParameter("sign");
        logger.info("#[单笔代扣还款异步回调-还款订单流水号、状态]-outerTradeNo={},tradeStatus={},sign={}", outerTradeNo, tradeStatus, sign);
        //根据流水号查询还款订单信息
        OrderRepay orderRepay = orderRepayService.selectByPrimaryKey(outerTradeNo);
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
            if (null == merchant || StringUtils.isEmpty(merchant.getCjPartnerId()) || StringUtils.isEmpty(merchant.getCjPublicKey()) || StringUtils.isEmpty(merchant.getCjMerchantPrivateKey())) {
                logger.info("#[该商户信息异常]-merchant={}", JSONObject.toJSON(merchant));
                return;
            }
            Map<String, String> map = new HashMap();
            //单笔代付放款异步回调-业务参数(post--request.getParameterMap()拿不到)
            map.put("notify_id", request.getParameter("notify_id"));
            map.put("notify_type", request.getParameter("notify_type"));
            map.put("notify_time", request.getParameter("notify_time"));
            map.put("_input_charset", request.getParameter("_input_charset"));
            map.put("version", request.getParameter("version"));
            map.put("outer_trade_no", request.getParameter("outer_trade_no"));
            map.put("inner_trade_no", request.getParameter("inner_trade_no"));
            map.put("trade_amount", request.getParameter("trade_amount"));
            map.put("trade_status", request.getParameter("trade_status"));
            map.put("gmt_create", request.getParameter("gmt_create"));
            map.put("gmt_payment", request.getParameter("gmt_payment"));
            map.put("gmt_close", request.getParameter("gmt_close"));
            if (!StringUtils.isEmpty(request.getParameter("extension"))) {
                map.put("extension", request.getParameter("extension"));
            }
            //把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
            String prestr = ChanPayUtil.createLinkString(map, false);
            logger.info("#[把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串]-prestr={}", prestr);
            //验签
            boolean flag = true;
            try {
                flag = RSA.verify(prestr, sign, merchant.getCjPublicKey(), BaseConstant.CHARSET);
            } catch (Exception e) {
                logger.error("#[验签异常]-e={}", e);
                return;
            }
            if (flag) {
                //成功
                if (ChangjieRePayCallBackStatusEnum.TRADE_SUCCESS.getCode().equals(tradeStatus) || ChangjieRePayCallBackStatusEnum.TRADE_FINISHED.getCode().equals(tradeStatus)) {
                    //更新repay
                    orderRepay.setRepayStatus(OrderRepayStatusEnum.REPAY_SUCCESS.getCode());
                    orderRepay.setRemark("畅捷代扣还款成功");
                    orderRepay.setUpdateTime(new Date());

                    //更新order
                    order.setRealRepayTime(new Date());
                    order.setHadRepay(orderRepay.getRepayMoney());
                    order.setStatus(orderService.setRepaySuccStatusByCurrStatus(order.getStatus()));
                    orderRepayService.updateOrderRepayInfo(orderRepay, order);
                } else if (ChangjiePayCallBackStatusEnum.WITHDRAWAL_FAIL.getCode().equals(tradeStatus)) {
                    orderRepay.setRepayStatus(OrderRepayStatusEnum.REPAY_FAILED.getCode());
                    orderRepay.setRemark("畅捷代扣还款失败");
                    orderRepay.setUpdateTime(new Date());
                    orderRepayService.updateOrderRepayInfo(orderRepay, null);
                }
            }
        }
    }
}
