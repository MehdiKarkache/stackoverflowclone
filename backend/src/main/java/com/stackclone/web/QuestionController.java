package com.stackclone.web;

import com.stackclone.dto.PageResponse;
import com.stackclone.dto.PostDtos.*;
import com.stackclone.service.AnswerService;
import com.stackclone.service.QuestionService;
import com.stackclone.service.QuestionService.SortOrder;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final AnswerService answerService;

    public QuestionController(QuestionService questionService, AnswerService answerService) {
        this.questionService = questionService;
        this.answerService = answerService;
    }

    @GetMapping
    public PageResponse<QuestionSummary> list(@RequestParam(defaultValue = "newest") String sort,
                                              @RequestParam(required = false) String tag,
                                              @RequestParam(required = false) String q,
                                              @RequestParam(required = false) Long author,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "15") int size) {
        return questionService.list(parseSort(sort), tag, q, author, page, size);
    }

    @GetMapping("/{id}")
    public QuestionDetail get(@PathVariable Long id) {
        return questionService.view(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionDetail create(@Valid @RequestBody QuestionRequest req) {
        return questionService.create(req);
    }

    @PutMapping("/{id}")
    public QuestionDetail update(@PathVariable Long id, @Valid @RequestBody QuestionRequest req) {
        return questionService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        questionService.delete(id);
    }

    @PostMapping("/{id}/answers")
    @ResponseStatus(HttpStatus.CREATED)
    public AnswerDto answer(@PathVariable Long id, @Valid @RequestBody AnswerRequest req) {
        return answerService.create(id, req);
    }

    private static SortOrder parseSort(String sort) {
        try {
            return SortOrder.valueOf(sort.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return SortOrder.NEWEST;
        }
    }
}
