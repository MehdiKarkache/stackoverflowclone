package com.stackclone.repository;

import com.stackclone.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    Optional<Vote> findByUserIdAndPostId(Long userId, Long postId);

    List<Vote> findByUserIdAndPostIdIn(Long userId, Collection<Long> postIds);
}
