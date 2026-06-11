package com.shopping.controller;

import com.shopping.dto.Result;
import com.shopping.entity.MemberLevel;
import com.shopping.service.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/membership")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    @GetMapping("/info")
    public Result<MembershipService.MembershipInfoResponse> getMembershipInfo(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return membershipService.getMembershipInfo(userId);
    }

    @GetMapping("/levels")
    public Result<List<MemberLevel>> getAllLevels() {
        return membershipService.getAllLevels();
    }
}
