package com.stackclone.web;

import com.stackclone.service.VoteService;
import com.stackclone.service.VoteService.VoteResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class VoteController {

    public record VoteRequest(@NotNull Integer value) {
    }

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    /** value : 1 (pour), -1 (contre), 0 (annuler). */
    @PostMapping("/{postId}/vote")
    public VoteResult vote(@PathVariable Long postId, @Valid @RequestBody VoteRequest req) {
        return voteService.vote(postId, req.value());
    }
}
