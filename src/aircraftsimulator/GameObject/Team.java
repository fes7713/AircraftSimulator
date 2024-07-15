package aircraftsimulator.GameObject;

import java.util.UUID;

public class Team {
    private final String teamName;
    private final String pw;

    public Team(String name)
    {
        teamName = name;
        this.pw = UUID.randomUUID().toString();
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
