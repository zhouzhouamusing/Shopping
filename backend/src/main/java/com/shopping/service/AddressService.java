package com.shopping.service;

import com.shopping.dto.AddressRequest;
import com.shopping.dto.Result;
import com.shopping.entity.Address;
import com.shopping.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    public Result<List<Address>> getAddresses(Long userId) {
        List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
        return Result.success(addresses);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Address> addAddress(Long userId, AddressRequest request) {
        if (addressRepository.countByUserId(userId) >= 20) {
            return Result.error(400, "最多保存20个收货地址");
        }

        Address address = new Address();
        address.setUserId(userId);
        address.setReceiverName(request.getReceiverName());
        address.setPhone(request.getPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetailAddress(request.getDetailAddress());

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.clearDefaultByUserId(userId);
            address.setIsDefault(true);
        } else if (addressRepository.countByUserId(userId) == 0) {
            address.setIsDefault(true);
        } else {
            address.setIsDefault(false);
        }

        addressRepository.save(address);
        return Result.success(address);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Address> updateAddress(Long userId, Long addressId, AddressRequest request) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId).orElse(null);
        if (address == null) {
            return Result.error(404, "地址不存在");
        }

        address.setReceiverName(request.getReceiverName());
        address.setPhone(request.getPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetailAddress(request.getDetailAddress());

        if (Boolean.TRUE.equals(request.getIsDefault()) && !address.getIsDefault()) {
            addressRepository.clearDefaultByUserId(userId);
            address.setIsDefault(true);
        }

        addressRepository.save(address);
        return Result.success(address);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId).orElse(null);
        if (address == null) {
            return Result.error(404, "地址不存在");
        }

        boolean wasDefault = address.getIsDefault();
        addressRepository.delete(address);

        if (wasDefault) {
            List<Address> remaining = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
            if (!remaining.isEmpty()) {
                Address first = remaining.get(0);
                first.setIsDefault(true);
                addressRepository.save(first);
            }
        }

        return Result.success();
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> setDefault(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId).orElse(null);
        if (address == null) {
            return Result.error(404, "地址不存在");
        }

        addressRepository.clearDefaultByUserId(userId);
        address.setIsDefault(true);
        addressRepository.save(address);
        return Result.success();
    }
}
