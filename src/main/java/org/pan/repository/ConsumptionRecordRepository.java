package org.pan.repository;

import org.pan.bean.ConsumptionRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

/**
 * @author panmingzhi
 */
public interface ConsumptionRecordRepository extends CrudRepository<ConsumptionRecord, Long> {

    Page<ConsumptionRecord> findConsumptionRecordsByCardUserIdentifier(String cardUserIdentifier, Pageable pageable);
}
