package com.haoyou.spring.cloud.alibaba.fighting.info.skill.shape;

/**
 * @Author: wanghui
 * @Date: 2019/5/10 10:56
 * @Version 1.0
 */

public class L extends Tetromino {
    //T型位置
    public L() {
        type="L";
        cells = new Cell[4];
        cells[0] = new Cell(0,0);
        cells[1] = new Cell(0,1);
        cells[2] = new Cell(0,2);
        cells[3] = new Cell(1,0);
        this.leftAndDrop();
    }
}
