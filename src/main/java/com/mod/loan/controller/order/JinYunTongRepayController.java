package com.mod.loan.controller.order;

import com.alibaba.fastjson.JSON;
import com.mod.loan.common.annotation.LoginRequired;
import com.mod.loan.common.model.ResultMessage;
import com.mod.loan.service.OrderJinYunTongRePayService;
import com.mod.loan.task.OrderJinYunTongRepayQueryTask;
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
    private OrderJinYunTongRePayService orderJinYunTongRePayService;
    @Autowired
    private OrderJinYunTongRepayQueryTask orderJinYunTongRepayQueryTask;

    /**
     * 金运通还款回调
     *
     * @Author actor
     * @Date 2019/6/20 15:04
     */
    @RequestMapping("orderRepayNotice")
    public String jinyuntongOrderRepayNotice(@RequestBody Map map) {
        log.info("金运通还款回调,request={}", JSON.toJSONString(map));
        return orderJinYunTongRePayService.jinyuntongOrderRepayNotice(map);
    }

    /**
     * 还款
     *
     * @Author actor
     * @Date 2019/6/21 16:57
     */
    @RequestMapping("repay")
    @LoginRequired
    public ResultMessage rePay(Long orderId) {
        log.info("金运通还款开始:orderId={}",orderId);
        return orderJinYunTongRePayService.orderRepay(orderId);
    }

    /**
     * 还款查询
     *
     * @Author actor
     * @Date 2019/6/21 17:01
     */
    @RequestMapping("queryRepay")
    @LoginRequired
    public void queryRepay(String repayNo) {
        log.info("金运通还款查询,repayNo={}",repayNo);
        orderJinYunTongRePayService.queryRePayStatus(repayNo);
    }
    /**
    * 测试查询还款定时任务
    * @Author actor
    * @Date 2019/6/25 9:41
    */
    @RequestMapping("testQuery")
    public void testQuery(){
        orderJinYunTongRepayQueryTask.jinyuntongRepayQuery();
    }
}
