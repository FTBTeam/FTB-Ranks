package dev.ftb.mods.ftbranks.impl.decorate;

import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class TextComponentParser {
	public static final Char2ObjectOpenHashMap<ChatFormatting> CODE_TO_FORMATTING = new Char2ObjectOpenHashMap<>();

	static {
		CODE_TO_FORMATTING.put('0', ChatFormatting.BLACK);
		CODE_TO_FORMATTING.put('1', ChatFormatting.DARK_BLUE);
		CODE_TO_FORMATTING.put('2', ChatFormatting.DARK_GREEN);
		CODE_TO_FORMATTING.put('3', ChatFormatting.DARK_AQUA);
		CODE_TO_FORMATTING.put('4', ChatFormatting.DARK_RED);
		CODE_TO_FORMATTING.put('5', ChatFormatting.DARK_PURPLE);
		CODE_TO_FORMATTING.put('6', ChatFormatting.GOLD);
		CODE_TO_FORMATTING.put('7', ChatFormatting.GRAY);
		CODE_TO_FORMATTING.put('8', ChatFormatting.DARK_GRAY);
		CODE_TO_FORMATTING.put('9', ChatFormatting.BLUE);
		CODE_TO_FORMATTING.put('a', ChatFormatting.GREEN);
		CODE_TO_FORMATTING.put('b', ChatFormatting.AQUA);
		CODE_TO_FORMATTING.put('c', ChatFormatting.RED);
		CODE_TO_FORMATTING.put('d', ChatFormatting.LIGHT_PURPLE);
		CODE_TO_FORMATTING.put('e', ChatFormatting.YELLOW);
		CODE_TO_FORMATTING.put('f', ChatFormatting.WHITE);
		CODE_TO_FORMATTING.put('k', ChatFormatting.OBFUSCATED);
		CODE_TO_FORMATTING.put('l', ChatFormatting.BOLD);
		CODE_TO_FORMATTING.put('m', ChatFormatting.STRIKETHROUGH);
		CODE_TO_FORMATTING.put('n', ChatFormatting.UNDERLINE);
		CODE_TO_FORMATTING.put('o', ChatFormatting.ITALIC);
		CODE_TO_FORMATTING.put('r', ChatFormatting.RESET);
	}

	private final String text;
	private final Function<String, @Nullable Component> substitutes;

	private TextComponentParser(String text, Function<String, @Nullable Component> substitutes) {
		this.text = text;
		this.substitutes = substitutes;
	}

	public static MutableComponent parse(String text, Function<String, @Nullable Component> substitutes) {
		return new TextComponentParser(text, substitutes).parse();
	}

	private MutableComponent parse() {
		if (text.isEmpty()) {
			return Component.empty();
		}

		char[] c = text.toCharArray();
		boolean hasSpecialCodes = false;

		for (char c1 : c) {
			if (c1 == '{' || c1 == '&' || c1 == '§') {
				hasSpecialCodes = true;
				break;
			}
		}

		if (!hasSpecialCodes) {
			return Component.literal(text);
		}

		MutableComponent component = Component.literal("");
		Style style = Style.EMPTY;
		StringBuilder builder = new StringBuilder();
		boolean sub = false;

		for (int i = 0; i < c.length; i++) {
			boolean escape = i > 0 && c[i - 1] == '\\';
			boolean end = i == c.length - 1;

			if (sub && (end || c[i] == '{' || c[i] == '}')) {
				if (c[i] == '{') {
					throw new IllegalArgumentException("Invalid formatting! Can't nest multiple substitutes!");
				}

				finishPart(component, style, builder);
				sub = false;
				continue;
			}

			if (!escape) {
				if (c[i] == '§') {
					c[i] = '&';
				}

				if (c[i] == '&') {
					finishPart(component, style, builder);

					if (end) {
						throw new IllegalArgumentException("Invalid formatting! Can't end string with & or §!");
					}

					i++;

					if (c[i] == '#') {
						char[] rrggbb = new char[7];
						rrggbb[0] = '#';
						System.arraycopy(c, i + 1, rrggbb, 1, 6);
						i += 6;
						style = style.withColor(TextColor.parseColor(new String(rrggbb)).result().orElse(TextColor.fromRgb(0xFFFFFF)));
					} else {
						ChatFormatting formatting = CODE_TO_FORMATTING.get(c[i]);
                        style = style.applyFormat(formatting);
					}

					continue;
				} else if (c[i] == '{') {
					finishPart(component, style, builder);

					if (end) {
						throw new IllegalArgumentException("Invalid formatting! Can't end string with {!");
					}

					sub = true;
				}
			}

			if (c[i] != '\\' || escape) {
				builder.append(c[i]);
			}
		}

		finishPart(component, style, builder);
		return component;
	}

	private void finishPart(MutableComponent component, Style style, StringBuilder builder) {
		String string = builder.toString();
		builder.setLength(0);

		if (string.isEmpty()) {
			return;
		} else if (string.length() < 2 || string.charAt(0) != '{') {
			component.append(Component.literal(string).setStyle(style));
			return;
		}

		Component component1 = substitutes.apply(string.substring(1));
		if (component1 != null) {
			Style style0 = component1.getStyle();
            style.withHoverEvent(style0.getHoverEvent())
					.withClickEvent(style0.getClickEvent())
					.withInsertion(style0.getInsertion());
			component1 = Component.empty().append(component1).withStyle(style);
		} else {
			throw new IllegalArgumentException("Invalid formatting! Unknown substitute: " + string.substring(1));
		}

		component.append(component1);
	}
}
