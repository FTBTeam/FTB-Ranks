package com.feed_the_beast.mods.ftbranks.impl;

import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * @author LatvianModder
 */
public class TextComponentParser
{
	public static final Char2ObjectOpenHashMap<TextFormatting> CODE_TO_FORMATTING = new Char2ObjectOpenHashMap<>();

	static
	{
		CODE_TO_FORMATTING.put('0', TextFormatting.BLACK);
		CODE_TO_FORMATTING.put('1', TextFormatting.DARK_BLUE);
		CODE_TO_FORMATTING.put('2', TextFormatting.DARK_GREEN);
		CODE_TO_FORMATTING.put('3', TextFormatting.DARK_AQUA);
		CODE_TO_FORMATTING.put('4', TextFormatting.DARK_RED);
		CODE_TO_FORMATTING.put('5', TextFormatting.DARK_PURPLE);
		CODE_TO_FORMATTING.put('6', TextFormatting.GOLD);
		CODE_TO_FORMATTING.put('7', TextFormatting.GRAY);
		CODE_TO_FORMATTING.put('8', TextFormatting.DARK_GRAY);
		CODE_TO_FORMATTING.put('9', TextFormatting.BLUE);
		CODE_TO_FORMATTING.put('a', TextFormatting.GREEN);
		CODE_TO_FORMATTING.put('b', TextFormatting.AQUA);
		CODE_TO_FORMATTING.put('c', TextFormatting.RED);
		CODE_TO_FORMATTING.put('d', TextFormatting.LIGHT_PURPLE);
		CODE_TO_FORMATTING.put('e', TextFormatting.YELLOW);
		CODE_TO_FORMATTING.put('f', TextFormatting.WHITE);
		CODE_TO_FORMATTING.put('k', TextFormatting.OBFUSCATED);
		CODE_TO_FORMATTING.put('l', TextFormatting.BOLD);
		CODE_TO_FORMATTING.put('m', TextFormatting.STRIKETHROUGH);
		CODE_TO_FORMATTING.put('n', TextFormatting.UNDERLINE);
		CODE_TO_FORMATTING.put('o', TextFormatting.ITALIC);
		CODE_TO_FORMATTING.put('r', TextFormatting.RESET);
	}

	public static StringTextComponent parse(String text, @Nullable Function<String, IFormattableTextComponent> substitutes)
	{
		return new TextComponentParser(text, substitutes).parse();
	}

	private final String text;
	private final Function<String, IFormattableTextComponent> substitutes;

	private StringTextComponent component;
	private StringBuilder builder;
	private Style style;

	private TextComponentParser(String txt, @Nullable Function<String, IFormattableTextComponent> sub)
	{
		text = txt;
		substitutes = sub;
	}

	private StringTextComponent parse()
	{
		if (text.isEmpty())
		{
			return new StringTextComponent("");
		}

		char[] c = text.toCharArray();
		boolean hasSpecialCodes = false;

		for (char c1 : c)
		{
			if (c1 == '{' || c1 == '&' || c1 == '\u00a7')
			{
				hasSpecialCodes = true;
				break;
			}
		}

		if (!hasSpecialCodes)
		{
			return new StringTextComponent(text);
		}

		component = new StringTextComponent("");
		style = Style.EMPTY;
		builder = new StringBuilder();
		boolean sub = false;

		for (int i = 0; i < c.length; i++)
		{
			boolean escape = i > 0 && c[i - 1] == '\\';
			boolean end = i == c.length - 1;

			if (sub && (end || c[i] == '{' || c[i] == '}'))
			{
				if (c[i] == '{')
				{
					throw new IllegalArgumentException("Invalid formatting! Can't nest multiple substitutes!");
				}

				finishPart();
				sub = false;
				continue;
			}

			if (!escape)
			{
				if (c[i] == '&')
				{
					c[i] = '\u00a7';
				}

				if (c[i] == '\u00a7')
				{
					finishPart();

					if (end)
					{
						throw new IllegalArgumentException("Invalid formatting! Can't end string with & or \u00a7!");
					}

					i++;

					TextFormatting formatting = CODE_TO_FORMATTING.get(c[i]);

					if (formatting == null)
					{
						throw new IllegalArgumentException("Illegal formatting! Unknown color code character: " + c[i] + "!");
					}

					switch (formatting)
					{
						case OBFUSCATED:
							style = style.setObfuscated(!style.getObfuscated());
							break;
						case BOLD:
							style = style.setBold(!style.getBold());
							break;
						case STRIKETHROUGH:
							style = style.setStrikethrough(!style.getStrikethrough());
							break;
						case UNDERLINE:
							style = style.setUnderlined(!style.getUnderlined());
							break;
						case ITALIC:
							style = style.setItalic(!style.getItalic());
							break;
						case RESET:
							style = Style.EMPTY;
							break;
						default:
							style = style.setColor(Color.func_240744_a_(formatting));
					}

					continue;
				}
				else if (c[i] == '{')
				{
					finishPart();

					if (end)
					{
						throw new IllegalArgumentException("Invalid formatting! Can't end string with {!");
					}

					sub = true;
				}
			}

			if (c[i] != '\\' || escape)
			{
				builder.append(c[i]);
			}
		}

		finishPart();
		return component;
	}

	private void finishPart()
	{
		String string = builder.toString();
		builder.setLength(0);

		if (string.isEmpty())
		{
			return;
		}
		else if (string.length() < 2 || string.charAt(0) != '{')
		{
			StringTextComponent component1 = new StringTextComponent(string);
			component1.setStyle(style);
			component.append(component1);
			return;
		}

		IFormattableTextComponent component1 = substitutes.apply(string.substring(1));

		if (component1 != null)
		{
			Style style0 = component1.getStyle();
			Style style1 = style;
			style1.setHoverEvent(style0.getHoverEvent());
			style1.setClickEvent(style0.getClickEvent());
			style1.setInsertion(style0.getInsertion());
			component1.setStyle(style1);
		}
		else
		{
			throw new IllegalArgumentException("Invalid formatting! Unknown substitute: " + string.substring(1));
		}

		component.append(component1);
	}
}