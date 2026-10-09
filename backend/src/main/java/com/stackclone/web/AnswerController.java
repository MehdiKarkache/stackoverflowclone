package com.stackclone.web;

import com.stackclone.dto.PostDtos.AnswerDto;
import com.stackclone.dto.PostDtos.AnswerRequest;
import com.stackclone.service.AnswerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/answers")
public class AnswerController {

    private final AnswerService answerService;

    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    @PutMapping("/{id}")
    public AnswerDto update(@PathVariable Long id, @Valid @RequestBody AnswerRequest req) {
        return answerService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        answerService.delete(id);
    }

    @PostMapping("/{id}/accept")
    public AnswerDto accept(@PathVariable Long id) {
        return answerService.toggleAccept(id);
    }
}
