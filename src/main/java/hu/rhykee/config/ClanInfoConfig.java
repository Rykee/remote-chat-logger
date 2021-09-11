package hu.rhykee.config;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("Remote Clan Info Logger")
public interface ClanInfoConfig extends Config {

    @ConfigSection(
            name = "Connection",
            description = "Connection details for sending clan related info",
            position = 0,
            closedByDefault = true
    )
    String connection = "connection";

    @ConfigItem(
            position = 2,
            keyName = "authConfig",
            name = "Authorization header",
            description = "The Authorization header's value. E.g.: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJSaHlrZ",
            section = connection
    )
    default String authorization() {
        return "";
    }

    @ConfigItem(
            position = 2,
            keyName = "clanChat",
            name = "Clan chat URL",
            description = "The URL used to submit clan chat logs",
            section = connection
    )
    default String clanChatUrl() {
        return "";
    }

    @ConfigItem(
            position = 3,
            keyName = "addMember",
            name = "Add clan members URL",
            description = "The URL used when a new clan member joined",
            section = connection
    )
    default String addClanMembersUrl() {
        return "";
    }

    @ConfigItem(
            position = 4,
            keyName = "removeMember",
            name = "Remove clan member URL",
            description = "The URL used when a clan member leaves",
            section = connection
    )
    default String removeClanMemberUrl() {
        return "";
    }


}
