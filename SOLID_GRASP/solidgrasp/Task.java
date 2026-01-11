package solidgrasp;
import java.time.LocalDate;
import java.util.Set;
import java.util.HashSet;

public class Task {
    private String name;
    private String description; 
    private LocalDate dueDate;
    private String status;
    private int priority;
    private Set<TeamMember> assignees;

    public Task(String name, String description, LocalDate dueDate, String status, int priority) {
        this.name = name;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status;
        this.priority = priority;
        this.assignees = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public String getStatus() {
        return status;
    }

    public int getPriority() {
        return priority;
    }

    public Set<TeamMember> getAssignees() {
        return assignees;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
