package com.mod.loan.mapper;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.Blacklist;
import org.apache.ibatis.annotations.Param;

public interface BlacklistMapper extends MyBaseMapper<Blacklist> {

    Blacklist getByUid(Long uid);

    Blacklist getByPhone(@Param("tel") String phone);
}