package edu.ucmerced.chealth.datasource.health.repository;

import edu.ucmerced.chealth.datasource.health.domain.County;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountyRepository extends CrudRepository<County, Long> {
}
