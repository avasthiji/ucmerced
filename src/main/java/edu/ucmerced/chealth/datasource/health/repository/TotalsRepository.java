package edu.ucmerced.chealth.datasource.health.repository;

import edu.ucmerced.chealth.datasource.health.domain.Totals;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TotalsRepository extends CrudRepository<Totals, Long>,
        QuerydslPredicateExecutor<Totals> {
}
