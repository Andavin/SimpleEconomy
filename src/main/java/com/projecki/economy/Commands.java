package com.projecki.economy;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.projecki.economy.ChatFormats.*;

/**
 * @since February 22, 2022
 * @author Andavin
 */
public record Commands(SimpleEconomy economy) {

    private static final DecimalFormat FORMAT = new DecimalFormat("#,###");
    public static final long THOUSAND = 1000, MILLION = 1000000, BILLION = 1000000000;

    public void eco(CommandSender sender, String[] args) {
        switch (args.length) {
            case 0 -> sender.spigot().sendMessage(error("No command specified"));
            case 1 -> sender.spigot().sendMessage(error("No target specified"));
            case 2 -> sender.spigot().sendMessage(error("No amount specified"));
            default -> {
                String name = args[1];
                economy.getData(name).ifPresentOrElse(
                        data -> this.execute(sender, data, args),
                        () -> economy.load(name).thenAccept(data -> {

                            if (data != null) {
                                this.execute(sender, data, args);
                            } else {
                                sender.spigot().sendMessage(error(nameValue(ChatColor.RED, "Player Not Found", name)));
                            }
                        })
                );
            }
        }

    }

    private void execute(CommandSender sender, EconomyData data, String[] args) {

        long amount;
        try {
            amount = parseLongWithSuffix(args[2]);
        } catch (NumberFormatException e) {
            sender.spigot().sendMessage(error(nameValue(ChatColor.RED, "Invalid amount ", args[2])));
            return;
        }

        switch (args[0]) {
            case "add", "give" -> this.ecoAdd(sender, data, amount);
            case "remove", "take" -> this.ecoRemove(sender, data, amount);
            case "set" -> this.ecoSet(sender, data, amount);
        }
    }

    private void ecoAdd(CommandSender sender, EconomyData data, long amount) {
        data.add(amount);
        sender.spigot().sendMessage(line(
                ChatColor.GREEN, "Added ", ChatColor.WHITE, FORMAT.format(amount),
                ChatColor.GREEN, " to ", ChatColor.WHITE, data.getName()
        ));
    }

    private void ecoRemove(CommandSender sender, EconomyData data, long amount) {
        data.subtract(amount);
        sender.spigot().sendMessage(line(
                ChatColor.GREEN, "Removed ", ChatColor.WHITE, FORMAT.format(amount),
                ChatColor.GREEN, " from ", ChatColor.WHITE, data.getName()
        ));
    }

    private void ecoSet(CommandSender sender, EconomyData data, long amount) {
        data.set(amount);
        sender.spigot().sendMessage(line(
                ChatColor.GREEN, "Set ", ChatColor.WHITE, data.getName() + "'s",
                ChatColor.GREEN, " balance to ", ChatColor.WHITE, FORMAT.format(amount)
        ));
    }

    public void balance(CommandSender sender, String[] args) {

        if (args.length == 0) {

            if (sender instanceof Player player) {
                EconomyData data = economy.getData(player.getUniqueId()).orElseThrow();
                sender.spigot().sendMessage(nameValue("Balance", FORMAT.format(data.getBalance())));
            } else {
                sender.spigot().sendMessage(error("No balance available"));
            }
        } else if (sender.hasPermission("economy.commands.balance.other")) {

            String name = args[0];
            Optional<EconomyData> optionalData = economy.getData(name);
            if (optionalData.isPresent()) {
                EconomyData data = optionalData.get();
                sender.spigot().sendMessage(nameValue(data.getName() + "Balance",
                        FORMAT.format(data.getBalance())));
            } else {
                economy.load(name).thenAccept(data -> {

                    if (data != null) {
                        sender.spigot().sendMessage(nameValue(data.getName() + "Balance",
                                FORMAT.format(data.getBalance())));
                    } else {
                        sender.spigot().sendMessage(error(nameValue(ChatColor.RED, "Player Not Found", name)));
                    }
                });
            }
        } else {
            sender.spigot().sendMessage(error("Insufficient Permission"));
        }
    }

    /**
     * Parse the given string to a long allowing for
     * the following suffixes:
     * <ul>
     *     <li>{@code k} or {@code K} for thousand</li>
     *     <li>{@code m} or {@code M} for million</li>
     *     <li>{@code b} or {@code B} for billion</li>
     * </ul>
     * If any of the suffixes are present, then the
     * string will be parsed as a double rather than a
     * long, multiplied by the applicable number and
     * cast to a long.
     *
     * @param s The string to parse the long from.
     * @return The parsed long.
     * @see Long#parseLong(String)
     * @see Double#parseDouble(String)
     */
    private static long parseLongWithSuffix(String s) {

        int length = s.length();
        checkArgument(length > 0, "invalid length: %s", length);
        long multiplier;
        switch (s.charAt(length - 1)) {
            case 'k':
            case 'K':
                multiplier = THOUSAND;
                break;
            case 'm':
            case 'M':
                multiplier = MILLION;
                break;
            case 'b':
            case 'B':
                multiplier = BILLION;
                break;
            default:
                return Long.parseLong(s);
        }
        // No need to check for overflow since double cannot overflow
        // instead it will be clamped to Long.MAX_VALUE or Long.MIN_VALUE
        return (long) (Double.parseDouble(s.substring(0, length - 1)) * multiplier);
    }

    /*
     * Note that above is not my preferred way of managing commands.
     * My own platform independent command API is not quite yet complete, but,
     * if used, would look similar to these commands below:
     *

    @Command(
            names = { "eco", "economy" },
            usage = "<add|remove|set|show>",
            help = "Default economy management command"
    )
    @PermissionNode("economy.command.eco")
    public void eco(User<?> user, @Remainder String arguments) {
        checkArgumentFound(arguments.isEmpty(), "Command", arguments);
        user.sendMessage(new MessageBuilder(Format.RED, "Please specify a command"));
    }

    @Command(
            names = { "add", "give", "deposit" },
            usage = "[target] <amount> [economy]",
            help = """
                    Add the economic value amount to a balance
                    using the default economy if one is not specified

                    Flags:
                     -e The specific economy"""
    )
    @NestedCommand("eco")
    @PermissionNode("economy.command.eco.add")
    public void add(User<?> user,
                    @PermissionNode("economy.command.eco.add.other") @Optional User<?> target,
                    @Parsable("currency") long amount,
                    @Completable("economy") @Parsable("economy")
                    @Value('e') Class<? extends Economy> economy) {
        accept(user, target != null ? target : user, amount, economy,
                UserEconomy::deposit, "Added", "to");
    }

    @Command(
            names = { "remove", "take", "withdraw" },
            usage = "[target] <amount> [economy]",
            help = """
                    Remove the economic value amount from a balance
                    using the default economy if one is not specified

                    Flags:
                     -e The specific economy"""
    )
    @NestedCommand("eco")
    @PermissionNode("economy.command.eco.remove")
    public void remove(User<?> user,
                       @PermissionNode("economy.command.eco.remove.other") @Optional User<?> target,
                       @Parsable("currency") long amount,
                       @Completable("economy") @Parsable("economy")
                       @Value('e') Class<? extends Economy> economy) {
        accept(user, target != null ? target : user, amount, economy,
                UserEconomy::withdraw, "Removed", "from");
    }

    @Command(
            names = "set",
            usage = "[target] <amount> [economy]",
            help = """
                    Set the economic value of a balance
                    using the default economy if one is not specified

                    Flags:
                     -e The specific economy"""
    )
    @NestedCommand("eco")
    @PermissionNode("economy.command.eco.set")
    public void set(User<?> user,
                    @PermissionNode("economy.command.eco.set.other") @Optional User<?> target,
                    @Parsable("currency") long amount,
                    @Completable("economy") @Parsable("economy")
                    @Value('e') Class<? extends Economy> economy) {
        accept(user, target != null ? target : user, amount, economy,
                UserEconomy::set, "Set", "to");
    }

    @Command(
            names = { "show", "bal", "balance" },
            usage = "[target] [economy]",
            help = """
                    Show the economic value of a balance
                    using the default economy if one is not specified

                    Flags:
                     -e The specific economy"""
    )
    @NestedCommand("eco")
    @PermissionNode("economy.command.eco.show")
    public void set(User<?> user,
                    @PermissionNode("economy.command.eco.show.other") @Optional User<?> target,
                    @Completable("economy") @Parsable("economy")
                    @Value('e') Class<? extends Economy> economy) {

        target = target != null ? target : user;
        Class<? extends Economy> ecoType = economy != null ?
                economy : EconomyAPI.getDefaultEconomy();
        target.handle(UserEconomy.class, attach -> {
            Economy eco = attach.getEconomy(ecoType);
            user.sendMessage(new MessageBuilder(Format.GREEN, "Balance ")
                    .append(Format.WHITE, eco.formatAmount(attach.getBalance(ecoType)) + ' ' + eco.getName()));
        });
    }

    private static void accept(User<?> user, User<?> target, long amount, Class<? extends Economy> economyType,
                               EconomyAction action, String actionName, String actionPronoun) {
        UserEconomy ecoUser = target.get(UserEconomy.class);
        Class<? extends Economy> ecoType = economyType != null ?
                economyType : EconomyAPI.getDefaultEconomy();
        action.apply(ecoUser, ecoType, amount).apply(
                left -> {
                    Economy economy = ecoUser.getEconomy(ecoType);
                    user.sendMessage(new MessageBuilder(Format.GREEN, actionName + ' ')
                            .append(Format.WHITE, economy.formatAmount(amount) + ' ' + economy.getName())
                            .append(Format.GREEN, ' ' + actionPronoun + ' ' +
                                    (user.equals(target) ? "your" : target.getName() + "'s") + " balance"));
                },
                right -> user.sendMessage(new MessageBuilder(Format.RED, "Economy error " + right.name()))
        );
    }

     */
}
