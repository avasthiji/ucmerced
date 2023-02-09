package edu.ucmerced.chealth.datasource.health.repository;

import edu.ucmerced.chealth.datasource.health.domain.DiseaseCategory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiseaseCategoryRepository extends CrudRepository<DiseaseCategory, Long> {
}
