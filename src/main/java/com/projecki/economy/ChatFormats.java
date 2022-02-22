package com.projecki.economy;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.List;

/**
 * @since December 14, 2020
 * @author Andavin
 */
public interface ChatFormats {

    // Example:
    // static BaseComponent[] itemName(ItemStack item, ItemQuality quality ...) {
    //     ChatColor color = colorForQuality(quality);
    //     return new ComponentBuilder(color, itemName(item))...
    // }

    /*
     * *** READ ME BEFORE ADDING ANY METHODS ***
     *
     * All methods should be relatively long descriptive
     * names so that it is clear what the purpose of
     * the method is without ChatFormats preceding it.
     *
     * In other words, so that it can be easily statically
     * imported and still be descriptive.
     */

    ChatColor[] EMPTY_COLOR_ARRAY = new ChatColor[0];
    BaseComponent[] BLANK = { new TextComponent("") };
    BaseComponent[][] EMPTY = new BaseComponent[0][];
    BaseComponent[] LOCKED = line(ChatColor.RED, "LOCKED");
    BaseComponent[] CROSS = line(ChatColor.RED, "✕");
    BaseComponent[] INSUFFICIENT_FUNDS = line(ChatColor.RED, ChatColor.BOLD, "Insufficient Funds");
    BaseComponent[] ERROR_START = line(ChatColor.RED, ChatColor.BOLD, "Error", ChatColor.WHITE, ": ");
    BaseComponent[] NEW_LINE = line("\n");

    static BaseComponent[] newLine() {
        return NEW_LINE;
    }

    static BaseComponent[] blank() {
        return BLANK;
    }

    static BaseComponent[][] emptyLines() {
        return EMPTY;
    }

    static BaseComponent[] insufficientFunds() {
        return INSUFFICIENT_FUNDS;
    }

    static BaseComponent[] error(String error) {
        return new ComponentBuilder()
                .append(ERROR_START)
                .append(error).color(ChatColor.GRAY).bold(false)
                .create();
    }

    static BaseComponent[] error(BaseComponent[] error) {
        return new ComponentBuilder()
                .append(ERROR_START)
                .append(error).bold(false)
                .create();
    }

    static BaseComponent[] name(String name) {
        return name(ChatColor.GREEN, name);
    }

    static BaseComponent[] name(ChatColor color, String name) {
        return new ComponentBuilder(name).color(color)
                .append(": ").color(ChatColor.WHITE)
                .create();
    }

    static BaseComponent[] nameValue(String name, String value) {
        return nameValue(ChatColor.GREEN, name, ChatColor.WHITE, value);
    }

    static BaseComponent[] nameValue(String name, ChatColor valueColor, String value) {
        return nameValue(ChatColor.GREEN, name, valueColor, value);
    }

    static BaseComponent[] nameValue(ChatColor nameColor, String name, String value) {
        return nameValue(nameColor, name, ChatColor.WHITE, value);
    }

    static BaseComponent[] nameValue(ChatColor nameColor, String name, ChatColor valueColor, String value) {
        return new ComponentBuilder(name).color(nameColor)
                .append(": ").color(ChatColor.WHITE)
                .append(TextComponent.fromLegacyText(value, valueColor))
                .create();
    }

    static BaseComponent[] nameValue(String name, BaseComponent[] value) {
        return nameValue(ChatColor.GREEN, name, ChatColor.WHITE, value);
    }

    static BaseComponent[] nameValue(ChatColor nameColor, String name, BaseComponent[] value) {
        return nameValue(nameColor, name, ChatColor.WHITE, value);
    }

    static BaseComponent[] nameValue(String name, ChatColor valueColor, BaseComponent[] value) {
        return nameValue(ChatColor.GREEN, name, valueColor, value);
    }

    static BaseComponent[] nameValue(ChatColor nameColor, String name, ChatColor valueColor, BaseComponent[] value) {

        for (BaseComponent component : value) {

            if (component.getColorRaw() == null) {
                component.setColor(valueColor);
            }
        }

        return new ComponentBuilder(name).color(nameColor)
                .append(": ").color(ChatColor.WHITE)
                .append(value)
                .create();
    }

    static BaseComponent[][] nameValue(String name, String... value) {
        return nameValue(ChatColor.GREEN, name, ChatColor.WHITE, value);
    }

    static BaseComponent[][] nameValue(ChatColor nameColor, String name, String... value) {
        return nameValue(nameColor, name, ChatColor.WHITE, value);
    }

    static BaseComponent[][] nameValue(String name, ChatColor valueColor, String... value) {
        return nameValue(ChatColor.GREEN, name, valueColor, value);
    }

    static BaseComponent[][] nameValue(ChatColor nameColor, String name, ChatColor valueColor, String... value) {

        int len = value.length;
        BaseComponent[][] components = new BaseComponent[len + 1][];
        components[0] = new ComponentBuilder(name).color(nameColor)
                .append(": ").color(ChatColor.WHITE)
                .create();
        for (int i = 0; i < len; i++) {
            components[i + 1] = TextComponent.fromLegacyText(value[i], valueColor);
        }

        return components;
    }

    static BaseComponent[][] nameValue(String name, List<String> value) {
        return nameValue(ChatColor.GREEN, name, ChatColor.WHITE, value);
    }

    static BaseComponent[][] nameValue(ChatColor nameColor, String name, List<String> value) {
        return nameValue(nameColor, name, ChatColor.WHITE, value);
    }

    static BaseComponent[][] nameValue(String name, ChatColor valueColor, List<String> value) {
        return nameValue(ChatColor.GREEN, name, valueColor, value);
    }

    static BaseComponent[][] nameValue(ChatColor nameColor, String name, ChatColor valueColor, List<String> value) {

        int len = value.size();
        BaseComponent[][] components = new BaseComponent[len + 1][];
        components[0] = new ComponentBuilder(name).color(nameColor)
                .append(": ").color(ChatColor.WHITE)
                .create();
        for (int i = 0; i < len; i++) {
            components[i + 1] = TextComponent.fromLegacyText(value.get(i), valueColor);
        }

        return components;
    }

    static BaseComponent[][] nameValue(String name, BaseComponent[][] value) {
        return nameValue(ChatColor.GREEN, name, ChatColor.WHITE, value);
    }

    static BaseComponent[][] nameValue(ChatColor nameColor, String name, BaseComponent[][] value) {
        return nameValue(nameColor, name, ChatColor.WHITE, value);
    }

    static BaseComponent[][] nameValue(String name, ChatColor valueColor, BaseComponent[][] value) {
        return nameValue(ChatColor.GREEN, name, valueColor, value);
    }

    static BaseComponent[][] nameValue(ChatColor nameColor, String name, ChatColor valueColor, BaseComponent[][] value) {

        int len = value.length;
        BaseComponent[][] components = new BaseComponent[len + 1][];
        components[0] = new ComponentBuilder(name).color(nameColor)
                .append(": ").color(ChatColor.WHITE)
                .create();
        for (int i = 0; i < len; i++) {

            for (BaseComponent component : value[i]) {

                if (component.getColorRaw() == null) {
                    component.setColor(valueColor);
                }
            }

            components[i + 1] = value[i];
        }

        return components;
    }

    static BaseComponent[] line(String line) {
        return line.isEmpty() ? blank() : new BaseComponent[]{ new TextComponent(line) };
    }

    static BaseComponent[] line(ChatColor color, String line) {

        if (line.isEmpty()) {
            return blank();
        }
        // Simple text component - leave it up to the user
        TextComponent component = new TextComponent(line);
        component.setColor(color);
        return new BaseComponent[]{ component };
    }

    static BaseComponent[] line(ChatColor format1, ChatColor format2, String line) {

        if (line.isEmpty()) {
            return blank();
        }
        // Simple text component - leave it up to the user
        TextComponent component = new TextComponent(line);
        setFormat(component, format1, format2);
        return new BaseComponent[]{ component };
    }

    static BaseComponent[] line(ChatColor format1, ChatColor format2, ChatColor format3, String line) {

        if (line.isEmpty()) {
            return blank();
        }
        // Simple text component - leave it up to the user
        TextComponent component = new TextComponent(line);
        setFormat(component, format1, format2, format3);
        return new BaseComponent[]{ component };
    }

    static BaseComponent[] line(ChatColor[] format, String line) {
        // Simple text component - leave it up to the user
        return line.isEmpty() ? blank() : new BaseComponent[]{ setFormat(new TextComponent(line), format) };
    }

    static BaseComponent[] line(ChatColor[] format1, String line1, ChatColor[] format2, String line2) {
        return line1.isEmpty() && line2.isEmpty() ? blank() : new BaseComponent[]{
                setFormat(new TextComponent(line1), format1),
                setFormat(new TextComponent(line2), format2)
        };
    }

    static BaseComponent[] line(ChatColor[] format1, String line1,
                                ChatColor[] format2, String line2,
                                ChatColor[] format3, String line3) {
        return line1.isEmpty() && line2.isEmpty() && line3.isEmpty() ? blank() : new BaseComponent[]{
                setFormat(new TextComponent(line1), format1),
                setFormat(new TextComponent(line2), format2),
                setFormat(new TextComponent(line3), format3)
        };
    }

    static BaseComponent[] line(ChatColor[] format1, String line1,
                                ChatColor[] format2, String line2,
                                ChatColor[] format3, String line3,
                                ChatColor[] format4, String line4) {
        return line1.isEmpty() && line2.isEmpty() && line3.isEmpty() ? blank() : new BaseComponent[]{
                setFormat(new TextComponent(line1), format1),
                setFormat(new TextComponent(line2), format2),
                setFormat(new TextComponent(line3), format3),
                setFormat(new TextComponent(line4), format4)
        };
    }

    static BaseComponent[][] lines(String... lines) {

        int len = lines.length;
        BaseComponent[][] components = new BaseComponent[len][];
        for (int i = 0; i < len; i++) {
            components[i] = line(lines[i]);
        }

        return components;
    }

    static BaseComponent[][] lines(ChatColor color, String... lines) {

        int len = lines.length;
        BaseComponent[][] components = new BaseComponent[len][];
        for (int i = 0; i < len; i++) {
            components[i] = line(color, lines[i]);
        }

        return components;
    }

    static BaseComponent[][] lines(ChatColor format1, ChatColor format2, String... lines) {

        int len = lines.length;
        BaseComponent[][] components = new BaseComponent[len][];
        for (int i = 0; i < len; i++) {
            components[i] = line(format1, format2, lines[i]);
        }

        return components;
    }

    static BaseComponent[][] lines(ChatColor format1, ChatColor format2, ChatColor format3, String... lines) {

        int len = lines.length;
        BaseComponent[][] components = new BaseComponent[len][];
        for (int i = 0; i < len; i++) {
            components[i] = line(format1, format2, format3, lines[i]);
        }

        return components;
    }

    // =====================================
    // ========== Start Generated ==========
    // =====================================

    static BaseComponent[] line(String line1,
                                String line2,
                                String line3,
                                String line4) {
        return line(
                EMPTY_COLOR_ARRAY, line1,
                EMPTY_COLOR_ARRAY, line2,
                EMPTY_COLOR_ARRAY, line3,
                EMPTY_COLOR_ARRAY, line4
        );
    }

    static BaseComponent[] line(ChatColor format11, String line1,
                                String line2,
                                String line3,
                                String line4) {
        return line(
                new ChatColor[]{ format11 }, line1,
                EMPTY_COLOR_ARRAY, line2,
                EMPTY_COLOR_ARRAY, line3,
                EMPTY_COLOR_ARRAY, line4
        );
    }

    static BaseComponent[] line(ChatColor format11, ChatColor format12, String line1,
                                String line2,
                                String line3,
                                String line4) {
        return line(
                new ChatColor[]{ format11, format12 }, line1,
                EMPTY_COLOR_ARRAY, line2,
                EMPTY_COLOR_ARRAY, line3,
                EMPTY_COLOR_ARRAY, line4
        );
    }

    static BaseComponent[] line(ChatColor format11, ChatColor format12, ChatColor format13, String line1,
                                String line2,
                                String line3,
                                String line4) {
        return line(
                new ChatColor[]{ format11, format12, format13 }, line1,
                EMPTY_COLOR_ARRAY, line2,
                EMPTY_COLOR_ARRAY, line3,
                EMPTY_COLOR_ARRAY, line4
        );
    }

    static BaseComponent[] line(String line1,
                                ChatColor format21, String line2,
                                String line3,
                                String line4) {
        return line(
                EMPTY_COLOR_ARRAY, line1,
                new ChatColor[]{ format21 }, line2,
                EMPTY_COLOR_ARRAY, line3,
                EMPTY_COLOR_ARRAY, line4
        );
    }

    static BaseComponent[] line(String line1,
                                ChatColor format21, ChatColor format22, String line2,
                                String line3,
                                String line4) {
        return line(
                EMPTY_COLOR_ARRAY, line1,
                new ChatColor[]{ format21, format22 }, line2,
                EMPTY_COLOR_ARRAY, line3,
                EMPTY_COLOR_ARRAY, line4
        );
    }

    static BaseComponent[] line(String line1,
                                ChatColor format21, ChatColor format22, ChatColor format23, String line2,
                                String line3,
                                String line4) {
        return line(
                EMPTY_COLOR_ARRAY, line1,
                new ChatColor[]{ format21, format22, format23 }, line2,
                EMPTY_COLOR_ARRAY, line3,
                EMPTY_COLOR_ARRAY, line4
        );
    }

    static BaseComponent[] line(String line1,
                                String line2,
                                ChatColor format31, String line3,
                                String line4) {
        return line(
                EMPTY_COLOR_ARRAY, line1,
                EMPTY_COLOR_ARRAY, line2,
                new ChatColor[]{ format31 }, line3,
                EMPTY_COLOR_ARRAY, line4
        );
    }

    static BaseComponent[] line(String line1,
                                String line2,
                                ChatColor format31, ChatColor format32, String line3,
                                String line4) {
        return line(
                EMPTY_COLOR_ARRAY, line1,
                EMPTY_COLOR_ARRAY, line2,
                new ChatColor[]{ format31, format32 }, line3,
                EMPTY_COLOR_ARRAY, line4
        );
    }

    static BaseComponent[] line(String line1,
                                String line2,
                                ChatColor format31, ChatColor format32, ChatColor format33, String line3,
                                String line4) {
        return line(
                EMPTY_COLOR_ARRAY, line1,
                EMPTY_COLOR_ARRAY, line2,
                new ChatColor[]{ format31, format32, format33 }, line3,
                EMPTY_COLOR_ARRAY, line4
        );
    }

    static BaseComponent[] line(String line1,
                                String line2,
                                String line3,
                                ChatColor format41, String line4) {
        return line(
                EMPTY_COLOR_ARRAY, line1,
                EMPTY_COLOR_ARRAY, line2,
                EMPTY_COLOR_ARRAY, line3,
                new ChatColor[]{ format41 }, line4
        );
    }

    static BaseComponent[] line(String line1,
                                String line2,
                                String line3,
                                ChatColor format41, ChatColor format42, String line4) {
        return line(
                EMPTY_COLOR_ARRAY, line1,
                EMPTY_COLOR_ARRAY, line2,
                EMPTY_COLOR_ARRAY, line3,
                new ChatColor[]{ format41, format42 }, line4
        );
    }

    static BaseComponent[] line(String line1,
                                String line2,
                                String line3,
                                ChatColor format41, ChatColor format42, ChatColor format43, String line4) {
        return line(
                EMPTY_COLOR_ARRAY, line1,
                EMPTY_COLOR_ARRAY, line2,
                EMPTY_COLOR_ARRAY, line3,
                new ChatColor[]{ format41, format42, format43 }, line4
        );
    }

    static BaseComponent[] line(String line1,
                                String line2,
                                String line3) {
        return line(
                EMPTY_COLOR_ARRAY, line1,
                EMPTY_COLOR_ARRAY, line2,
                EMPTY_COLOR_ARRAY, line3
        );
    }

    static BaseComponent[] line(ChatColor format11, String line1,
                                String line2,
                                String line3) {
        return line(
                new ChatColor[]{ format11 }, line1,
                EMPTY_COLOR_ARRAY, line2,
                EMPTY_COLOR_ARRAY, line3
        );
    }

    static BaseComponent[] line(ChatColor format11, ChatColor format12, String line1,
                                String line2,
                                String line3) {
        return line(
                new ChatColor[]{ format11, format12 }, line1,
                EMPTY_COLOR_ARRAY, line2,
                EMPTY_COLOR_ARRAY, line3
        );
    }

    static BaseComponent[] line(ChatColor format11, ChatColor format12, ChatColor format13, String line1,
                                String line2,
                                String line3) {
        return line(
                new ChatColor[]{ format11, format12, format13 }, line1,
                EMPTY_COLOR_ARRAY, line2,
                EMPTY_COLOR_ARRAY, line3
        );
    }

    static BaseComponent[] line(String line1,
                                ChatColor format21, String line2,
                                String line3) {
        return line(
                EMPTY_COLOR_ARRAY, line1,
                new ChatColor[]{ format21 }, line2,
                EMPTY_COLOR_ARRAY, line3
        );
    }

    static BaseComponent[] line(String line1,
                                ChatColor format21, ChatColor format22, String line2,
                                String line3) {
        return line(
                EMPTY_COLOR_ARRAY, line1,
                new ChatColor[]{ format21, format22 }, line2,
                EMPTY_COLOR_ARRAY, line3
        );
    }

    static BaseComponent[] line(String line1,
                                ChatColor format21, ChatColor format22, ChatColor format23, String line2,
                                String line3) {
        return line(
                EMPTY_COLOR_ARRAY, line1,
                new ChatColor[]{ format21, format22, format23 }, line2,
                EMPTY_COLOR_ARRAY, line3
        );
    }

    static BaseComponent[] line(String line1,
                                String line2,
                                ChatColor format31, String line3) {
        return line(
                EMPTY_COLOR_ARRAY, line1,
                EMPTY_COLOR_ARRAY, line2,
                new ChatColor[]{ format31 }, line3
        );
    }

    static BaseComponent[] line(String line1,
                                String line2,
                                ChatColor format31, ChatColor format32, String line3) {
        return line(
                EMPTY_COLOR_ARRAY, line1,
                EMPTY_COLOR_ARRAY, line2,
                new ChatColor[]{ format31, format32 }, line3
        );
    }

    static BaseComponent[] line(String line1,
                                String line2,
                                ChatColor format31, ChatColor format32, ChatColor format33, String line3) {
        return line(
                EMPTY_COLOR_ARRAY, line1,
                EMPTY_COLOR_ARRAY, line2,
                new ChatColor[]{ format31, format32, format33 }, line3
        );
    }

    static BaseComponent[] line(String line1,
                                String line2) {
        return line(
                EMPTY_COLOR_ARRAY, line1,
                EMPTY_COLOR_ARRAY, line2
        );
    }

    static BaseComponent[] line(ChatColor format11, String line1,
                                String line2) {
        return line(
                new ChatColor[]{ format11 }, line1,
                EMPTY_COLOR_ARRAY, line2
        );
    }

    static BaseComponent[] line(ChatColor format11, ChatColor format12, String line1,
                                String line2) {
        return line(
                new ChatColor[]{ format11, format12 }, line1,
                EMPTY_COLOR_ARRAY, line2
        );
    }

    static BaseComponent[] line(ChatColor format11, ChatColor format12, ChatColor format13, String line1,
                                String line2) {
        return line(
                new ChatColor[]{ format11, format12, format13 }, line1,
                EMPTY_COLOR_ARRAY, line2
        );
    }

    static BaseComponent[] line(String line1,
                                ChatColor format21, String line2) {
        return line(
                EMPTY_COLOR_ARRAY, line1,
                new ChatColor[]{ format21 }, line2
        );
    }

    static BaseComponent[] line(String line1,
                                ChatColor format21, ChatColor format22, String line2) {
        return line(
                EMPTY_COLOR_ARRAY, line1,
                new ChatColor[]{ format21, format22 }, line2
        );
    }

    static BaseComponent[] line(String line1,
                                ChatColor format21, ChatColor format22, ChatColor format23, String line2) {
        return line(
                EMPTY_COLOR_ARRAY, line1,
                new ChatColor[]{ format21, format22, format23 }, line2
        );
    }

    static BaseComponent[] line(ChatColor format11, String line1,
                                ChatColor format21, String line2,
                                ChatColor format31, String line3,
                                ChatColor format41, String line4) {
        return line(
                new ChatColor[]{ format11 }, line1,
                new ChatColor[]{ format21 }, line2,
                new ChatColor[]{ format31 }, line3,
                new ChatColor[]{ format41 }, line4
        );
    }

    static BaseComponent[] line(ChatColor format11, ChatColor format12, String line1,
                                ChatColor format21, String line2,
                                ChatColor format31, String line3,
                                ChatColor format41, String line4) {
        return line(
                new ChatColor[]{ format11, format12 }, line1,
                new ChatColor[]{ format21 }, line2,
                new ChatColor[]{ format31 }, line3,
                new ChatColor[]{ format41 }, line4
        );
    }

    static BaseComponent[] line(ChatColor format11, ChatColor format12, ChatColor format13, String line1,
                                ChatColor format21, String line2,
                                ChatColor format31, String line3,
                                ChatColor format41, String line4) {
        return line(
                new ChatColor[]{ format11, format12, format13 }, line1,
                new ChatColor[]{ format21 }, line2,
                new ChatColor[]{ format31 }, line3,
                new ChatColor[]{ format41 }, line4
        );
    }

    static BaseComponent[] line(ChatColor format11, String line1,
                                ChatColor format21, ChatColor format22, String line2,
                                ChatColor format31, String line3,
                                ChatColor format41, String line4) {
        return line(
                new ChatColor[]{ format11 }, line1,
                new ChatColor[]{ format21, format22 }, line2,
                new ChatColor[]{ format31 }, line3,
                new ChatColor[]{ format41 }, line4
        );
    }

    static BaseComponent[] line(ChatColor format11, String line1,
                                ChatColor format21, ChatColor format22, ChatColor format23, String line2,
                                ChatColor format31, String line3,
                                ChatColor format41, String line4) {
        return line(
                new ChatColor[]{ format11 }, line1,
                new ChatColor[]{ format21, format22, format23 }, line2,
                new ChatColor[]{ format31 }, line3,
                new ChatColor[]{ format41 }, line4
        );
    }

    static BaseComponent[] line(ChatColor format11, String line1,
                                ChatColor format21, String line2,
                                ChatColor format31, ChatColor format32, String line3,
                                ChatColor format41, String line4) {
        return line(
                new ChatColor[]{ format11 }, line1,
                new ChatColor[]{ format21 }, line2,
                new ChatColor[]{ format31, format32 }, line3,
                new ChatColor[]{ format41 }, line4
        );
    }

    static BaseComponent[] line(ChatColor format11, String line1,
                                ChatColor format21, String line2,
                                ChatColor format31, ChatColor format32, ChatColor format33, String line3,
                                ChatColor format41, String line4) {
        return line(
                new ChatColor[]{ format11 }, line1,
                new ChatColor[]{ format21 }, line2,
                new ChatColor[]{ format31, format32, format33 }, line3,
                new ChatColor[]{ format41 }, line4
        );
    }

    static BaseComponent[] line(ChatColor format11, String line1,
                                ChatColor format21, String line2,
                                ChatColor format31, String line3,
                                ChatColor format41, ChatColor format42, String line4) {
        return line(
                new ChatColor[]{ format11 }, line1,
                new ChatColor[]{ format21 }, line2,
                new ChatColor[]{ format31 }, line3,
                new ChatColor[]{ format41, format42 }, line4
        );
    }

    static BaseComponent[] line(ChatColor format11, String line1,
                                ChatColor format21, String line2,
                                ChatColor format31, String line3,
                                ChatColor format41, ChatColor format42, ChatColor format43, String line4) {
        return line(
                new ChatColor[]{ format11 }, line1,
                new ChatColor[]{ format21 }, line2,
                new ChatColor[]{ format31 }, line3,
                new ChatColor[]{ format41, format42, format43 }, line4
        );
    }

    static BaseComponent[] line(ChatColor format11, String line1,
                                ChatColor format21, String line2,
                                ChatColor format31, String line3) {
        return line(
                new ChatColor[]{ format11 }, line1,
                new ChatColor[]{ format21 }, line2,
                new ChatColor[]{ format31 }, line3
        );
    }

    static BaseComponent[] line(ChatColor format11, ChatColor format12, String line1,
                                ChatColor format21, String line2,
                                ChatColor format31, String line3) {
        return line(
                new ChatColor[]{ format11, format12 }, line1,
                new ChatColor[]{ format21 }, line2,
                new ChatColor[]{ format31 }, line3
        );
    }

    static BaseComponent[] line(ChatColor format11, ChatColor format12, ChatColor format13, String line1,
                                ChatColor format21, String line2,
                                ChatColor format31, String line3) {
        return line(
                new ChatColor[]{ format11, format12, format13 }, line1,
                new ChatColor[]{ format21 }, line2,
                new ChatColor[]{ format31 }, line3
        );
    }

    static BaseComponent[] line(ChatColor format11, String line1,
                                ChatColor format21, ChatColor format22, String line2,
                                ChatColor format31, String line3) {
        return line(
                new ChatColor[]{ format11 }, line1,
                new ChatColor[]{ format21, format22 }, line2,
                new ChatColor[]{ format31 }, line3
        );
    }

    static BaseComponent[] line(ChatColor format11, String line1,
                                ChatColor format21, ChatColor format22, ChatColor format23, String line2,
                                ChatColor format31, String line3) {
        return line(
                new ChatColor[]{ format11 }, line1,
                new ChatColor[]{ format21, format22, format23 }, line2,
                new ChatColor[]{ format31 }, line3
        );
    }

    static BaseComponent[] line(ChatColor format11, String line1,
                                ChatColor format21, String line2,
                                ChatColor format31, ChatColor format32, String line3) {
        return line(
                new ChatColor[]{ format11 }, line1,
                new ChatColor[]{ format21 }, line2,
                new ChatColor[]{ format31, format32 }, line3
        );
    }

    static BaseComponent[] line(ChatColor format11, String line1,
                                ChatColor format21, String line2,
                                ChatColor format31, ChatColor format32, ChatColor format33, String line3) {
        return line(
                new ChatColor[]{ format11 }, line1,
                new ChatColor[]{ format21 }, line2,
                new ChatColor[]{ format31, format32, format33 }, line3
        );
    }

    static BaseComponent[] line(ChatColor format11, String line1,
                                ChatColor format21, String line2) {
        return line(
                new ChatColor[]{ format11 }, line1,
                new ChatColor[]{ format21 }, line2
        );
    }

    static BaseComponent[] line(ChatColor format11, ChatColor format12, String line1,
                                ChatColor format21, String line2) {
        return line(
                new ChatColor[]{ format11, format12 }, line1,
                new ChatColor[]{ format21 }, line2
        );
    }

    static BaseComponent[] line(ChatColor format11, ChatColor format12, ChatColor format13, String line1,
                                ChatColor format21, String line2) {
        return line(
                new ChatColor[]{ format11, format12, format13 }, line1,
                new ChatColor[]{ format21 }, line2
        );
    }

    static BaseComponent[] line(ChatColor format11, String line1,
                                ChatColor format21, ChatColor format22, String line2) {
        return line(
                new ChatColor[]{ format11 }, line1,
                new ChatColor[]{ format21, format22 }, line2
        );
    }

    static BaseComponent[] line(ChatColor format11, String line1,
                                ChatColor format21, ChatColor format22, ChatColor format23, String line2) {
        return line(
                new ChatColor[]{ format11 }, line1,
                new ChatColor[]{ format21, format22, format23 }, line2
        );
    }

    static BaseComponent[] line(ChatColor format11, ChatColor format12, String line1,
                                ChatColor format21, ChatColor format22, String line2,
                                ChatColor format31, ChatColor format32, String line3,
                                ChatColor format41, ChatColor format42, String line4) {
        return line(
                new ChatColor[]{ format11, format12 }, line1,
                new ChatColor[]{ format21, format22 }, line2,
                new ChatColor[]{ format31, format32 }, line3,
                new ChatColor[]{ format41, format42 }, line4
        );
    }

    static BaseComponent[] line(ChatColor format11, ChatColor format12, ChatColor format13, String line1,
                                ChatColor format21, ChatColor format22, String line2,
                                ChatColor format31, ChatColor format32, String line3,
                                ChatColor format41, ChatColor format42, String line4) {
        return line(
                new ChatColor[]{ format11, format12, format13 }, line1,
                new ChatColor[]{ format21, format22 }, line2,
                new ChatColor[]{ format31, format32 }, line3,
                new ChatColor[]{ format41, format42 }, line4
        );
    }

    static BaseComponent[] line(ChatColor format11, ChatColor format12, String line1,
                                ChatColor format21, ChatColor format22, ChatColor format23, String line2,
                                ChatColor format31, ChatColor format32, String line3,
                                ChatColor format41, ChatColor format42, String line4) {
        return line(
                new ChatColor[]{ format11, format12 }, line1,
                new ChatColor[]{ format21, format22, format23 }, line2,
                new ChatColor[]{ format31, format32 }, line3,
                new ChatColor[]{ format41, format42 }, line4
        );
    }

    static BaseComponent[] line(ChatColor format11, ChatColor format12, String line1,
                                ChatColor format21, ChatColor format22, String line2,
                                ChatColor format31, ChatColor format32, ChatColor format33, String line3,
                                ChatColor format41, ChatColor format42, String line4) {
        return line(
                new ChatColor[]{ format11, format12 }, line1,
                new ChatColor[]{ format21, format22 }, line2,
                new ChatColor[]{ format31, format32, format33 }, line3,
                new ChatColor[]{ format41, format42 }, line4
        );
    }

    static BaseComponent[] line(ChatColor format11, ChatColor format12, String line1,
                                ChatColor format21, ChatColor format22, String line2,
                                ChatColor format31, ChatColor format32, String line3,
                                ChatColor format41, ChatColor format42, ChatColor format43, String line4) {
        return line(
                new ChatColor[]{ format11, format12 }, line1,
                new ChatColor[]{ format21, format22 }, line2,
                new ChatColor[]{ format31, format32 }, line3,
                new ChatColor[]{ format41, format42, format43 }, line4
        );
    }

    static BaseComponent[] line(ChatColor format11, ChatColor format12, String line1,
                                ChatColor format21, ChatColor format22, String line2,
                                ChatColor format31, ChatColor format32, String line3) {
        return line(
                new ChatColor[]{ format11, format12 }, line1,
                new ChatColor[]{ format21, format22 }, line2,
                new ChatColor[]{ format31, format32 }, line3
        );
    }

    static BaseComponent[] line(ChatColor format11, ChatColor format12, ChatColor format13, String line1,
                                ChatColor format21, ChatColor format22, String line2,
                                ChatColor format31, ChatColor format32, String line3) {
        return line(
                new ChatColor[]{ format11, format12, format13 }, line1,
                new ChatColor[]{ format21, format22 }, line2,
                new ChatColor[]{ format31, format32 }, line3
        );
    }

    static BaseComponent[] line(ChatColor format11, ChatColor format12, String line1,
                                ChatColor format21, ChatColor format22, ChatColor format23, String line2,
                                ChatColor format31, ChatColor format32, String line3) {
        return line(
                new ChatColor[]{ format11, format12 }, line1,
                new ChatColor[]{ format21, format22, format23 }, line2,
                new ChatColor[]{ format31, format32 }, line3
        );
    }

    static BaseComponent[] line(ChatColor format11, ChatColor format12, String line1,
                                ChatColor format21, ChatColor format22, String line2,
                                ChatColor format31, ChatColor format32, ChatColor format33, String line3) {
        return line(
                new ChatColor[]{ format11, format12 }, line1,
                new ChatColor[]{ format21, format22 }, line2,
                new ChatColor[]{ format31, format32, format33 }, line3
        );
    }

    static BaseComponent[] line(ChatColor format11, ChatColor format12, String line1,
                                ChatColor format21, ChatColor format22, String line2) {
        return line(
                new ChatColor[]{ format11, format12 }, line1,
                new ChatColor[]{ format21, format22 }, line2
        );
    }

    static BaseComponent[] line(ChatColor format11, ChatColor format12, ChatColor format13, String line1,
                                ChatColor format21, ChatColor format22, String line2) {
        return line(
                new ChatColor[]{ format11, format12, format13 }, line1,
                new ChatColor[]{ format21, format22 }, line2
        );
    }

    static BaseComponent[] line(ChatColor format11, ChatColor format12, String line1,
                                ChatColor format21, ChatColor format22, ChatColor format23, String line2) {
        return line(
                new ChatColor[]{ format11, format12 }, line1,
                new ChatColor[]{ format21, format22, format23 }, line2
        );
    }

    static BaseComponent[] line(ChatColor format11, ChatColor format12, ChatColor format13, String line1,
                                ChatColor format21, ChatColor format22, ChatColor format23, String line2,
                                ChatColor format31, ChatColor format32, ChatColor format33, String line3,
                                ChatColor format41, ChatColor format42, ChatColor format43, String line4) {
        return line(
                new ChatColor[]{ format11, format12, format13 }, line1,
                new ChatColor[]{ format21, format22, format23 }, line2,
                new ChatColor[]{ format31, format32, format33 }, line3,
                new ChatColor[]{ format41, format42, format43 }, line4
        );
    }

    static BaseComponent[] line(ChatColor format11, ChatColor format12, ChatColor format13, String line1,
                                ChatColor format21, ChatColor format22, ChatColor format23, String line2,
                                ChatColor format31, ChatColor format32, ChatColor format33, String line3) {
        return line(
                new ChatColor[]{ format11, format12, format13 }, line1,
                new ChatColor[]{ format21, format22, format23 }, line2,
                new ChatColor[]{ format31, format32, format33 }, line3
        );
    }

    static BaseComponent[] line(ChatColor format11, ChatColor format12, ChatColor format13, String line1,
                                ChatColor format21, ChatColor format22, ChatColor format23, String line2) {
        return line(
                new ChatColor[]{ format11, format12, format13 }, line1,
                new ChatColor[]{ format21, format22, format23 }, line2
        );
    }

    private static BaseComponent setFormat(BaseComponent component, ChatColor... formats) {

        for (ChatColor format : formats) {

            if (format.getColor() != null) {
                component.setColor(format);
            } else if (format == ChatColor.BOLD) {
                component.setBold(true);
            } else if (format == ChatColor.ITALIC) {
                component.setItalic(true);
            } else if (format == ChatColor.UNDERLINE) {
                component.setUnderlined(true);
            } else if (format == ChatColor.STRIKETHROUGH) {
                component.setStrikethrough(true);
            } else if (format == ChatColor.MAGIC) {
                component.setObfuscated(true);
            }
        }

        return component;
    }
}
