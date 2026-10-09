package com.stackclone.repository;

import com.stackclone.model.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface QuestionRepository extends JpaRepository<Question, Long>, JpaSpecificationExecutor<Question> {

    Page<Question> findByAuthorId(Long authorId, Pageable pageable);

    long countByAuthorId(Long authorId);
}
