package solidgrasp;
public class TeamMember {
    private String name;
    private String email;

    public TeamMember(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void joinProject(Project project) {
        project.addMember(this);
    }

    public void leaveProject(Project project) {
        project.removeMember(this);
    }
}