package org.hommi.bellCheckin.events;

import me.TechsCode.UltraEconomy.objects.Account;
import me.TechsCode.UltraEconomy.objects.Currency;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BellRingEvent;
import org.hommi.bellCheckin.BellCheckin;
import org.hommi.bellCheckin.manager.BellLocationManager;
import org.hommi.bellCheckin.manager.CheckinManager;
import org.hommi.bellCheckin.manager.ConfigManager;
import org.hommi.bellCheckin.manager.LanguageManager;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public class BellInteractEventListener implements Listener {

    @EventHandler
    public void onBellInteract(BellRingEvent event) {
        if (event.getEntity() instanceof Player player) {
            Location bellLoc = event.getBlock()
                    .getLocation();
            BellLocationManager bellLocationManager = BellCheckin.getInstance()
                    .getBellLocationManager();

            // Kiểm tra xem chuông có phải là chuông điểm danh không
            boolean isCheckinBell = bellLocationManager.isCheckinBell(
                    bellLoc.getWorld()
                            .getName(),
                    bellLoc.getBlockX(),
                    bellLoc.getBlockY(),
                    bellLoc.getBlockZ());

            if (!isCheckinBell) {
                return;
            }

            // Kiểm tra tài khoản người chơi
            Optional<Account> optionalAccount = BellCheckin.getInstance().ultraEconomy.getAccounts()
                    .stream()
                    .filter(account -> account.getUuid()
                            .equals(player.getUniqueId()))
                    .findFirst();

            if (optionalAccount.isEmpty()) {
                Component message = Component.text("Bạn chưa có tài khoản trong hệ thống!")
                        .color(NamedTextColor.RED);
                player.sendMessage(message);
                return;
            }

            Account account = optionalAccount.get();
            Currency currency = BellCheckin.getInstance().ultraEconomy.getCurrencies()
                    .get(0);
            CheckinManager checkinManager = BellCheckin.getInstance()
                    .getCheckinManager();
            ConfigManager configManager = BellCheckin.getInstance()
                    .getConfigManager();
            LanguageManager langManager = BellCheckin.getInstance()
                    .getLanguageManager();

            long now = Instant.now()
                    .getEpochSecond();
            boolean canCheckin = checkinManager.canCheckin(player.getUniqueId(), now);

            if (canCheckin) {
                // Thực hiện điểm danh
                boolean isStreakMilestone = checkinManager.checkin(player.getUniqueId(), now);

                // Phần thưởng hàng ngày
                if (configManager.isDailyRewardEnabled()) {
                    int rewardAmount = configManager.getDailyRewardAmount();
                    account.addBalance(currency, rewardAmount);

                    Component message = Component
                            .text(langManager.getDailyRewardMessage(rewardAmount))
                            .color(NamedTextColor.GREEN);
                    player.sendMessage(message);

                    // Thực thi lệnh nếu có
                    String command = configManager.getDailyRewardCommand();
                    if (!command.isEmpty()) {
                        executeCommand(command, player);
                    }
                }

                // Phần thưởng streak nếu đạt milestone
                if (isStreakMilestone && configManager.isStreakRewardEnabled()) {
                    int streak = checkinManager.getCurrentStreak(player.getUniqueId());
                    int streakReward = configManager.getStreakMilestoneReward(streak);

                    if (streakReward > 0) {
                        account.addBalance(currency, streakReward);
                        Component message = Component
                                .text(langManager.getStreakRewardMessage(streak))
                                .color(NamedTextColor.AQUA);
                        player.sendMessage(message);

                        // Thực thi lệnh nếu có
                        String command = configManager.getStreakRewardCommand();
                        if (!command.isEmpty()) {
                            executeCommand(command, player);
                        }
                    }
                }
            }
            else {
                // Thông báo thời gian còn lại
                long secondsLeft = checkinManager.secondsUntilNextCheckin(player.getUniqueId(),
                        now);
                Component message = formatTimeLeft(secondsLeft);
                player.sendMessage(message);
            }
        }
    }

    private void executeCommand(String command, Player player) {
        // Thay thế các placeholder
        command = command.replace("%player%", player.getName())
                .replace("%uuid%", player.getUniqueId()
                        .toString());

        // Thực thi lệnh với quyền console
        BellCheckin.getInstance()
                .getServer()
                .dispatchCommand(
                        BellCheckin.getInstance()
                                .getServer()
                                .getConsoleSender(),
                        command);
    }

    private @NotNull Component formatTimeLeft(long secondsLeft) {
        Duration duration = Duration.ofSeconds(secondsLeft);
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours)
                .toMinutes();
        long seconds = duration.minusHours(hours)
                .minusMinutes(minutes)
                .getSeconds();
        String timeLeft = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        return Component.text("Bạn cần chờ thêm " + timeLeft + " nữa để điểm danh lại!")
                .color(NamedTextColor.YELLOW);
    }
}
