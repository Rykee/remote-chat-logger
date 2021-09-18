package hu.rhykee;

import com.google.inject.Provides;
import hu.rhykee.config.ClanInfoConfig;
import hu.rhykee.model.ClanMemberOperation;
import hu.rhykee.model.ClanMembersRequest;
import hu.rhykee.model.ClanMessageRequest;
import hu.rhykee.model.UpdateClanMemberLoginRequest;
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
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static hu.rhykee.model.ClanMemberOperation.ADD;
import static hu.rhykee.model.ClanMemberOperation.REMOVE;

@Slf4j
@PluginDescriptor(
        name = "Remote Clan Info Logger"
)
public class ClanInfoPlugin extends Plugin {

    private static final Pattern KICKED_FROM_CLAN = Pattern.compile("(?<expeller>[\\w\\d ]+) has expelled (?<expellee>[\\w\\d ]+) from the clan");
    private static final Pattern INVITED_TO_CLAN = Pattern.compile("(?<invitee>[\\w\\d ]+) has been invited to the clan by (?<inviter>[\\w\\d ]+)");
    private static final Pattern LEFT_THE_CLAN = Pattern.compile("(?<player>[\\w\\d ]+) has left the clan");
    private final HttpMessageSender messageSender = HttpMessageSender.getInstance();
    private final EnumMap<ClanMemberOperation, Consumer<String>> OPERATION_MAP = new EnumMap<>(ClanMemberOperation.class);

    @Inject
    private Client client;

    @Inject
    private ClanInfoConfig config;

    @Inject
    private ClientToolbar clientToolbar;
    private NavigationButton panelButton;

    @Override
    protected void startUp() {
        panelButton = createPanelButton();
        clientToolbar.addNavigation(panelButton);
        OPERATION_MAP.put(ADD, playerName -> messageSender.sendPostHttpMessage(config.addClanMembersUrl(), new ClanMembersRequest(playerName), config.authorization()));
        OPERATION_MAP.put(REMOVE, playerName -> messageSender.sendDeleteHttpMessage(config.removeClanMemberUrl(), new ClanMembersRequest(playerName), config.authorization()));
        log.info("Remote Clan Info Logger started!");
    }

    @Override
    protected void shutDown() {
        clientToolbar.removeNavigation(panelButton);
        log.info("Remote Clan Info Logger stopped!");
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (config.clanChatUrl().isBlank()) {
            return;
        }
        switch (event.getType()) {
            case CLAN_CHAT: {
                handleClanChatMessage(event);
                break;
            }
            case CLAN_MESSAGE: {
                handleClanSystemMessage(event.getMessage());
                break;
            }
        }
    }

    private void handleClanChatMessage(ChatMessage event) {
        ClanMessageRequest message = convertToClanMessage(event);
        log.info("Received clan chat message: " + message);
        messageSender.sendPostHttpMessage(config.clanChatUrl(), message, config.authorization());
    }

    private void handleClanSystemMessage(String message) {
        if (tryPattern(message, KICKED_FROM_CLAN, "expellee", REMOVE)) {
            return;
        }
        if (tryPattern(message, INVITED_TO_CLAN, "invitee", ADD)) {
            return;
        }
        tryPattern(message, LEFT_THE_CLAN, "player", REMOVE);
    }

    private boolean tryPattern(String input, Pattern pattern, String groupName, ClanMemberOperation operation) {
        Matcher matcher = pattern.matcher(input);
        if (matcher.matches()) {
            OPERATION_MAP.get(operation).accept(matcher.group(groupName));
            return true;
        } else {
            return false;
        }
    }

    @Subscribe
    public void onClanMemberJoined(ClanMemberJoined event) {
        if (config.lastLoginUrl().isBlank()) {
            return;
        }
        String playerName = event.getClanMember().getName();
        log.info("Clan member logged in! Name: " + playerName);
        UpdateClanMemberLoginRequest request = new UpdateClanMemberLoginRequest(playerName, LocalDateTime.now().atOffset(ZoneOffset.UTC));
        messageSender.sendPostHttpMessage(config.lastLoginUrl(), request, config.authorization());
    }

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
