package edu.ucmerced.chealth.datasource.health.repository;

import edu.ucmerced.chealth.datasource.health.domain.Ethnicity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EthnicityRepository extends CrudRepository<Ethnicity, Long> {
}
