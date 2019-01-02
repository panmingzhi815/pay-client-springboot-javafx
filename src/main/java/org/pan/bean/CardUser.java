package org.pan.bean;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "CardUser")
public class CardUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //用户编号
    private String identifier;

    //用户姓名
    private String name;

    //用户组
    private String groupCodeNameJoinStr;

    //用户钱包
    @OneToMany(mappedBy = "cardUser", fetch = FetchType.EAGER)
    private List<ConsumptionWallet> walletList = new ArrayList<>();

}
