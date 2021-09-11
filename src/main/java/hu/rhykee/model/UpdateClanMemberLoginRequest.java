package hu.rhykee.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UpdateClanMemberLoginRequest {

    @JsonProperty
    private String username;

    @JsonProperty
    private OffsetDateTime lastLoginTime;

}
