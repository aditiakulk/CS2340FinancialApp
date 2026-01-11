package solidgrasp;
public interface CloseTasks {
    default void closeTask(Project project, Task task) {
        project.getTasks().remove(task);
    }
}
