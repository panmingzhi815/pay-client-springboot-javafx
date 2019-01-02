package org.pan.bean;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "ConsumptionRecord")
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    //剩余时间
    private Double leftMoney;

    //用户组
    private String cardUserGroup;

    //充值端名称
    private String deviceName;
}
