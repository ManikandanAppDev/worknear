package com.worknear.api.user;

import com.worknear.api.common.exception.BadRequestException;
import com.worknear.api.common.exception.NotFoundException;
import com.worknear.api.user.domain.CustomerAddress;
import com.worknear.api.user.domain.User;
import com.worknear.api.user.dto.AddressRequest;
import com.worknear.api.user.dto.AddressResponse;
import com.worknear.api.user.dto.UpdateProfileRequest;
import com.worknear.api.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    /** Maximum number of saved addresses a customer may keep. */
    private static final int MAX_ADDRESSES = 3;

    private final UserRepository userRepository;
    private final CustomerAddressRepository addressRepository;

    @Transactional(readOnly = true)
    public UserResponse getMe(UUID userId) {
        return UserResponse.from(getUser(userId));
    }

    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = getUser(userId);
        if (StringUtils.hasText(request.fullName())) {
            user.setFullName(request.fullName());
        }
        if (request.email() != null) {
            user.setEmail(request.email());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> listAddresses(UUID userId) {
        return addressRepository.findByUserIdOrderByDefaultAddressDescCreatedAtDesc(userId)
                .stream().map(AddressResponse::from).toList();
    }

    @Transactional
    public AddressResponse addAddress(UUID userId, AddressRequest request) {
        List<CustomerAddress> existing = addressRepository.findByUserIdOrderByDefaultAddressDescCreatedAtDesc(userId);
        if (existing.size() >= MAX_ADDRESSES) {
            throw new BadRequestException("ADDRESS_LIMIT_REACHED",
                    "You can save up to " + MAX_ADDRESSES + " addresses. Delete one to add a new address.");
        }
        CustomerAddress address = new CustomerAddress();
        address.setUserId(userId);
        apply(address, request);
        boolean firstAddress = existing.isEmpty();
        if (request.makeDefault() || firstAddress) {
            addressRepository.clearDefaultForUser(userId);
            address.setDefaultAddress(true);
        }
        return AddressResponse.from(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse updateAddress(UUID userId, UUID addressId, AddressRequest request) {
        CustomerAddress address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> NotFoundException.of("Address", addressId));
        apply(address, request);
        if (request.makeDefault()) {
            addressRepository.clearDefaultForUser(userId);
            address.setDefaultAddress(true);
        }
        return AddressResponse.from(address);
    }

    @Transactional
    public void deleteAddress(UUID userId, UUID addressId) {
        CustomerAddress address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> NotFoundException.of("Address", addressId));
        addressRepository.delete(address);
    }

    private void apply(CustomerAddress address, AddressRequest request) {
        address.setLabel(request.label());
        address.setLine1(request.line1());
        address.setLine2(request.line2());
        address.setCity(request.city());
        address.setState(request.state());
        address.setPincode(request.pincode());
        address.setLatitude(request.latitude());
        address.setLongitude(request.longitude());
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.of("User", userId));
    }
}
