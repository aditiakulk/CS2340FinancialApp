package solidgrasp;
import java.time.LocalDate;

public class DailyRecurringTask extends Task implements Repeatable {

    public DailyRecurringTask(String name, String description, LocalDate dueDate, String status, int priority) {
        super(name, description, dueDate, status, priority);
    }

    @Override
    public LocalDate getNextDueDate(LocalDate currentDueDate) {
        return currentDueDate.plusDays(1);
    }
}
