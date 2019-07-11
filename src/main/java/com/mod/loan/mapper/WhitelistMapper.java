package com.mod.loan.mapper;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.Whitelist;
import org.apache.ibatis.annotations.Param;

public interface WhitelistMapper extends MyBaseMapper<Whitelist> {

    Whitelist getByPhone(@Param("tel") String phone);
}