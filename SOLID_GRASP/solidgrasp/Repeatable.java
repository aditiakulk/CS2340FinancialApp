package solidgrasp;
import java.time.LocalDate;

public interface Repeatable {
    LocalDate getNextDueDate(LocalDate currentDueDate);
    default void repeatTask(Task task) {
        task.setDueDate(getNextDueDate(task.getDueDate()));
    }
}
