package com.salah.taskmate.task;

import com.salah.taskmate.category.Category;
import com.salah.taskmate.task.enums.TaskPriority;
import com.salah.taskmate.task.enums.TaskStatus;
import com.salah.taskmate.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid",  nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 1000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Column(name = "created_at", nullable = false,  updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean isFavorite = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany
    @JoinTable(
            name = "task_category",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();


    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = TaskStatus.PENDING;
        }
        if (priority == null) {
            priority = TaskPriority.MEDIUM;
        }
    }
}
