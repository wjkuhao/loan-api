package com.mod.loan.util.kuaiqian.common;


import com.mod.loan.util.kuaiqian.notice.NotifyHead;
import com.mod.loan.util.kuaiqian.notice.NotifyResponse;
import com.mod.loan.util.kuaiqian.notice.NotifyResponseBody;

/**
 * 工具类
 *
 * @author zhiwei.ma
 */
public class CCSUtil {

    /**
     * 创建通知response
     * @param membercode_head
     * @param version
     * @return
     */
    public static NotifyResponse genNoticeResponse(String membercode_head , String version){
        NotifyResponse response = new NotifyResponse();
        NotifyHead head = new NotifyHead();
        head.setMemberCode(membercode_head);
        head.setVersion(version);
        NotifyResponseBody responseBody = new NotifyResponseBody();
        SealDataType sealDataType = new SealDataType();
        responseBody.setSealDataType(sealDataType);
        responseBody.setIsReceived("1");
        response.setNotifyHead(head);
        response.setNotifyResponseBody(responseBody);
        return response;
    }
}
