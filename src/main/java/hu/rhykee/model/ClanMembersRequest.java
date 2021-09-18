package hu.rhykee.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ClanMembersRequest {

    @JsonProperty
    private List<String> members;

    public ClanMembersRequest(String singlePlayer) {
        this.members = Collections.singletonList(singlePlayer);
    }

}
