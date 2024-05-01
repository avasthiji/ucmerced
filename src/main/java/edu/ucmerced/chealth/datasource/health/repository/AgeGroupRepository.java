package edu.ucmerced.chealth.datasource.health.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import edu.ucmerced.chealth.datasource.health.domain.AgeGroup;

@Repository
public interface AgeGroupRepository extends CrudRepository<AgeGroup, Long> {
	
	List<AgeGroup> findByIdIn(List<Integer> ids);

}
