package edu.ucmerced.chealth.datasource.health.repository;

import edu.ucmerced.chealth.datasource.health.domain.Region;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionRepository extends CrudRepository<Region, Long> {
}
