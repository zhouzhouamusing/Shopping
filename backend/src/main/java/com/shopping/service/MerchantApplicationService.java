package com.shopping.service;

import com.shopping.dto.MerchantApplicationRequest;
import com.shopping.dto.MerchantApplicationReviewRequest;
import com.shopping.dto.Result;
import com.shopping.entity.MerchantApplication;
import com.shopping.entity.Shop;
import com.shopping.entity.User;
import com.shopping.repository.MerchantApplicationRepository;
import com.shopping.repository.ShopRepository;
import com.shopping.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class MerchantApplicationService {

    private final MerchantApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    public Result<MerchantApplication> submitApplication(Long userId, MerchantApplicationRequest request) {
        if (applicationRepository.existsByUserIdAndStatusIn(userId, Arrays.asList("PENDING", "APPROVED"))) {
            return Result.error(400, "您已有待审核或已通过的申请");
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        if ("ADMIN".equals(user.getRole())) {
            return Result.error(400, "管理员无需申请商家入驻");
        }
        if ("MERCHANT".equals(user.getRole())) {
            return Result.error(400, "您已是商家身份");
        }

        MerchantApplication application = new MerchantApplication();
        application.setUserId(userId);
        application.setShopName(request.getShopName());
        application.setBusinessLicense(request.getBusinessLicense());
        application.setContactName(request.getContactName());
        application.setContactPhone(request.getContactPhone());
        application.setDescription(request.getDescription());
        application.setStatus("PENDING");

        applicationRepository.save(application);
        return Result.success(application);
    }

    public Result<MerchantApplication> getMyApplication(Long userId) {
        MerchantApplication application = applicationRepository
                .findTopByUserIdOrderByCreatedAtDesc(userId).orElse(null);
        return Result.success(application);
    }

    public Result<Page<MerchantApplication>> getAllApplications(int page, int size, String status) {
        Page<MerchantApplication> applications;
        if (status != null && !status.isBlank()) {
            applications = applicationRepository.findByStatusOrderByCreatedAtDesc(status, PageRequest.of(page, size));
        } else {
            applications = applicationRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
        }
        return Result.success(applications);
    }

    @Transactional
    public Result<Void> reviewApplication(Long applicationId, MerchantApplicationReviewRequest request, Long reviewerId) {
        MerchantApplication application = applicationRepository.findById(applicationId).orElse(null);
        if (application == null) {
            return Result.error(404, "申请不存在");
        }
        if (!"PENDING".equals(application.getStatus())) {
            return Result.error(400, "该申请已审核");
        }

        if (request.getApproved()) {
            application.setStatus("APPROVED");
            User user = userRepository.findById(application.getUserId()).orElse(null);
            if (user != null) {
                user.setRole("MERCHANT");
                userRepository.save(user);

                Shop shop = new Shop();
                shop.setMerchantId(user.getId());
                shop.setShopName(application.getShopName());
                shop.setDescription(application.getDescription());
                shop.setContactName(application.getContactName());
                shop.setContactPhone(application.getContactPhone());
                shop.setBusinessLicense(application.getBusinessLicense());
                shop.setStatus(1);
                shopRepository.save(shop);
            }
        } else {
            application.setStatus("REJECTED");
            application.setRejectReason(request.getRejectReason());
        }

        application.setReviewedBy(reviewerId);
        application.setReviewedAt(LocalDateTime.now());
        applicationRepository.save(application);
        return Result.success();
    }
}
