package edu.ucmerced.chealth.datasource.health.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import edu.ucmerced.chealth.datasource.health.domain.Disease;

@Repository
public interface DiseaseRepository extends CrudRepository<Disease, Long> {
	
	@Query(nativeQuery = true,
			value = "select disease from disease where id In (:ids)")
	List<String> findByIdIn(List<Long> ids);
}
