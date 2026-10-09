package com.stackclone.web;

import com.stackclone.dto.PostDtos.CommentDto;
import com.stackclone.dto.PostDtos.CommentRequest;
import com.stackclone.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /** postId peut être l'id d'une question ou d'une réponse. */
    @PostMapping("/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto add(@PathVariable Long postId, @Valid @RequestBody CommentRequest req) {
        return commentService.add(postId, req);
    }

    @DeleteMapping("/comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        commentService.delete(id);
    }
}
