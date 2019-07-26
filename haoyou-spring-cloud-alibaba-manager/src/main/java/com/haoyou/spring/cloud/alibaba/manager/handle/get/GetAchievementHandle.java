package com.haoyou.spring.cloud.alibaba.manager.handle.get;


import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 获取成就系统
 */
@Service
public class GetAchievementHandle extends ManagerHandle {


    private static final Logger logger = LoggerFactory.getLogger(GetAchievementHandle.class);
    private static final long serialVersionUID = -9187389077840416711L;

    @Override
    protected void setHandleType() {
        this.handleType = SendType.GET_RANK;
    }

    @Override
    public BaseMessage handle(MyRequest req) {
        User user = req.getUser();
        MapBody mapBody = new MapBody<>();

        List<Map> achievementsMsg = new ArrayList<>();

        HashMap<String, Achievement> stringAchievementHashMap = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.ACHIEVEMENT), Achievement.class);
        //所有成就
        for (Achievement achievement : stringAchievementHashMap.values()) {

            Map<String, Object> achievementMsg = new HashMap<>();
            //成就名称
            achievementMsg.put("name", achievement.getName());
            //成就中文名称
            achievementMsg.put("l10n", achievement.getL10n());
            //成就介绍
            achievementMsg.put("description", achievement.getDescription());

            String lkKey = RedisKeyUtil.getlkKey(RedisKey.USER_AWARD, user.getUid(), RedisKey.ACHIEVEMENT, achievement.getName());
            //是否有待领取的成就奖励
            HashMap<String, Award> stringAwardHashMap = redisObjectUtil.getlkMap(lkKey, Award.class);
            if (!stringAwardHashMap.isEmpty()) {
                TreeMap<Integer, Award> awardsTreeMap = new TreeMap<>();
                for (Map.Entry<String, Award> entry : stringAwardHashMap.entrySet()) {
                    if (!entry.getValue().isUsed()) {
                        String[] keys = entry.getKey().split(":");
                        awardsTreeMap.put(Integer.valueOf(keys[keys.length - 1]), entry.getValue());
                    } else {
                        redisObjectUtil.delete(entry.getKey());
                    }
                }
                //获取最靠前的奖励
                Map.Entry<Integer, Award> firstEntry = awardsTreeMap.firstEntry();
                achievementMsg.put("award", firstEntry.getValue());

            }
            //无带领取的奖励
            else {
                UserNumerical userNumerical = user.getUserNumericalMap().get(achievement.getNumericalName());
                if(userNumerical!=null){
                    //当前数值
                    achievementMsg.put("numerical", userNumerical.getValue());

                    List<AchievementAims> achievementAims = achievement.getAchievementAims();

                    for (AchievementAims achievementAim : achievementAims) {
                        if (achievementAim.getAim() > userNumerical.getValue()) {
                            //当前目标
                            achievementMsg.put("achievementAim", achievementAim.getAim());
                            break;
                        }
                    }
                }

            }


            achievementsMsg.add(achievementMsg);
        }


        mapBody.put("achievementsMsg", achievementsMsg);
        mapBody.setState(ResponseMsg.MSG_ERR);
        return mapBody;
    }
}