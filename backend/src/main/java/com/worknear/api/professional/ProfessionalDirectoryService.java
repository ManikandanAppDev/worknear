package com.worknear.api.professional;

import com.worknear.api.catalog.ServiceCategoryRepository;
import com.worknear.api.catalog.domain.ServiceCategory;
import com.worknear.api.common.exception.NotFoundException;
import com.worknear.api.common.util.GeoUtils;
import com.worknear.api.professional.domain.ProfessionalProfile;
import com.worknear.api.professional.domain.ProfessionalService;
import com.worknear.api.professional.domain.VerificationStatus;
import com.worknear.api.professional.dto.AvailabilityResponse;
import com.worknear.api.professional.dto.ProfessionalDetailResponse;
import com.worknear.api.professional.dto.ProfessionalServiceResponse;
import com.worknear.api.professional.dto.ProfessionalSort;
import com.worknear.api.professional.dto.ProfessionalSummaryResponse;
import com.worknear.api.user.UserRepository;
import com.worknear.api.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Read-side service powering the customer-facing professional listing and profile.
 * Distance is computed with the Haversine formula; for production scale this should
 * move to a PostGIS spatial query.
 */
@Service
@RequiredArgsConstructor
public class ProfessionalDirectoryService {

    private final ProfessionalServiceRepository serviceRepository;
    private final ProfessionalProfileRepository profileRepository;
    private final ProfessionalSpecializationRepository specializationRepository;
    private final ProfessionalAvailabilityRepository availabilityRepository;
    private final ServiceCategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ProfessionalSummaryResponse> search(UUID categoryId, ProfessionalSort sort,
                                                     Double lat, Double lng, int limit) {
        List<ProfessionalService> services = serviceRepository.findByCategoryIdAndActiveTrue(categoryId);
        if (services.isEmpty()) {
            return List.of();
        }
        Map<UUID, BigDecimal> priceByProfile = services.stream()
                .collect(Collectors.toMap(ProfessionalService::getProfessionalId, ProfessionalService::getBasePrice, (a, b) -> a));

        List<ProfessionalProfile> profiles = profileRepository.findAllById(priceByProfile.keySet()).stream()
                .filter(p -> p.getVerificationStatus() == VerificationStatus.APPROVED)
                .toList();

        Map<UUID, User> usersById = usersByIds(profiles.stream().map(ProfessionalProfile::getUserId).toList());

        List<ProfessionalSummaryResponse> result = profiles.stream()
                .map(p -> toSummary(p, usersById.get(p.getUserId()), priceByProfile.get(p.getId()), lat, lng))
                .sorted(comparator(sort))
                .limit(limit)
                .toList();
        return result;
    }

    @Transactional(readOnly = true)
    public ProfessionalDetailResponse getDetailByUserId(UUID userId) {
        ProfessionalProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> NotFoundException.of("Professional", userId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.of("User", userId));
        return toDetail(profile, user);
    }

    ProfessionalDetailResponse toDetail(ProfessionalProfile profile, User user) {
        Map<UUID, ServiceCategory> categories = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(ServiceCategory::getId, Function.identity()));

        List<ProfessionalServiceResponse> services = serviceRepository.findByProfessionalId(profile.getId()).stream()
                .map(s -> {
                    ServiceCategory c = categories.get(s.getCategoryId());
                    return new ProfessionalServiceResponse(
                            s.getCategoryId(),
                            c != null ? c.getSlug() : null,
                            c != null ? c.getName() : null,
                            s.getBasePrice(),
                            s.isActive());
                })
                .toList();

        List<String> specializations = specializationRepository.findByProfessionalId(profile.getId()).stream()
                .map(com.worknear.api.professional.domain.ProfessionalSpecialization::getLabel)
                .toList();

        List<AvailabilityResponse> availability = availabilityRepository
                .findByProfessionalIdOrderByDayOfWeekAsc(profile.getId()).stream()
                .map(a -> new AvailabilityResponse(a.getDayOfWeek(), a.getStartTime(), a.getEndTime(), a.isAvailable()))
                .toList();

        return new ProfessionalDetailResponse(
                user.getId(), profile.getId(), user.getFullName(), user.getAvatarUrl(), profile.getBio(),
                profile.getRating(), profile.getRatingCount(), profile.getExperienceYears(),
                profile.getServiceRadiusKm(), profile.getCity(), profile.getArea(), profile.getLanguages(),
                profile.isOnline(), profile.getVerificationStatus(), profile.getJobsCompleted(),
                specializations, services, availability);
    }

    private ProfessionalSummaryResponse toSummary(ProfessionalProfile p, User user, BigDecimal price,
                                                  Double lat, Double lng) {
        Double distance = GeoUtils.distanceKm(lat, lng, p.getBaseLatitude(), p.getBaseLongitude());
        return new ProfessionalSummaryResponse(
                p.getUserId(), p.getId(),
                user != null ? user.getFullName() : null,
                user != null ? user.getAvatarUrl() : null,
                p.getRating(), p.getRatingCount(), p.getExperienceYears(),
                p.getVerificationStatus() == VerificationStatus.APPROVED, p.isOnline(),
                price, distance);
    }

    private Comparator<ProfessionalSummaryResponse> comparator(ProfessionalSort sort) {
        ProfessionalSort s = sort == null ? ProfessionalSort.RECOMMENDED : sort;
        return switch (s) {
            case NEAREST -> Comparator.comparing(r -> r.distanceKm() == null ? Double.MAX_VALUE : r.distanceKm());
            case TOP_RATED -> Comparator.comparing(ProfessionalSummaryResponse::rating, Comparator.reverseOrder());
            case LOW_PRICE -> Comparator.comparing(r -> r.price() == null ? BigDecimal.valueOf(Long.MAX_VALUE) : r.price());
            // RECOMMENDED: online first, then rating desc, then distance asc
            case RECOMMENDED -> Comparator
                    .comparing(ProfessionalSummaryResponse::online, Comparator.reverseOrder())
                    .thenComparing(ProfessionalSummaryResponse::rating, Comparator.reverseOrder())
                    .thenComparing(r -> r.distanceKm() == null ? Double.MAX_VALUE : r.distanceKm());
        };
    }

    private Map<UUID, User> usersByIds(List<UUID> ids) {
        return userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }
}
