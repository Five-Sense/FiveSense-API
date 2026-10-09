package com.fivesense.api.auth.controller;
import com.fivesense.api.shared.error.ApiException;
import com.fivesense.api.users.domain.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.Locale;
import java.util.Set;
/**
 * Controle de acesso simples por perfil. O cliente envia o perfil recebido no login no header
 * {@code X-User-Role}. Criar, editar e excluir usuarios, problemas, materiais e equipes exige ADMIN ou MANAGER;
 * as demais operacoes (consultas, ocorrencias, status de equipe e ajuste de estoque) aceitam qualquer perfil.
 */
public class RoleAccessInterceptor implements HandlerInterceptor {
    public static final String ROLE_HEADER = "X-User-Role";
    private static final Set<String> MANAGEMENT_METHODS = Set.of("POST", "PUT", "DELETE");
    private static final Set<String> MANAGEMENT_RESOURCES = Set.of("users", "problems", "materials", "teams");
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        UserRole role = parseRole(request.getHeader(ROLE_HEADER));
        if (requiresManagement(request) && role == UserRole.VIEWER) throw ApiException.forbidden();
        return true;
    }
    private static UserRole parseRole(String header) {
        if (header == null || header.isBlank()) throw ApiException.unauthorized("Header " + ROLE_HEADER + " is required");
        try { return UserRole.valueOf(header.trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException ex) { throw ApiException.unauthorized("Invalid " + ROLE_HEADER); }
    }
    private static boolean requiresManagement(HttpServletRequest request) {
        if (!MANAGEMENT_METHODS.contains(request.getMethod().toUpperCase(Locale.ROOT))) return false;
        String[] parts = request.getRequestURI().split("/");
        // "", "api", "v1", "<recurso>", ...
        return parts.length > 3 && MANAGEMENT_RESOURCES.contains(parts[3]);
    }
}
