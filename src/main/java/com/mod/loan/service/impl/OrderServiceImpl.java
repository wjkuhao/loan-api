package com.mod.loan.service.impl;

import java.util.Date;
import java.util.List;

import com.mod.loan.common.enums.OrderEnum;
import com.mod.loan.model.User;
import com.mod.loan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mod.loan.common.mapper.BaseServiceImpl;
import com.mod.loan.mapper.OrderMapper;
import com.mod.loan.mapper.OrderPayMapper;
import com.mod.loan.mapper.OrderPhoneMapper;
import com.mod.loan.model.Order;
import com.mod.loan.model.OrderPay;
import com.mod.loan.model.OrderPhone;
import com.mod.loan.service.OrderService;

@Service
public class OrderServiceImpl  extends BaseServiceImpl<Order,Long> implements OrderService {

	@Autowired
	OrderMapper orderMapper;
	@Autowired
	OrderPhoneMapper orderPhoneMapper;
	@Autowired
	OrderPayMapper orderPayMapper;
	@Autowired
    UserService userService;

	@Override
	public int countLoaningOrderByUid(Long uid) {
		return orderMapper.countLoaningOrderByUid(uid);
	}

	@Override
	public Order findUserLatestOrder(Long uid) {
		return orderMapper.findUserLatestOrder(uid);
	}

	@Override
	public List<Order> getByUid(Long uid) {
		return orderMapper.getByUid(uid);
	}

	@Override
	public int addOrder(Order order, OrderPhone orderPhone) {
		orderMapper.insertSelective(order);
		orderPhone.setOrderId(order.getId());
		return orderPhoneMapper.insertSelective(orderPhone);
	}

	@Override
	public OrderPhone findOrderPhoneByOrderId(Long orderId) {
		return orderPhoneMapper.selectByPrimaryKey(orderId);
	}

	@Override
	public OrderPay findOrderPaySuccessRecord(Long orderId) {
		return orderPayMapper.selectByOrderIdAndStatus(orderId,3);
	}

	@Override
	public Integer judgeUserTypeByUid(Long uid) {
		return orderMapper.judgeUserTypeByUid(uid);
	}

	@Override
	public Integer countByUid(Long uid) {
		Order order = new Order();
		order.setUid(uid);
		return orderMapper.selectCount(order);
	}

	@Override
	public Integer countPaySuccessByUid(Long uid){
		return orderMapper.countPaySuccessByUid(uid);
	}

    @Override
    public List<Order> findOverdueOrder() {
	    return orderMapper.findOverdueOrder();
    }

	@Override
	public Order findOverdueByCertNo(String certNo) {
		List<User> users = userService.selectUserByCertNo(certNo);
		for (User user : users) {
			Order overdueOrder = orderMapper.findOneOverdueOrder(user.getId());
			if (overdueOrder!=null) {
				return overdueOrder;
			}
		}
		return null;
	}

    @Override
    public Order findOverdueByPhone(String phone) {
        List<User> users = userService.selectUserByPhone(phone);
        for (User user : users) {
            Order overdueOrder = orderMapper.findOneOverdueOrder(user.getId());
            if (overdueOrder!=null) {
                return overdueOrder;
            }
        }
        return null;
    }

    @Override
    public boolean checkUnfinishOrderByPhone(String phone) {
        List<User> users = userService.selectUserByPhone(phone);
		return checkUnfinishOrder(users);
    }

	@Override
	public boolean checkUnfinishOrderByCertNo(String certNo) {
		List<User> users = userService.selectUserByCertNo(certNo);
		return checkUnfinishOrder(users);
	}

	private boolean checkUnfinishOrder(List<User> users) {
		for (User user : users) {
			List<Order> orderList = orderMapper.getByUid(user.getId());
			for (Order order : orderList) {
				if(order.getStatus() < 40){
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int setRepaySuccStatusByCurrStatus(Integer status) {
		if (OrderEnum.OVERDUE.getCode().equals(status) || OrderEnum.BAD_DEBTS.getCode().equals(status)) {
			return OrderEnum.OVERDUE_REPAY.getCode();
		} else if (OrderEnum.DEFER.getCode().equals(status) || OrderEnum.OVERDUE_DEFER.getCode().equals(status)
				|| OrderEnum.DEFER_OVERDUE.getCode().equals(status)){
			return OrderEnum.DEFER_REPAY.getCode();
		}else {
			return OrderEnum.NORMAL_REPAY.getCode();
		}
	}

	@Override
	public void updatePayCallbackInfo(Order order, OrderPay orderPay) {
		orderMapper.updateByPrimaryKeySelective(order);
		orderPayMapper.updateByPrimaryKeySelective(orderPay);
	}

	@Override
	public void updatePayConfirmLoan(Long orderId) {
		Order order = new Order();
		order.setId(orderId);
		order.setStatus(OrderEnum.WAIT_LOAN.getCode());
		orderMapper.updateByPrimaryKeySelective(order);
	}

}
