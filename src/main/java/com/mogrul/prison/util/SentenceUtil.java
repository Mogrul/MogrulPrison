package com.mogrul.prison.util;

import com.mogrul.prison.manager.LocaleManager;
import com.mogrul.prison.model.Prisoner;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.time.Duration;

public class SentenceUtil {
    public static void sendFirstJoinMessage(Player player, Prisoner prisoner) {
        player.showTitle(Title.title(
                LocaleManager.get("event.first-join.title")
                        .color(NamedTextColor.GOLD),
                LocaleManager.get("event.first-join.charge", prisoner.getSentence())
                        .color(NamedTextColor.GRAY),
                Title.Times.times(
                        Duration.ofSeconds(2),
                        Duration.ofSeconds(10),
                        Duration.ofSeconds(2)
                )
        ));
    }
}
