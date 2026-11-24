package com.portfolio.HelpDesk_API.Security;

import com.portfolio.HelpDesk_API.entities.Role;
import com.portfolio.HelpDesk_API.entities.Ticket;
import com.portfolio.HelpDesk_API.entities.User;
import com.portfolio.HelpDesk_API.Exceptions.BusinessRuleException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new BusinessRuleException("Usuário não autenticado.");
        }
        return user;
    }

    public static boolean isAdmin() {
        return getCurrentUser().getRole() == Role.ADMIN;
    }

    public static boolean isStaff() {
        Role role = getCurrentUser().getRole();
        return role == Role.ADMIN || role == Role.TECHNICIAN;
    }

    public static boolean canAccessTicket(Ticket ticket) {
        return isStaff() || ticket.getUser().getId().equals(getCurrentUser().getId());
    }

    public static void requireTicketAccess(Ticket ticket) {
        if (!canAccessTicket(ticket)) {
            throw new BusinessRuleException("Você não tem permissão para acessar este chamado.");
        }
    }
}
