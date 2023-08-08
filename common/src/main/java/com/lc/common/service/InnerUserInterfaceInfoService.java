package com.lc.common.service;


/**
 *
 */

public interface InnerUserInterfaceInfoService  {

    /**
     * 每次用户调用接口调整使用次数
     * @param userId
     * @param interfaceInfoId
     * @return
     */
    boolean invokeCount(long userId, long interfaceInfoId);
}
