package com.intecx.stride.repository;

import com.intecx.stride.entity.Task;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    Optional<Task> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsByStory_IdAndDeletedAtIsNull(UUID storyId);

    List<Task> findByStory_IdAndDeletedAtIsNull(UUID storyId);

    List<Task> findByStory_Id(UUID storyId);

    List<Task> findByStory_Project_IdAndDeletedAtIsNull(UUID projectId);

    List<Task> findByStory_Project_Id(UUID projectId);

    @Modifying
    @Query(value = "DELETE FROM in_scope.task_assignments WHERE task_id IN (:taskIds)", nativeQuery = true)
    void deleteAssignmentsByTaskIds(@Param("taskIds") Collection<UUID> taskIds);

    @Modifying
    @Query(value = "DELETE FROM in_scope.task_assignments WHERE technical_profile_id = :profileId", nativeQuery = true)
    void deleteAssignmentsByProfileId(@Param("profileId") UUID profileId);

    @Query("SELECT COALESCE(MAX(t.position), 0) FROM Task t "
            + "WHERE t.story.id = :storyId AND t.deletedAt IS NULL")
    int findLastPositionByStoryId(@Param("storyId") UUID storyId);
}
