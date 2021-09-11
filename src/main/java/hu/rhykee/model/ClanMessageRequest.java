package hu.rhykee.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ClanMessageRequest {

    @JsonProperty
    private String sender;

    @JsonProperty
    private String message;

    @JsonProperty
    private String clanName;

    @JsonProperty
    private String loggedBy;

    @JsonProperty
    private OffsetDateTime timestamp;

}
