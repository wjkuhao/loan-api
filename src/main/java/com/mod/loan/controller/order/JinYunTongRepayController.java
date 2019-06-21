package com.mod.loan.controller.order;

import com.alibaba.fastjson.JSON;
import com.mod.loan.common.annotation.LoginRequired;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.service.OrderJInYunTongRePayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("jinyuntong")
public class JinYunTongRepayController {
    private static Logger log = LoggerFactory.getLogger(JinYunTongRepayController.class);

    @Autowired
    private OrderJInYunTongRePayService orderJInYunTongRePayService;

    /**
     * 金运通还款回调
     *
     * @Author actor
     * @Date 2019/6/20 15:04
     */
    @RequestMapping("orderRepayNotice")
    public String jinyuntongOrderRepayNotice(@RequestBody Map map) {
        log.info("金运通还款回调,request={}", JSON.toJSONString(map));
        return orderJInYunTongRePayService.jinyuntongOrderRepayNotice(map);
    }

    /**
    * 换款测试
    * @Author actor
    * @Date 2019/6/21 16:57
    */
    @RequestMapping("repay")
    @LoginRequired
    public ResultMessage rePay(Long orderId){
        return orderJInYunTongRePayService.orderRepay(orderId);
    }

    /**
    * 还款查询测试
    * @Author actor
    * @Date 2019/6/21 17:01
    */
    @RequestMapping("queryRepay")
    @LoginRequired
    public void queryRepay(String repayNo){
         orderJInYunTongRePayService.queryRePayStatus(repayNo);
    }
}
