package com.worknear.api.banner;

import com.worknear.api.banner.domain.Banner;
import com.worknear.api.banner.dto.AdminBannerRequest;
import com.worknear.api.banner.dto.BannerResponse;
import com.worknear.api.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;

    /** Public: active banners for the given audience (audience ALL always included). */
    @Transactional(readOnly = true)
    public List<BannerResponse> activeBanners(String audience) {
        String want = normalizeAudience(audience);
        return bannerRepository.findByActiveTrueOrderBySortOrderAscCreatedAtAsc().stream()
                .filter(b -> "ALL".equals(want)
                        || "ALL".equalsIgnoreCase(b.getAudience())
                        || want.equalsIgnoreCase(b.getAudience()))
                .map(BannerResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BannerResponse> listAll() {
        return bannerRepository.findAllByOrderBySortOrderAscCreatedAtAsc()
                .stream().map(BannerResponse::from).toList();
    }

    @Transactional
    public BannerResponse create(AdminBannerRequest request) {
        Banner banner = new Banner();
        apply(banner, request);
        return BannerResponse.from(bannerRepository.save(banner));
    }

    @Transactional
    public BannerResponse update(UUID id, AdminBannerRequest request) {
        Banner banner = require(id);
        apply(banner, request);
        return BannerResponse.from(banner);
    }

    @Transactional
    public void delete(UUID id) {
        Banner banner = require(id);
        bannerRepository.delete(banner);
    }

    private void apply(Banner banner, AdminBannerRequest request) {
        banner.setTitle(request.title());
        banner.setSubtitle(request.subtitle());
        banner.setImageUrl(request.imageUrl());
        banner.setCtaLabel(request.ctaLabel());
        if (StringUtils.hasText(request.audience())) {
            banner.setAudience(normalizeAudience(request.audience()));
        }
        if (request.sortOrder() != null) {
            banner.setSortOrder(request.sortOrder());
        }
        if (request.active() != null) {
            banner.setActive(request.active());
        }
    }

    private String normalizeAudience(String audience) {
        if (!StringUtils.hasText(audience)) {
            return "ALL";
        }
        String upper = audience.trim().toUpperCase();
        return switch (upper) {
            case "CUSTOMER", "PROFESSIONAL", "ALL" -> upper;
            default -> "ALL";
        };
    }

    private Banner require(UUID id) {
        return bannerRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Banner", id));
    }
}
