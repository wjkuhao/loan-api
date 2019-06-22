package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.mapper.BaseServiceImpl;
import com.mod.loan.config.Constant;
import com.mod.loan.mapper.OrderRiskInfoMapper;
import com.mod.loan.model.OrderRiskInfo;
import com.mod.loan.service.OrderRiskInfoService;
import com.mod.loan.util.OkHttpReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("orderRiskService")
public class OrderRiskInfoServiceImpl extends BaseServiceImpl<OrderRiskInfo, Long> implements OrderRiskInfoService {

    private final OrderRiskInfoMapper orderRiskInfoMapper;
    private final OkHttpReader okHttpReader;

    private static Logger logger = LoggerFactory.getLogger(OrderRiskInfoServiceImpl.class);


    @Autowired
    public OrderRiskInfoServiceImpl(OrderRiskInfoMapper orderRiskInfoMapper, OkHttpReader okHttpReader) {
        this.orderRiskInfoMapper = orderRiskInfoMapper;
        this.okHttpReader = okHttpReader;
    }

    @Override
    public OrderRiskInfo getLastOneByOrderId(Long orderId) {
        return orderRiskInfoMapper.getLastOneByOrderId(orderId);
    }

    @Override
    public OrderRiskInfo getLastOneByPhone(String userPhone) {
        return orderRiskInfoMapper.getLastOneByPhone(userPhone);
    }

    /**
     * 更新风控模型分到order_risk_info, 并返回
     * @param id 风控订单id
     * @return 风控模型分
     */
    @Override
    public String updateRiskMotelScore(Long id){
        try {
            OrderRiskInfo orderRiskInfo = orderRiskInfoMapper.selectByPrimaryKey(id);
            //风控id格式 R2-id
            String riskId = orderRiskInfo.getRiskId();
            String[] riskIds = riskId.split("-");
            if (riskIds.length<2){
                return null;
            }
            String reportId = riskIds[1];

            String riskModelScoreUrl = Constant.MX_RISK_API_MODEL_SCORE_URL;
            String getParam = String.format(riskModelScoreUrl, reportId, Constant.MX_RISK_TOKEN);

            String result = okHttpReader.get(getParam,null, null);
            JSONObject resultJson = JSON.parseObject(result);
            if (resultJson.getInteger("status")==200) {
                String riskModelScore = resultJson.getString("data");
                orderRiskInfo.setRiskModelScore(riskModelScore);
                orderRiskInfoMapper.updateByPrimaryKeySelective(orderRiskInfo);
                return riskModelScore;
            }else {
                logger.error("get error, riskInfoId={}, err={}", id, result);
            }
        }
        catch (Exception e){
            logger.error("getRiskMotelScore error, riskInfoId={}, err={}", id, e.getStackTrace());
        }
        return null;
    }
}
