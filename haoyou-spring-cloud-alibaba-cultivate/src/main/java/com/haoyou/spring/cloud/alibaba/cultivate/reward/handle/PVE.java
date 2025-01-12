package com.haoyou.spring.cloud.alibaba.cultivate.reward.handle;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.domain.RewardType;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import com.haoyou.spring.cloud.alibaba.commons.entity.Skill;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.fighting.info.skill.shape.Tetromino;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class PVE extends RewardHandle {
    @Override
    protected void setHandleType() {
        this.handleType = RewardType.PVE;
    }

    @Override
    public Award handle() {

        String skillLkKey = RedisKeyUtil.getlkKey(RedisKey.SKILL);
        HashMap<String, Skill> skills = this.redisObjectUtil.getlkMap(skillLkKey, Skill.class);
        //随机获取一个技能
        int r = RandomUtil.randomInt(skills.size());
        Skill skill = skills.values().toArray(new Skill[0])[r];


        //添加道具
        List<Prop> props = new ArrayList<>();

        //获取技能道具，并塞进去技能uid和形状
        String propKey = RedisKeyUtil.getKey(RedisKey.PROP, "PetSkill");

        Prop prop = redisObjectUtil.get(propKey, Prop.class);
        prop.setPropInstenceUid(IdUtil.simpleUUID());
        prop.setProperty1(skill.getL10n());
        prop.setProperty2(Tetromino.randomOne().getType());
        prop.setProperty3(skill.getName());
        prop.setProperty4(skill.getUid());
        prop.setProperty5(skill.getDescribe());
        props.add(prop);
        /**
         * 发送奖励信息
         */


        Award award = new Award().init(100,20,100,100,props).notToLong();

        return award;
    }
}
