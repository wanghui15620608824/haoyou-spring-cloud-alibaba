package com.haoyou.spring.cloud.alibaba.cultivate.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.entity.*;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.cultivate.service.*;
import com.haoyou.spring.cloud.alibaba.pojo.cultivate.*;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.util.UserUtil;
import org.apache.dubbo.config.annotation.Service;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.mapper.PetMapper;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.service.cultivate.CultivateService;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.*;
import java.util.concurrent.atomic.LongAdder;

import static com.haoyou.spring.cloud.alibaba.pojo.cultivate.SkillConfigMsg.*;

/**
 * @Author: wanghui
 * @Date: 2019/5/13 11:37
 * @Version 1.0
 */
@Service(version = "${cultivate.service.version}")
@RefreshScope
public class CultivateServiceImpl implements CultivateService {
    private static final Logger logger = LoggerFactory.getLogger(CultivateServiceImpl.class);

    @Autowired
    private RedisObjectUtil redisObjectUtil;
    @Autowired
    private SendMsgUtil sendMsgUtil;
    @Autowired
    private PetMapper petMapper;
    @Autowired
    private SkillConfigService skillConfigService;
    @Autowired
    private RewardService rewardService;
    @Autowired
    private PropUseService propUseService;
    @Autowired
    private CurrencyUseService currencyUseService;
    @Autowired
    private NumericalService numericalService;
    @Autowired
    private SettlementService settlementService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private RMBUseService rmbUseService;
    @Autowired
    private FriendsService friendsService;
    @Autowired
    private PlantingSystemService plantingSystemService;


    @Autowired
    private UserUtil userUtil;

    /**
     * 技能配置处理
     *
     * @param req
     * @return
     */
    @Override
    public boolean skillConfig(MyRequest req) {

        User user = req.getUser();
        SkillConfigMsg skillConfigMsg = sendMsgUtil.deserialize(req.getMsg(), SkillConfigMsg.class);
        String propInstenceUid = skillConfigMsg.getPropInstenceUid();
        switch (skillConfigMsg.getType()) {
            case ADD_PET_SKILL:
                //获取道具
                Prop prop = getProp(user, propInstenceUid);
                if (prop != null) {
                    if (skillConfigService.addPetSkill(user, skillConfigMsg, prop)) {
                        return this.saveUser(user);
                    }
                }
                break;
            case REMOVE_PET_SKILL:
                if (skillConfigService.removePetSkill(user, skillConfigMsg)) {
                    return this.saveUser(user);
                }
                break;
            case SET_PET_SKILL:
                if (skillConfigService.setPetTypeSkill(user, skillConfigMsg)) {
                    return true;
                }
                break;
        }
        return false;
    }

    /**
     * 使用道具
     *
     * @param req
     * @return
     */
    public BaseMessage propUse(MyRequest req) {
        MapBody rt = new MapBody();

        User user = req.getUser();
        PropUseMsg propUseMsg = sendMsgUtil.deserialize(req.getMsg(), PropUseMsg.class);
        String propInstenceUid = propUseMsg.getPropInstenceUid();
        Prop prop = getProp(user, propInstenceUid);

        if (prop != null) {
            if (propUseMsg.getPropCount() <= prop.getCount() && propUseMsg.getPropCount()>0) {
                propUseMsg.setProp(prop);
                propUseMsg.setUser(user);
                rt = propUseService.propUse(propUseMsg);
                if (rt.getState() == ResponseMsg.MSG_SUCCESS) {
                    if (this.saveUser(user)) {
                        return rt;
                    }
                } else {
                    rt.put("errMsg", "道具无法使用！");
                }
            } else {
                rt.setState(ResponseMsg.MSG_ERR);
                rt.put("errMsg", "道具数量不足，或为0！");
            }
        } else {
            rt.setState(ResponseMsg.MSG_ERR);
            rt.put("errMsg", "道具未找到！");
        }

        return rt;
    }


    /**
     * 注册时生成三个宠物
     *
     * @param req
     * @return
     */
    @Override
    public boolean petGeneration(MyRequest req) {
        logger.debug("注册赠送宠物！！！");
        User user = req.getUser();

        HashMap<String, PetType> stringPetTypeHashMap = redisObjectUtil.getlkMap(RedisKeyUtil.getlkKey(RedisKey.PET_TYPE), PetType.class);

        for (int i = 0; i < 3; i++) {

            int ind = RandomUtil.randomInt(6);


            petMapper.insertSelective(new Pet(user,stringPetTypeHashMap.values().toArray(new PetType[0])[ind], i+1));
        }


        return true;
    }


    /**
     * 宠物升级
     */
    @Override
    public BaseMessage petUpLev(MyRequest req) {
        MapBody rt = new MapBody();
        User user = req.getUser();
        if (user == null) {
            rt.setState(ResponseMsg.MSG_ERR);
        }
        //解析msg
        PetUpLevMsg petUpLevMsg = sendMsgUtil.deserialize(req.getMsg(), PetUpLevMsg.class);
        //获取要升级的宠物
        FightingPet fightingPet = FightingPet.getByUserAndPetUid(user, petUpLevMsg.getPetUid(), redisObjectUtil);

        //当前等级
        Integer level = fightingPet.getPet().getLevel();

        //获取升级所需经验
        String levelUpExpKey = RedisKeyUtil.getKey(RedisKey.LEVEL_UP_EXP, level.toString());
        LevelUpExp levelUpExp = redisObjectUtil.get(levelUpExpKey, LevelUpExp.class);
        //玩家拥有的经验
        Long petExp = user.getCurrency().getPetExp();

        if (levelUpExp.getUpLevExp() > petExp) {
            //经验不足不能升级 ，1
            rt.setState(ResponseMsg.MSG_ERR);
            rt.put("errMsg", 1);
            return rt;
        }


        //忠诚等级
        Integer loyaltyLev = fightingPet.getPet().getLoyaltyLev();
        //获取忠诚等级关系
        String levLoyaltyKey = RedisKeyUtil.getKey(RedisKey.LEV_LOYALTY, loyaltyLev.toString());
        LevLoyalty levLoyalty = redisObjectUtil.get(levLoyaltyKey, LevLoyalty.class);

        if (level >= levLoyalty.getLevelMax()) {
            //忠诚等级不足 ，2
            rt.setState(ResponseMsg.MSG_ERR);
            rt.put("errMsg", 2);
            return rt;
        }


        //数值系统宠物最高级记录
        UserNumerical userNumerical = user.getUserNumericalMap().get("max_pet_lev");

        if (level + 1 > userNumerical.getValue()) {
            this.numericalAdd(user, "max_pet_lev", level + 1 - userNumerical.getValue());
        }

        //升级
        fightingPet.upLevel();
        //减掉经验,保存
        user.getCurrency().setPetExp(petExp - levelUpExp.getUpLevExp());

        this.saveUser(user);

        //修改升级所需经验
        LevelUpExp nextLevelUpExp = redisObjectUtil.get(RedisKeyUtil.getKey(RedisKey.LEVEL_UP_EXP, Integer.toString(level + 1)), LevelUpExp.class);
        fightingPet.getPet().setLevUpExp(nextLevelUpExp.getUpLevExp());
        fightingPet.save();
        this.saveUser(user);
        rt.setState(ResponseMsg.MSG_SUCCESS);
        //数值系统每日升级次数
        this.numericalAdd(user, "daily_pet_up_lev", 1L);

        return rt;
    }


    /**
     * 奖励分发，根据type获取不同奖励模式
     *
     * @param user
     * @param type
     * @return
     */
    @Override
    public boolean rewards(User user, String type) {
        if (rewardService.rewards(user, type)) {
            return this.saveUser(user);
        }
        return false;
    }

    /**
     * 领取奖励
     *
     * @param req
     * @return
     */
    @Override
    public BaseMessage receiveAward(MyRequest req) {
        User user = req.getUser();
        String type = null;
        try {
            Map<String, Object> pro = MapperUtils.json2map(new String(req.getMsg()));
            type = (String) pro.get("type");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (StrUtil.isEmpty(type)){
            MapBody mapBody = new MapBody();
            mapBody.setState(ResponseMsg.MSG_ERR);
            mapBody.put("errMsg", "没有奖励类型！");
            return mapBody;
        }

        MapBody mapBody = rewardService.receiveAward(user, type);

        if (mapBody.getState().equals(ResponseMsg.MSG_SUCCESS)) {
            if (!this.saveUser(user)){
                mapBody.setState(ResponseMsg.MSG_ERR);
                mapBody.put("errMsg", "奖励保存未成功！");
            }
        }
        return mapBody;
    }


    /**
     * 数值系统
     *
     * @param user
     * @param numericalName
     * @param value
     * @return
     */
    @Override
    public boolean numericalAdd(User user, String numericalName, long value) {


        if (numericalService.numericalAdd(user, numericalName, value)) {
            if (this.saveUser(user)) {
                return true;
            }
        }

        return false;
    }
    @Override
    public boolean numericalSet (User user,String numericalName,long value){
        if (numericalService.numericalSet(user, numericalName, value)) {
            if (this.saveUser(user)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 计时执行
     */
    @Override
    public void doSettlement() {
        settlementService.inspect();
    }


    /**
     * 修改出战
     *
     * @param req
     * @return
     */
    @Override
    public boolean updateIsWork(MyRequest req) {

        User user = req.getUser();
        UpdateIsworkMsg updateIsworkMsg = sendMsgUtil.deserialize(req.getMsg(), UpdateIsworkMsg.class);
        String friendUid = updateIsworkMsg.getFriendUid();

        if(StrUtil.isNotEmpty(friendUid)){

            String hashKey = RedisKeyUtil.getKey(RedisKey.HELP_PET, user.getUid(), RedisKey.HAS_HELP,friendUid);
            String s = redisObjectUtil.get(hashKey, String.class);
            if(StrUtil.isNotEmpty(s)){
                return false;
            }

            //删除旧的助战设置
            String lkKey = RedisKeyUtil.getlkKey(RedisKey.HELP_PET, user.getUid(), RedisKey.HELP);
            redisObjectUtil.deleteAll(lkKey);

            if(StrUtil.isNotEmpty(updateIsworkMsg.getPetUid())){
                String key = RedisKeyUtil.getKey(RedisKey.HELP_PET, user.getUid(),RedisKey.HELP, friendUid);
                //助战玩家以及助战位置存入设置
                redisObjectUtil.save(key,RedisKeyUtil.getKey(friendUid,Integer.toString(updateIsworkMsg.getIswork())));
            }

        }else{
            //修改出战
            FightingPet fightingPet = FightingPet.getByUserAndPetUid(user, updateIsworkMsg.getPetUid(), redisObjectUtil);

            //交换位置
            Integer isworkbf = fightingPet.getPet().getIswork();
            String userUidKey = RedisKeyUtil.getKey(RedisKey.FIGHT_PETS, user.getUid());
            String key = RedisKeyUtil.getlkKey(userUidKey);
            HashMap<String, FightingPet> fightingPets = redisObjectUtil.getlkMap(key, FightingPet.class);
            for (FightingPet fightingPetOne : fightingPets.values()) {
                Pet pet = fightingPetOne.getPet();
                if (updateIsworkMsg.getIswork() != 0 && pet.getIswork() == updateIsworkMsg.getIswork()) {
                    pet.setIswork(isworkbf);
                    fightingPetOne.setRedisObjectUtil(redisObjectUtil);
                    fightingPetOne.save();
                }
            }

            fightingPet.getPet().setIswork(updateIsworkMsg.getIswork());

            fightingPet.save();
        }

        return true;
    }


    /**
     * 使用货币
     *
     * @param req
     * @return
     */
    @Override
    public BaseMessage currencyUse(MyRequest req) {

        MapBody mapBody = new MapBody();
        User user = req.getUser();

        CyrrencyUseMsg deserialize = sendMsgUtil.deserialize(req.getMsg(), CyrrencyUseMsg.class);
        deserialize.setUser(user);

        mapBody.put("type",deserialize.getType());

        int bkMsg = currencyUseService.currencyUse(deserialize);


        if (bkMsg == ResponseMsg.MSG_SUCCESS) {
            if (this.saveUser(user)) {
                mapBody.setState(bkMsg);
            } else {
                mapBody.setState(ResponseMsg.MSG_ERR);
            }
        } else {
            mapBody.setState(bkMsg);
        }


        return mapBody;
    }


    /**
     * 邮件操作
     *
     * @param req
     * @return
     */
    @Override
    public BaseMessage emailDo(MyRequest req) {
        MapBody mapBody = new MapBody();
        User user = req.getUser();

        EmailDoMsg emailDoMsg = sendMsgUtil.deserialize(req.getMsg(), EmailDoMsg.class);

        String emailUid = emailDoMsg.getEmailUid();

        if(StrUtil.isEmpty(emailUid)){
            emailService.emailAll(user,emailDoMsg.getType());
            this.saveUser(user);
            mapBody.setState(ResponseMsg.MSG_SUCCESS);
        }else{
            mapBody = emailService.emailOne(user, emailDoMsg.getType(), emailUid);
            if(ResponseMsg.MSG_SUCCESS == mapBody.getState()){
                this.saveUser(user);
            }
        }
        mapBody.put("type",emailDoMsg.getType());
        return mapBody;

    }

    /**
     * 人民币消费
     *
     * @param req
     * @return
     */
    @Override
    public BaseMessage rmbUse(MyRequest req) {

        User user = req.getUser();

        RMBUseMsg rmbUseMsg = sendMsgUtil.deserialize(req.getMsg(), RMBUseMsg.class);

        rmbUseMsg.setUser(user);

        return rmbUseService.rmbUse(rmbUseMsg);
    }

    /**
     * 好友系统
     * @param req
     * @return
     */
    @Override
    public BaseMessage friendsDo(MyRequest req) {
        return friendsService.friendsDo(req);
    }
    /**
     * 增加好友亲密度
     * @param user
     * @param userUid
     */
    @Override
    public void addIntimacy(User user, String userUid,long value) {
        friendsService.addIntimacy(user,userUid,value);
    }

    /**
     * 种植系统操作
     * @param req
     * @return
     */
    @Override
    public BaseMessage plantingDo(MyRequest req) {
        return plantingSystemService.handle(req);
    }





    /**
     * 获取道具
     *
     * @param user
     * @param propInstenceUid
     * @return
     */
    private Prop getProp(User user, String propInstenceUid) {

        List<Prop> props = user.propList();

        if (props != null && props.size() > 0) {
            for (Prop propTrue : props) {
                if (propTrue.getPropInstenceUid().equals(propInstenceUid)) {
                    return propTrue;
                }
            }
        }
        return null;
    }

    /**
     * 保存user
     *
     * @param user
     * @return
     */
    private boolean saveUser(User user) {
        user.setLastUpdateDate(new Date());
        userUtil.saveUser(user,RedisKey.USER);
        return true;
    }
}
