package com.worknear.api.catalog;

import com.worknear.api.catalog.domain.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, UUID> {
    List<ServiceCategory> findByActiveTrueOrderBySortOrderAsc();

    List<ServiceCategory> findAllByOrderBySortOrderAscNameAsc();

    Optional<ServiceCategory> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
