package org.pan.repository;

import org.pan.bean.PhysicalCard;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author panmingzhi
 */
@Repository
public interface PhysicalCardRepository extends CrudRepository<PhysicalCard, Long> {

    public PhysicalCard findPhysicalCardByPhysicalId(String card);
}
