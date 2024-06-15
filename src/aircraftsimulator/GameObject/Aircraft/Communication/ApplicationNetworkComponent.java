package aircraftsimulator.GameObject.Aircraft.Communication;

public interface ApplicationNetworkComponent extends NetworkComponent{
    void setResendLimit(Integer limit);
}
