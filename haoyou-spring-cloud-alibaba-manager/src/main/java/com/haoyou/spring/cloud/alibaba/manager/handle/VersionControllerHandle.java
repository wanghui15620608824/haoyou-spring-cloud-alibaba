package com.haoyou.spring.cloud.alibaba.manager.handle;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.domain.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.entity.VersionControl;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.VersionReqMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 版号校验
 */
@Service
@RefreshScope
public class VersionControllerHandle extends ManagerHandle {

    private static final long serialVersionUID = -3411252494816691844L;
    private static final Logger logger = LoggerFactory.getLogger(VersionControllerHandle.class);

    @Override
    protected void setHandleType() {
        this.handleType = ManagerHandle.VERSION_CONTROLLER;
    }


    @Override
    public BaseMessage handle(MyRequest req) {
        //TODO 版本校验，并传输文件下载地址
        MapBody mapBody = new MapBody<>();
        HashMap<String, VersionControl> stringObjectHashMap = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.VERSION), VersionControl.class);
        VersionControl latest=null;
        for(VersionControl versionControl:stringObjectHashMap.values()){
            if(latest!=null){
                if(versionControl.getDate().getTime()>latest.getDate().getTime()){
                    latest=versionControl;
                }
            }else{
                latest=versionControl;
            }
        }

        mapBody.setState(ResponseMsg.MSG_SUCCESS);
        mapBody.put("newVersions", latest);
        logger.debug(String.format("获取新的版本：%s", mapBody));
        return mapBody;

    }

}