package com.haoyou.spring.cloud.alibaba.login.UserCatch;

import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.entity.Currency;
import com.haoyou.spring.cloud.alibaba.commons.mapper.*;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 登录，登出时，用户信息缓存，以及删除
 */
@Component
public class UserDateSynchronization {
    private final static Logger logger = LoggerFactory.getLogger(UserDateSynchronization.class);

    @Autowired
    private PetMapper petMapper;
    @Autowired
    private PetSkillMapper petSkillMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserDataMapper userDataMapper;

    @Autowired
    private UserNumericalMapper userNumericalMapper;
    @Autowired
    private CurrencyMapper currencyMapper;
    @Autowired
    private RedisObjectUtil redisObjectUtil;
    @Autowired
    private UserUtil userUtil;


    /**
     * 每隔30分钟,将缓存同步到数据库
     */
//    @scheduled(cron = "0 */30 * * * ?")
    public void synchronization() {
        logger.info(String.format("同步到数据库 开始 ......"));
        HashMap<String, User> users = userUtil.getUserAllCatch();
        for (Map.Entry<String, User> entry : users.entrySet()) {
            User user = entry.getValue();
            User user1 = userMapper.selectByPrimaryKey(user.getId());
            //只同步修改过的user
            if (user1.getLastUpdateDate().getTime() < user.getLastUpdateDate().getTime()) {
                redisObjectUtil.refreshTime(entry.getKey());
                userUtil.saveSqlUserAndPets(user);
            }else{
                userUtil.cacheUserAndPet(user1);
                userUtil.saveUser(user1);
            }
        }
        backupUserAward();
        logger.info(String.format("同步到数据库 结束 ！！！"));
    }

    /**
     * 备份玩家奖励
     */
    private void backupUserAward(){

        redisObjectUtil.backup();

    }


}
