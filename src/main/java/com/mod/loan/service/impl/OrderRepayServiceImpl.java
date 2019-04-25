package com.mod.loan.service.impl;

import com.mod.loan.common.enums.OrderRepayStatusEnum;
import com.mod.loan.common.mapper.BaseServiceImpl;
import com.mod.loan.config.Constant;
import com.mod.loan.controller.order.YeepayRepayController;
import com.mod.loan.mapper.OrderMapper;
import com.mod.loan.mapper.OrderRepayMapper;
import com.mod.loan.model.Merchant;
import com.mod.loan.model.Order;
import com.mod.loan.model.OrderRepay;
import com.mod.loan.model.UserBank;
import com.mod.loan.service.MerchantService;
import com.mod.loan.service.OrderRepayService;
import com.mod.loan.service.UserBankService;
import com.mod.loan.service.YeepayService;
import com.mod.loan.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class OrderRepayServiceImpl extends BaseServiceImpl<OrderRepay,String>  implements OrderRepayService {

	private static Logger logger = LoggerFactory.getLogger(YeepayRepayController.class);

	@Autowired
	OrderRepayMapper orderRepayMapper;
	@Autowired
	OrderMapper orderMapper;
	@Autowired
    MerchantService merchantService;
	@Autowired
    UserBankService userBankService;
	@Autowired
    YeepayService yeepayService;

	@Override
	public void updateOrderRepayInfo( OrderRepay orderRepay,Order order){
		orderRepayMapper.updateByPrimaryKeySelective(orderRepay);
		orderMapper.updateByPrimaryKeySelective(order);
	}

	@Override
	public int countRepaySuccess(Long orderId) {
		return orderRepayMapper.countRepaySuccess(orderId);
	}

	@Override
	public String yeepayRepayNoSms(Long orderId) {

		if (countRepaySuccess(orderId) >= 1) {
			logger.error("orderId={}已存在还款中的记录", orderId);
			return "请勿重复还款";
		}

		Order order = orderMapper.selectByPrimaryKey(orderId);
		if (order.getStatus() == 31 || order.getStatus() == 33 || order.getStatus() == 34) { // 已放款，逾期，坏账状态
			try {
				String repayNo = StringUtil.getOrderNumber("r");// 支付流水号
				String amount = "dev".equals(Constant.ENVIROMENT)?"0.11":order.getShouldRepay().toString();
				Merchant merchant = merchantService.findMerchantByAlias(order.getMerchant());
				UserBank userBank = userBankService.selectUserCurrentBankCard(order.getUid());

				String err = yeepayService.payRequest(merchant.getYeepay_repay_appkey(), merchant.getYeepay_repay_private_key(),
						repayNo, String.valueOf(order.getUid()), userBank.getCardNo(), amount, false);

				// 还款记录表
				OrderRepay orderRepay = new OrderRepay();
				orderRepay.setRepayNo(repayNo);
				orderRepay.setUid(order.getUid());
				orderRepay.setOrderId(order.getId());
				orderRepay.setRepayType(1); //1-银行卡
				orderRepay.setRepayMoney(new BigDecimal(amount));
				orderRepay.setBank(userBank.getCardName());
				orderRepay.setBankNo(userBank.getCardNo());
				orderRepay.setCreateTime(new Date());
				orderRepay.setUpdateTime(new Date());
				orderRepay.setRepayStatus(OrderRepayStatusEnum.INIT.getCode());//初始状态

				if(err!=null){
					orderRepay.setRepayStatus(OrderRepayStatusEnum.ACCEPT_FAILED.getCode());
					orderRepay.setRemark("易宝受理失败:" + err);
                    orderRepayMapper.insertSelective(orderRepay);
					return err;
				}

				orderRepay.setRepayStatus(OrderRepayStatusEnum.ACCEPT_SUCCESS.getCode());
				orderRepay.setRemark("易宝受理成功");
                orderRepayMapper.insertSelective(orderRepay);
				return null;
			} catch (Exception e) {
				logger.error("易宝代付受理异常，error={}", e.getMessage());
				return "易宝代付受理异常";
			}
		}
		return "订单状态异常";
	}
}
