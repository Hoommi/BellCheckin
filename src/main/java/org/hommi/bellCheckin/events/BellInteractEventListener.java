package org.hommi.bellCheckin.events;

import me.TechsCode.UltraEconomy.objects.Account;
import me.TechsCode.UltraEconomy.objects.Currency;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BellRingEvent;
import org.hommi.bellCheckin.BellCheckin;
import org.hommi.bellCheckin.manager.CheckinManager;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public class BellInteractEventListener implements Listener {

    @EventHandler
    public void onBellInteract(BellRingEvent event) {
        if (event.getEntity() instanceof Player player) {
            Optional<Account> optionalAccount =
                    BellCheckin.getInstance().ultraEconomy.getAccounts()
                            .stream()
                            .filter(account -> account.getUuid()
                                    .equals(player.getUniqueId()))
                            .findFirst();
            if (optionalAccount.isEmpty()) {
                Component message = Component.text("Bạn chưa có tài khoản trong hệ thống!")
                        .color(net.kyori.adventure.text.format.NamedTextColor.RED);
                player.sendMessage(message);
                return;
            }
            Account account = optionalAccount.get();
            Currency currency = BellCheckin.getInstance().ultraEconomy.getCurrencies()
                    .get(0);

            CheckinManager checkinManager = BellCheckin.getInstance()
                    .getCheckinManager();
            long now = Instant.now().getEpochSecond();
            boolean canCheckin = checkinManager.canCheckin(player.getUniqueId(), now);
            if (canCheckin) {
                account.addBalance(currency, 1000);
                Component message = Component.text(
                                "Bạn đã điểm danh thành công! và nhận được 1000" + currency.getName())
                        .color(net.kyori.adventure.text.format.NamedTextColor.GREEN);
                player.sendMessage(message);
                checkinManager.checkin(player.getUniqueId(), now);
            }
            else {
                long secondsLeft = checkinManager.secondsUntilNextCheckin(player.getUniqueId(), now);
                Duration duration = Duration.ofSeconds(secondsLeft);
                long hours = duration.toHours();
                long minutes = duration.minusHours(hours)
                        .toMinutes();
                long seconds = duration.minusHours(hours)
                        .minusMinutes(minutes)
                        .getSeconds();
                String timeLeft = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                Component message = Component.text(
                                "Bạn cần chờ thêm " + timeLeft + " nữa để điểm danh lại!")
                        .color(net.kyori.adventure.text.format.NamedTextColor.YELLOW);
                player.sendMessage(message);
            }
        }
    }
}
