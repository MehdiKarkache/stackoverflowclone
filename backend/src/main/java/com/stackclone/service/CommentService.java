package com.stackclone.service;

import com.stackclone.dto.PostDtos.CommentDto;
import com.stackclone.dto.PostDtos.CommentRequest;
import com.stackclone.model.Comment;
import com.stackclone.model.Post;
import com.stackclone.repository.CommentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommentService {

    private final CommentRepository comments;
    private final Lookups lookups;
    private final PostMapper mapper;

    public CommentService(CommentRepository comments, Lookups lookups, PostMapper mapper) {
        this.comments = comments;
        this.lookups = lookups;
        this.mapper = mapper;
    }

    @Transactional
    public CommentDto add(Long postId, CommentRequest req) {
        Post post = lookups.post(postId);
        Comment comment = comments.save(new Comment(lookups.currentUser(), post, req.body().trim()));
        post.getComments().add(comment);
        return mapper.toDto(comment);
    }

    @Transactional
    public void delete(Long id) {
        Comment comment = comments.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commentaire introuvable"));
        Permissions.requireOwnerOrModerator(comment.getAuthor());
        comment.getPost().getComments().remove(comment);
    }
}
