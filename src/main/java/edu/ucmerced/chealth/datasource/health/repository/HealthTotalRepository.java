package edu.ucmerced.chealth.datasource.health.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import edu.ucmerced.chealth.datasource.health.domain.HealthTotalData;
import edu.ucmerced.chealth.datasource.health.domain.Totals;

@Repository
public interface HealthTotalRepository extends CrudRepository<HealthTotalData, Long>,
QuerydslPredicateExecutor<Totals> {

	@Query(nativeQuery = true,
			value = "select * from health_data where  county In (:county) AND age between :ageStart AND :ageEnd AND ethnicity In (:ethnicity) "
					+ " AND disease IN (:diseases) AND sex in (:sex) AND region In (:region)")
	List<HealthTotalData> retrieveHealthData(int ageStart, int ageEnd, List<String> county, List<String> ethnicity, List<String> diseases, List<String> sex, List<String>  	region); 
}