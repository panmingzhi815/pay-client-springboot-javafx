package org.pan.bean;


import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "ConsumptionRecord")
public class ConsumptionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //用户编号
    private String cardUserIdentifier;

    //记录类型
    private String consumptionRecordEnum;

    //操作金额
    private Double operatorMoney;

    //操作时间
    private Date databaseTime;

}
