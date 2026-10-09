package com.stackclone.web;

import com.stackclone.dto.PageResponse;
import com.stackclone.repository.TagRepository;
import com.stackclone.repository.TagUsage;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.function.Function;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagRepository tags;

    public TagController(TagRepository tags) {
        this.tags = tags;
    }

    /** Tags triés par popularité, avec recherche par nom. */
    @GetMapping
    public PageResponse<TagUsage> list(@RequestParam(defaultValue = "") String q,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "36") int size) {
        var result = tags.searchWithUsage(q.trim(), PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 100)));
        return PageResponse.of(result, Function.identity());
    }
}
