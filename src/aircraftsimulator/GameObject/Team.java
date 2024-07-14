package aircraftsimulator.GameObject;

public class Team {
    private final String teamName;
    private final String pw;

    public Team(String name, String pw)
    {
        teamName = name;
        this.pw = pw;
    }

    public String getTeamName()
    {
        return teamName;
    }

    public String getPW()
    {
        return pw;
    }
}
