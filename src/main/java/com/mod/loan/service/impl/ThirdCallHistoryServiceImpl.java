//package com.mod.loan.service.impl;
//
//import com.mod.loan.common.mapper.BaseServiceImpl;
//import com.mod.loan.mapper.ThirdCallHistoryMapper;
//import com.mod.loan.model.ThirdCallHistory;
//import com.mod.loan.service.ThirdCallHistoryService;
//import org.apache.commons.httpclient.util.DateUtil;
//import org.apache.commons.lang.time.DateUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.Resource;
//import java.util.Collections;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * @author actor
// * @date 2019/7/5 11:47
// */
//@Service
//public class ThirdCallHistoryServiceImpl extends BaseServiceImpl<ThirdCallHistory,Integer> implements ThirdCallHistoryService {
//
//    public static final Logger logger = LoggerFactory.getLogger(ThirdCallHistoryServiceImpl.class);
//
//
//    @Resource
//    private ThirdCallHistoryMapper thirdCallHistoryMapper;
//
//    public void addCount(String merchant,String code,Long uid){
//        try {
//            Date date = new Date();
//            ThirdCallHistory thirdCallHistory=new ThirdCallHistory();
//            thirdCallHistory.setMerchant(merchant);
//            thirdCallHistory.setCode(code);
//            thirdCallHistory.setUid(uid);
//            thirdCallHistory.setCreateTime(date);
//            thirdCallHistory.setDay(DateUtil.formatDate(date,"yyyy-MM-dd"));
//            thirdCallHistoryMapper.insertSelective(thirdCallHistory);
//        }catch (Exception e){
//            logger.error("插入第三方调用异常:uid={},code={},merchant={}",uid,code,merchant);
//        }
//    }
//}
