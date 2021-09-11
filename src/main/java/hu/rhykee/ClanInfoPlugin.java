package hu.rhykee;

import com.google.inject.Provides;
import hu.rhykee.config.ClanInfoConfig;
import hu.rhykee.model.ClanMembersRequest;
import hu.rhykee.model.ClanMessageRequest;
import hu.rhykee.panel.ClanInfoPanel;
import hu.rhykee.web.HttpMessageSender;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ClanMemberJoined;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;

@Slf4j
@PluginDescriptor(
        name = "Remote Clan Info Logger"
)
public class ClanInfoPlugin extends Plugin {

    private final HttpMessageSender messageSender = HttpMessageSender.getInstance();

    @Inject
    private Client client;

    @Inject
    private ClanInfoConfig config;

    @Inject
    private ClientToolbar clientToolbar;


    @Override
    protected void startUp() {
        clientToolbar.addNavigation(createPanelButton());
        log.info("Remote Clan Info Logger started!");
    }

    @Override
    protected void shutDown() {
        log.info("Remote Clan Info Logger stopped!");
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (config.clanChatUrl().isBlank()) {
            return;
        }
        switch (event.getType()) {
            case CLAN_CHAT: {
                ClanMessageRequest message = convertToClanMessage(event);
                log.info("Received clan chat message: " + message);
                messageSender.sendPostHttpMessage(config.clanChatUrl(), message, config.authorization());
                break;
            }
            case CLAN_MESSAGE: {
                log.info("Clan system message: " + event.getMessage());
            }
        }
    }

    @Subscribe
    public void onClanMemberJoined(ClanMemberJoined event) {
        if (config.addClanMembersUrl().isBlank()) {
            return;
        }
        String playerName = event.getClanMember().getName();
        log.info("Clan member joined! Name: " + playerName);
        ClanMembersRequest request = new ClanMembersRequest(Collections.singletonList(playerName));
        messageSender.sendPostHttpMessage(config.addClanMembersUrl(), request, config.authorization());
    }

   /* @Subscribe
    public void onClanMemberLeft(ClanMemberLeft event) {
        if (config.removeClanMemberUrl().isBlank()) {
            return;
        }
        String playerName = event.getClanMember().getName();
        log.info("Clan member left! Name: " + playerName);
        RemoveClanMembersRequest request = new RemoveClanMembersRequest(Collections.singletonList(playerName));
        messageSender.sendDeleteHttpMessage(config.removeClanMemberUrl(), request, config.authorization());
    }*/

    @Provides
    ClanInfoConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ClanInfoConfig.class);
    }

    private NavigationButton createPanelButton() {
        return NavigationButton.builder()
                .icon(ImageUtil.loadImageResource(getClass(), "/Clan_Chat.png"))
                .panel(new ClanInfoPanel(() -> client, config))
                .tooltip("Clan manager tools")
                .priority(5)
                .build();
    }

    private ClanMessageRequest convertToClanMessage(ChatMessage event) {
        return ClanMessageRequest.builder()
                .message(event.getMessage())
                .clanName(event.getSender())
                .sender(event.getName()
                        .replaceAll("<img=[0-9].*>", "")
                        .replaceAll("\\u00A0", " "))
                .timestamp(OffsetDateTime.ofInstant(Instant.ofEpochSecond(event.getTimestamp()), ZoneOffset.UTC))
                .loggedBy(client.getLocalPlayer().getName())
                .build();
    }

}
