package com.stackclone.repository;

import com.stackclone.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    List<Tag> findByNameIn(Collection<String> names);

    /** Tags avec leur nombre de questions, les plus utilisés en premier. */
    @Query(value = """
            select new com.stackclone.repository.TagUsage(t.id, t.name, t.description, count(q))
            from Tag t left join Question q on t member of q.tags
            where lower(t.name) like lower(concat('%', :q, '%'))
            group by t.id, t.name, t.description
            order by count(q) desc, t.name asc
            """,
            countQuery = "select count(t) from Tag t where lower(t.name) like lower(concat('%', :q, '%'))")
    Page<TagUsage> searchWithUsage(String q, Pageable pageable);
}
