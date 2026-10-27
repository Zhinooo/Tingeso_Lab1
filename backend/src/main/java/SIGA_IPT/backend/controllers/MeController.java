package SIGA_IPT.backend.controllers;

import SIGA_IPT.backend.dtos.UserDTOs.MeResponseDTO;
import SIGA_IPT.backend.services.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final CurrentUserService currentUserService;

    public MeController(CurrentUserService currentUserService){
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<MeResponseDTO> getMe(@AuthenticationPrincipal Jwt jwt, Authentication authentication){
        List<String> roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
                .toList();
        return ResponseEntity.ok(currentUserService.getMe(jwt.getClaimAsString("email"), jwt.getClaimAsString("preferred_username"), roles));
    }
}
