package snvn.taskmanagementservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import snvn.taskmanagementservice.domain.AppUser;
import snvn.taskmanagementservice.domain.Task;
import snvn.taskmanagementservice.domain.TaskStatus;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByDeletedFalse(Pageable pageable);
    Page<Task> findByDeletedFalseAndStatus(TaskStatus status, Pageable pageable);
    Page<Task> findByDeletedFalseAndOwner(AppUser owner, Pageable pageable);
    Page<Task> findByDeletedFalseAndOwnerAndStatus(AppUser owner, TaskStatus status, Pageable pageable);
}

