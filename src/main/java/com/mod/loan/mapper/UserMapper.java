package com.mod.loan.mapper;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper extends MyBaseMapper<User> {
    List<User> selectAllByCertNo(@Param("certNo") String certNo);
}