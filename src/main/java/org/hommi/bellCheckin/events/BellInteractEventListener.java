package org.hommi.bellCheckin.events;

import me.TechsCode.UltraEconomy.objects.Account;
import me.TechsCode.UltraEconomy.objects.Currency;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BellRingEvent;
import org.hommi.bellCheckin.BellCheckin;
import org.hommi.bellCheckin.ConfigKey;
import org.hommi.bellCheckin.manager.BellLocationManager;
import org.hommi.bellCheckin.manager.CheckinManager;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public class BellInteractEventListener implements Listener {

        @EventHandler
        public void onBellInteract(BellRingEvent event) {
                if (event.getEntity() instanceof Player player) {
                        Location bellLoc = event.getBlock().getLocation();
                        BellLocationManager bellLocationManager = BellCheckin.getInstance().getBellLocationManager();
                        // Check if the bell location is a registered check-in bell
                        boolean isCheckinBell = bellLocationManager.getBellLocations().stream().anyMatch(
                                        loc -> loc.worldName().equals(bellLoc.getWorld().getName()) &&
                                                        loc.x() == bellLoc.getX() &&
                                                        loc.y() == bellLoc.getY() &&
                                                        loc.z() == bellLoc.getZ());
                        if (!isCheckinBell) {
                                return;
                        }
                        Optional<Account> optionalAccount = BellCheckin.getInstance().ultraEconomy.getAccounts()
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
                                // Read reward amount from config
                                int rewardAmount = BellCheckin.getInstance().getConfig()
                                                .getInt(ConfigKey.REWARDS_DAILY_AMOUNT, 1000);
                                account.addBalance(currency, rewardAmount);
                                Component message = Component.text(
                                                "Bạn đã điểm danh thành công! và nhận được " + rewardAmount + " "
                                                                + currency.getName())
                                                .color(net.kyori.adventure.text.format.NamedTextColor.GREEN);
                                player.sendMessage(message);
                                checkinManager.checkin(player.getUniqueId(), now);
                        } else {
                                long secondsLeft = checkinManager.secondsUntilNextCheckin(player.getUniqueId(), now);
                                final Component message = getComponent(secondsLeft);
                                player.sendMessage(message);
                        }
                }
        }

        private @NotNull Component getComponent(long secondsLeft) {
                Duration duration = Duration.ofSeconds(secondsLeft);
                long hours = duration.toHours();
                long minutes = duration.minusHours(hours)
                                .toMinutes();
                long seconds = duration.minusHours(hours)
                                .minusMinutes(minutes)
                                .getSeconds();
                String timeLeft = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                return Component.text(
                                "Bạn cần chờ thêm " + timeLeft + " nữa để điểm danh lại!")
                                .color(net.kyori.adventure.text.format.NamedTextColor.YELLOW);
        }
}
