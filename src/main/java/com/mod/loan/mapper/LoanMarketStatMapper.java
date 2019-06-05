package com.mod.loan.mapper;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.LoanMarketStat;

public interface LoanMarketStatMapper extends MyBaseMapper<LoanMarketStat> {

    /**
     * 更新PV\UV统计
     */
    int updateLoanMarketStatById(LoanMarketStat loanMarketStat);

    /**
     * 更新PV统计
     */
    int updatePvTotalById(LoanMarketStat loanMarketStat);

}