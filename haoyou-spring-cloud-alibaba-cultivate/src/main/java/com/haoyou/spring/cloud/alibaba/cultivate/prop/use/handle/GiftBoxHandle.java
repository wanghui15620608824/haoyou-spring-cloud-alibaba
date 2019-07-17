package com.haoyou.spring.cloud.alibaba.cultivate.prop.use.handle;

import cn.hutool.core.lang.WeightRandom;
import cn.hutool.core.util.RandomUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.PropUseMsg;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/5 10:48
 *
 * 宠物蛋孵化类
 */
@Service
public class GiftBoxHandle extends PeopUseHandle {
    @Override
    protected void setHandleType() {
        this.handleType = "GiftBox";
    }

    @Override
    public int handle(PropUseMsg propUseMsg) {

        User user = propUseMsg.getUser();
        Prop prop = propUseMsg.getProp();
        String awardType = prop.getProperty2();

        String key = RedisKeyUtil.getKey(RedisKey.AWARD, awardType);
        Award award = redisObjectUtil.get(key, Award.class);


        rewardService.doAward(user,award);


        return ResponseMsg.MSG_SUCCESS;
    }
}