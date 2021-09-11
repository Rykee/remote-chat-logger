package hu.rhykee.panel;

import hu.rhykee.config.ClanInfoConfig;
import hu.rhykee.model.ClanMembersRequest;
import hu.rhykee.web.HttpMessageSender;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.clan.ClanMember;
import net.runelite.api.clan.ClanSettings;
import net.runelite.client.ui.PluginPanel;

import javax.swing.JButton;
import java.awt.BorderLayout;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Slf4j
public class ClanInfoPanel extends PluginPanel {

    private final Supplier<Client> clientSupplier;
    private final HttpMessageSender messageSender = HttpMessageSender.getInstance();
    private final ClanInfoConfig config;

    public ClanInfoPanel(Supplier<Client> clientSupplier, ClanInfoConfig config) {
        this.clientSupplier = clientSupplier;
        this.config = config;
        getParent().setLayout(new BorderLayout());
        getParent().add(this, BorderLayout.NORTH);
        add(createSendClanMembersButton());
    }

    private JButton createSendClanMembersButton() {
        JButton copyClanMembers = new JButton("Send clan members list");
        copyClanMembers.addActionListener(e -> {
            if (config.addClanMembersUrl().isBlank()) {
                return;
            }
            List<String> clanMemberNames = getClanMemberNames();
            if (clanMemberNames.isEmpty()) {
                log.warn("Can't send clan member names, because you are not in a clan or not logged in!");
            } else {
                ClanMembersRequest request = new ClanMembersRequest(clanMemberNames);
                messageSender.sendPostHttpMessage(config.addClanMembersUrl(), request, config.authorization());
            }
        });
        return copyClanMembers;
    }

    private List<String> getClanMemberNames() {
        return ofNullable(clientSupplier.get().getClanSettings())
                .map(ClanSettings::getMembers)
                .orElse(Collections.emptyList())
                .stream()
                .map(ClanMember::getName)
                .map(name -> name.replaceAll("\\u00A0", " "))
                .collect(Collectors.toList());
    }

}
