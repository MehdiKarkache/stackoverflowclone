package com.stackclone.repository;

import com.stackclone.model.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    Page<Answer> findByAuthorId(Long authorId, Pageable pageable);

    long countByAuthorId(Long authorId);
}
