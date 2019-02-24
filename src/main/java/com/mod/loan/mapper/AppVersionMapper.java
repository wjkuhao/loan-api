package com.mod.loan.mapper;

import org.apache.ibatis.annotations.Param;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.AppVersion;

public interface AppVersionMapper extends MyBaseMapper<AppVersion> {
	
	AppVersion findNewVersion(@Param("versionAlias")String versionAlias, @Param("versionType")String versionType);
}