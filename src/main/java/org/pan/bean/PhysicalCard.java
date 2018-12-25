package org.pan.bean;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "PhysicalCard")
public class PhysicalCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //用户
    @ManyToOne
    @JoinColumn(name = "cardUser")
    private CardUser cardUser;

    //卡片物理号
    private String physicalId;

    //卡片表面编号
    private String serialNumber;

}
