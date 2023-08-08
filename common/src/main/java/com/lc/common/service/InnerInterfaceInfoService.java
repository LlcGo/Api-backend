package com.lc.common.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.lc.common.model.entity.InterfaceInfo;

/**
 *
 */
public interface InnerInterfaceInfoService  {
    /**
     * 查询是否有这个接口
     * @param path
     * @param method
     * @return
     */
    InterfaceInfo getInterfaceInfoByPath(String path,String method);


}
