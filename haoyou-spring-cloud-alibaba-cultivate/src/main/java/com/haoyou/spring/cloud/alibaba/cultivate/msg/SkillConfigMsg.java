package com.haoyou.spring.cloud.alibaba.cultivate.msg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import com.haoyou.spring.cloud.alibaba.fighting.info.skill.shape.Tetromino;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: wanghui
 * @Date: 2019/5/13 14:00
 * @Version 1.0
 */
@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class SkillConfigMsg implements Serializable {

    //添加宠物技能
    public static final int ADD_PET_SKILL = 1;
    //删除宠物技能
    public static final int REMOVE_PET_SKILL = 2;
    private static final long serialVersionUID = -6508254959491612118L;


    private Tetromino tetromino;
    private Prop prop;
    private String petUid;

    private Integer type;
}