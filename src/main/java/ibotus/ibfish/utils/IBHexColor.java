package ibotus.ibfish.utils;

import java.util.regex.Pattern;

import net.md_5.bungee.api.ChatColor;

public class IBHexColor {

    public static String color(String input) {
        if (input == null) {
            return null;
        }
        String text = Pattern.compile("#[a-fA-F0-9]{6}")
                .matcher(input)
                .replaceAll(match -> ChatColor.of(match.group()).toString());
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}

