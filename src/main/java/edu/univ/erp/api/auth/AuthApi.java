package edu.univ.erp.api.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.univ.erp.access.AccessDeniedException;
import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.api.types.UserAuthDto;
import edu.univ.erp.auth.AuthService;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.UserAuth;
import edu.univ.erp.service.ServiceRegistry;

public class AuthApi {
    private static final Logger logger = LoggerFactory.getLogger(AuthApi.class);
    private final AuthService authService;

    public AuthApi() {
        this.authService = ServiceRegistry.getAuthService();
    }

    public ApiResponse<UserAuthDto> login(String username, String password) {
        try {
            UserAuth userAuth = authService.login(username, password);
            UserAuthDto dto = toDto(userAuth);
            return ApiResponse.ok(dto);
        } catch (IllegalArgumentException ex) {
            // Preserve existing error messages
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error during login", ex);
            return ApiResponse.error("Login error: " + ex.getMessage());
        }
    }

    public ApiResponse<Void> logout() {
        try {
            SessionManager.clear();
            return ApiResponse.ok((Void) null);
        } catch (Exception ex) {
            logger.error("Unexpected error during logout", ex);
            return ApiResponse.error("Logout error: " + ex.getMessage());
        }
    }

    public ApiResponse<UserAuthDto> whoAmI() {
        try {
            UserAuth currentUser = SessionManager.getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("Not authenticated");
            }
            UserAuthDto dto = toDto(currentUser);
            return ApiResponse.ok(dto);
        } catch (Exception ex) {
            logger.error("Unexpected error getting current user", ex);
            return ApiResponse.error("Error getting user info: " + ex.getMessage());
        }
    }

    public ApiResponse<Void> changePassword(String currentPassword, String newPassword) {
        try {
            UserAuth currentUser = SessionManager.getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("Not authenticated");
            }

            authService.changePassword(currentUser.getUserId(), currentPassword, newPassword);
            return ApiResponse.ok((Void) null);
        } catch (IllegalArgumentException ex) {
            // Preserve existing error messages
            return ApiResponse.error(ex.getMessage());
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error changing password", ex);
            return ApiResponse.error("Error changing password: " + ex.getMessage());
        }
    }

    private UserAuthDto toDto(UserAuth userAuth) {
        if (userAuth == null) {
            return null;
        }
        UserAuthDto dto = new UserAuthDto();
        dto.setUserId(userAuth.getUserId());
        dto.setUsername(userAuth.getUsername());
        dto.setRole(userAuth.getRole());
        dto.setStatus(userAuth.getStatus());
        return dto;
    }
}


