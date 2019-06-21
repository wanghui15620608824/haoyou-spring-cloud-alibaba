package com.haoyou.spring.cloud.alibaba.commons.domain;

public class SendType {


    /**
     * 系统管理
     */
    public final static int LOGIN = 0;//登录
    public final static int LOGINOUT = 1;//登出
    public final static int BEAT = 2;//心跳
    public final static int REGISTER = 3;//注册
    public final static int VERSION_CONTROLLER = 6;//版本控制


    /**
     * 匹配系统
     */
    public final static int MATCH_IN = 31;//开始匹配
    public final static int MATCH_OUT = 32;//取消匹配
    public final static int MATCH_ACCEPT = 33;//接受匹配
    public final static int MATCH_REFUSE = 34;//拒绝匹配

    /**
     * 排名系统
     */
    public final static int RANK_LIST = 41;//获取排行榜
    public final static int RANK_NUM = 42;//获取我的排名
    /**
     * 战斗系统
     */
    public final static int FIGHTING_MSG = 20;//战斗信息
    public final static int FIGHTING_AI = 25;//战斗信息
    public final static int FIGHTING_AI2 = 26;//战斗信息
    /**
     * 养成
     */
    public final static int SKILL_CONFIG = 41;//技能配置
    /**
     * 获取信息
     */
    public final static int GET_PROPS = 51;//获取仓库信息
    public final static int GET_HALL = 52;//获取仓库信息

//    *****************************************以上为接受信息类型，以下为主动发送*********************************************************

    public final static int AWARD=4;//发放奖励
    public final static int MANDATORY_OFFLINE=5;//强制下线

    public final static int MATCH_READY=35;//匹配就绪
    public final static int MATCH_SUCCESE=36;//匹配成功
    public final static int MATCH_FILD=37;//匹配失败

    public final static int FIGHTING_INIT=21;//战斗初始化
    public final static int FIGHTING_RESP=22;//战斗返回信息
    public final static int FIGHTING_REFRESHBOARD=23;//操作刷新棋盘

    public final static int FIGHTING_INITPET=24;//战斗初始化宠物

}