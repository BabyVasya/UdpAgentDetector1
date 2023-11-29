package org.example;

import jade.core.AID;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

public class AgentInfoDto {
    @Getter
    @Setter
    private String agentName;

    @Getter
    @Setter
    private boolean isGuid;

    public AgentInfoDto(String agentName, boolean isGuid) {
        this.agentName = agentName;
        this.isGuid = isGuid;
    }
    public AID toAID() {
        return new AID(this.agentName, this.isGuid);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentInfoDto agent = (AgentInfoDto) o;
        return Objects.equals(agentName, agent.agentName);
    }

    @Override
    public String toString() {
        return "AgentInfoDto{" +
                "agentName='" + agentName + '\'' +
                ", isGuid=" + isGuid +
                '}';
    }
    @Override
    public int hashCode() {
        return Objects.hash(agentName);
    }
}
