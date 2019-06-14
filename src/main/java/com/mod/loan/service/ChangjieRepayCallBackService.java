package com.mod.loan.service;

import javax.servlet.http.HttpServletRequest;

/**
 * @author NIELIN
 * @version $Id: ChangjieRepayCallBackService.java, v 0.1 2019/1/17 16:08 NIELIN Exp $
 */
public interface ChangjieRepayCallBackService {

    /**
     * 单笔代扣还款异步回调
     *
     * @param request
     * @throws Exception
     */
    void repayCallback(HttpServletRequest request);
}
