package com.mod.loan.mapper;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.AppEntry;

import java.util.List;

public interface AppEntryMapper extends MyBaseMapper<AppEntry> {

    List<AppEntry> findEntryList(String merchant);

}