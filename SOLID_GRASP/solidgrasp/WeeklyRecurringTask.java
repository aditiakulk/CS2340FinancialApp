package solidgrasp;
import java.time.LocalDate;

public class WeeklyRecurringTask extends Task implements Repeatable {

    public WeeklyRecurringTask(String name, String description, LocalDate dueDate, String status, int priority) {
        super(name, description, dueDate, status, priority);
    }

    @Override
    public LocalDate getNextDueDate(LocalDate currentDueDate) {
        return currentDueDate.plusWeeks(1);
    }
}
