package edu.ucmerced.chealth.datasource.health.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import edu.ucmerced.chealth.datasource.health.domain.County;

@Repository
public interface CountyRepository extends CrudRepository<County, Long> {
	
	@Query(nativeQuery = true,
			value = "select county from county where id In (:ids)")
	List<String> findByIdIn(List<Long> ids);

}
