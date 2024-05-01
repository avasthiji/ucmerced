package edu.ucmerced.chealth.datasource.health.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import edu.ucmerced.chealth.datasource.health.domain.Ethnicity;

@Repository
public interface EthnicityRepository extends CrudRepository<Ethnicity, Long> {
	
	@Query(nativeQuery = true,
			value = "select ethnicity from ethnicity where id In (:ids)")
	List<String> findByIdIn(List<Long> ids);
}
