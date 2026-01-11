package solidgrasp;
import java.util.HashSet;
import java.util.Set;

public class Project {
    
    private String projectName;
    private Overseer overseer;
    private Set<Task> tasks;
    private Set<TeamMember> members;

    public Project(String projectName, Overseer overseer) {
        this.projectName = projectName;
        this.overseer = overseer;
        this.tasks = new HashSet<>();
        this.members = new HashSet<>();
    }
    
    public void addTask(Task task) {
        tasks.add(task);
    }

    public void addMember(TeamMember member) {
        members.add(member);
    }

    public void removeMember(TeamMember member) {
        members.remove(member);
    }

    public void removeTask(Task task) {
        tasks.remove(task);
    }

    public Set<Task> getTasks() {
        return tasks;
    }

    public Set<TeamMember> getMembers() {
        return members;
    }

    public String getProjectName() {
        return projectName;
    }

    public Overseer getOverseer() {
        return overseer;
    }
}
