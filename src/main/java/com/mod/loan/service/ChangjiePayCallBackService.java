package com.mod.loan.service;

import javax.servlet.http.HttpServletRequest;

/**
 * @author NIELIN
 * @version $Id: ChangjiePayCallBackService.java, v 0.1 2019/1/17 16:08 NIELIN Exp $
 */
public interface ChangjiePayCallBackService {

    /**
     * 单笔代付放款异步回调
     *
     * @param request
     */
    void payCallback(HttpServletRequest request);
}
