package com.lc.common.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.lc.common.model.entity.User;

/**
 * 用户服务
 *
 * @author Lc
 */
public interface InnerUserService  {

    /**
     * 数据库中是否已经分配给用户密钥
     * @param accessKey
     * @return
     */
    User getInvokeUser(String accessKey);
}
