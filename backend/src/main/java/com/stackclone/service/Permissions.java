package com.stackclone.service;

import com.stackclone.model.User;
import com.stackclone.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

final class Permissions {

    private Permissions() {
    }

    static boolean isModerator() {
        return CurrentUser.hasRole("MODERATOR") || CurrentUser.hasRole("ADMIN");
    }

    /** L'auteur du contenu ou un modérateur peut le modifier / supprimer. */
    static void requireOwnerOrModerator(User author) {
        if (!author.getId().equals(CurrentUser.requireId()) && !isModerator()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Action non autorisée");
        }
    }
}
