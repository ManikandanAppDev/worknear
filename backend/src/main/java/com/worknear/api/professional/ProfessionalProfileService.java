package com.worknear.api.professional;

import com.worknear.api.catalog.ServiceCategoryRepository;
import com.worknear.api.catalog.domain.ServiceCategory;
import com.worknear.api.common.exception.BadRequestException;
import com.worknear.api.common.exception.NotFoundException;
import com.worknear.api.professional.domain.BankMethod;
import com.worknear.api.professional.domain.DocumentType;
import com.worknear.api.professional.domain.ProfessionalAvailability;
import com.worknear.api.professional.domain.ProfessionalBankAccount;
import com.worknear.api.professional.domain.ProfessionalDocument;
import com.worknear.api.professional.domain.ProfessionalProfile;
import com.worknear.api.professional.domain.ProfessionalSpecialization;
import com.worknear.api.professional.domain.VerificationStatus;
import com.worknear.api.professional.dto.AvailabilityResponse;
import com.worknear.api.professional.dto.BankAccountRequest;
import com.worknear.api.professional.dto.BankAccountResponse;
import com.worknear.api.professional.dto.DocumentResponse;
import com.worknear.api.professional.dto.ProProfileResponse;
import com.worknear.api.professional.dto.ProfessionalServiceResponse;
import com.worknear.api.professional.dto.SetAvailabilityRequest;
import com.worknear.api.professional.dto.SetServicesRequest;
import com.worknear.api.professional.dto.SetSpecializationsRequest;
import com.worknear.api.professional.dto.UpdateProfileRequest;
import com.worknear.api.storage.StorageService;
import com.worknear.api.user.UserRepository;
import com.worknear.api.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfessionalProfileService {

    private final ProfessionalProfileRepository profileRepository;
    private final ProfessionalServiceRepository serviceRepository;
    private final ProfessionalSpecializationRepository specializationRepository;
    private final ProfessionalAvailabilityRepository availabilityRepository;
    private final ProfessionalDocumentRepository documentRepository;
    private final ProfessionalBankAccountRepository bankAccountRepository;
    private final ServiceCategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    @Transactional(readOnly = true)
    public ProProfileResponse getMyProfile(UUID userId) {
        return buildResponse(requireProfile(userId));
    }

    @Transactional
    public ProProfileResponse updateProfile(UUID userId, UpdateProfileRequest req) {
        ProfessionalProfile p = requireProfile(userId);
        if (req.bio() != null) p.setBio(req.bio());
        if (req.experienceYears() != null) p.setExperienceYears(req.experienceYears());
        if (req.serviceRadiusKm() != null) p.setServiceRadiusKm(req.serviceRadiusKm());
        if (req.city() != null) p.setCity(req.city());
        if (req.area() != null) p.setArea(req.area());
        if (req.baseLatitude() != null) p.setBaseLatitude(req.baseLatitude());
        if (req.baseLongitude() != null) p.setBaseLongitude(req.baseLongitude());
        if (req.languages() != null) p.setLanguages(req.languages());
        return buildResponse(p);
    }

    @Transactional
    public ProProfileResponse setServices(UUID userId, SetServicesRequest req) {
        ProfessionalProfile p = requireProfile(userId);
        for (var item : req.services()) {
            if (!categoryRepository.existsById(item.categoryId())) {
                throw NotFoundException.of("Category", item.categoryId());
            }
        }
        serviceRepository.deleteByProfessionalId(p.getId());
        for (var item : req.services()) {
            com.worknear.api.professional.domain.ProfessionalService s = new com.worknear.api.professional.domain.ProfessionalService();
            s.setProfessionalId(p.getId());
            s.setCategoryId(item.categoryId());
            s.setBasePrice(item.basePrice());
            s.setActive(true);
            serviceRepository.save(s);
        }
        return buildResponse(p);
    }

    @Transactional
    public ProProfileResponse setSpecializations(UUID userId, SetSpecializationsRequest req) {
        ProfessionalProfile p = requireProfile(userId);
        specializationRepository.deleteByProfessionalId(p.getId());
        for (String label : req.labels()) {
            if (label == null || label.isBlank()) continue;
            ProfessionalSpecialization spec = new ProfessionalSpecialization();
            spec.setProfessionalId(p.getId());
            spec.setLabel(label.trim());
            specializationRepository.save(spec);
        }
        return buildResponse(p);
    }

    @Transactional
    public ProProfileResponse setAvailability(UUID userId, SetAvailabilityRequest req) {
        ProfessionalProfile p = requireProfile(userId);
        availabilityRepository.deleteByProfessionalId(p.getId());
        if (req.slots() != null) {
            for (var slot : req.slots()) {
                if (!slot.endTime().isAfter(slot.startTime())) {
                    throw new BadRequestException("Availability end time must be after start time");
                }
                ProfessionalAvailability a = new ProfessionalAvailability();
                a.setProfessionalId(p.getId());
                a.setDayOfWeek(slot.dayOfWeek());
                a.setStartTime(slot.startTime());
                a.setEndTime(slot.endTime());
                a.setAvailable(true);
                availabilityRepository.save(a);
            }
        }
        return buildResponse(p);
    }

    @Transactional
    public DocumentResponse uploadDocument(UUID userId, DocumentType type, MultipartFile file) {
        ProfessionalProfile p = requireProfile(userId);
        StorageService.StoredFile stored = storageService.store(file, "documents/" + p.getId());
        ProfessionalDocument doc = new ProfessionalDocument();
        doc.setProfessionalId(p.getId());
        doc.setType(type);
        doc.setFileUrl(stored.url());
        doc.setOriginalName(stored.originalName());
        return DocumentResponse.from(documentRepository.save(doc));
    }

    @Transactional
    public BankAccountResponse setBankAccount(UUID userId, BankAccountRequest req) {
        ProfessionalProfile p = requireProfile(userId);
        validateBank(req);
        ProfessionalBankAccount account = bankAccountRepository.findByProfessionalId(p.getId())
                .orElseGet(ProfessionalBankAccount::new);
        account.setProfessionalId(p.getId());
        account.setMethod(req.method());
        account.setUpiId(req.upiId());
        account.setAccountName(req.accountName());
        account.setAccountNumber(req.accountNumber());
        account.setIfsc(req.ifsc());
        return BankAccountResponse.from(bankAccountRepository.save(account));
    }

    @Transactional
    public ProProfileResponse setOnline(UUID userId, boolean online) {
        ProfessionalProfile p = requireProfile(userId);
        if (online && p.getVerificationStatus() != VerificationStatus.APPROVED) {
            throw new BadRequestException("NOT_VERIFIED", "You can go online only after verification is approved");
        }
        p.setOnline(online);
        return buildResponse(p);
    }

    @Transactional
    public ProProfileResponse submitForVerification(UUID userId) {
        ProfessionalProfile p = requireProfile(userId);
        if (p.getVerificationStatus() == VerificationStatus.APPROVED) {
            throw new BadRequestException("Profile is already verified");
        }
        if (serviceRepository.findByProfessionalId(p.getId()).isEmpty()) {
            throw new BadRequestException("Add at least one service before submitting");
        }
        if (documentRepository.findByProfessionalId(p.getId()).isEmpty()) {
            throw new BadRequestException("Upload required documents before submitting");
        }
        p.setVerificationStatus(VerificationStatus.PENDING);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.of("User", userId));
        user.setRoleConfirmed(true);
        return buildResponse(p);
    }

    private void validateBank(BankAccountRequest req) {
        if (req.method() == BankMethod.UPI && (req.upiId() == null || req.upiId().isBlank())) {
            throw new BadRequestException("UPI id is required for UPI payouts");
        }
        if (req.method() == BankMethod.BANK
                && (req.accountNumber() == null || req.accountNumber().isBlank()
                || req.ifsc() == null || req.ifsc().isBlank())) {
            throw new BadRequestException("Account number and IFSC are required for bank payouts");
        }
    }

    private ProfessionalProfile requireProfile(UUID userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> NotFoundException.of("Professional profile", userId));
    }

    private ProProfileResponse buildResponse(ProfessionalProfile p) {
        User user = userRepository.findById(p.getUserId())
                .orElseThrow(() -> NotFoundException.of("User", p.getUserId()));

        Map<UUID, ServiceCategory> categories = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(ServiceCategory::getId, Function.identity()));

        List<ProfessionalServiceResponse> services = serviceRepository.findByProfessionalId(p.getId()).stream()
                .map(s -> {
                    ServiceCategory c = categories.get(s.getCategoryId());
                    return new ProfessionalServiceResponse(s.getCategoryId(),
                            c != null ? c.getSlug() : null, c != null ? c.getName() : null,
                            s.getBasePrice(), s.isActive());
                })
                .toList();

        List<String> specializations = specializationRepository.findByProfessionalId(p.getId()).stream()
                .map(ProfessionalSpecialization::getLabel).toList();

        List<AvailabilityResponse> availability = availabilityRepository
                .findByProfessionalIdOrderByDayOfWeekAsc(p.getId()).stream()
                .map(a -> new AvailabilityResponse(a.getDayOfWeek(), a.getStartTime(), a.getEndTime(), a.isAvailable()))
                .toList();

        List<DocumentResponse> documents = documentRepository.findByProfessionalId(p.getId()).stream()
                .map(DocumentResponse::from).toList();

        BankAccountResponse bank = BankAccountResponse.from(
                bankAccountRepository.findByProfessionalId(p.getId()).orElse(null));

        return new ProProfileResponse(
                user.getId(), p.getId(), user.getFullName(), user.getAvatarUrl(), p.getBio(),
                p.getExperienceYears(), p.getServiceRadiusKm(), p.getCity(), p.getArea(),
                p.getBaseLatitude(), p.getBaseLongitude(), p.getLanguages(), p.isOnline(),
                p.getVerificationStatus(), p.getRating(), p.getRatingCount(), p.getJobsCompleted(),
                specializations, services, availability, documents, bank);
    }
}
