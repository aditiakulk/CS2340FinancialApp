package solidgrasp;
public interface AssignTasks {
    default void assignTask(Task task, TeamMember assignee) {
        task.getAssignees().add(assignee);
    }
}
