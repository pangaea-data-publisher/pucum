package de.pangaea.ucum.v1;

import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

public class RegularExParser {
	private final HashMap<String, String> pangUcumMappings;
	private final Pattern pattern;
	private final Pattern pattern1 = Pattern.compile("([a-zA-Z]+)(\\*)+(\\d)");
	private final Pattern pattern2 = Pattern.compile("(\\d+)+(\\*){2}(-?[1-9]\\d*|0)(\\s)?([a-zA-Z]+)");
	private final Pattern pattern3 = Pattern.compile("([a-zA-Z]+)(\\*){1}([a-zA-Z]+)");
	private final Pattern pattern4 = Pattern.compile("(\\d+)+(\\s)?([a-zA-Z]+)"); // 100 km -> 100.km

	public RegularExParser() {
		pangUcumMappings = PanUcumApp.getPangUcumMapping();
		this.pattern = Pattern.compile(pangUcumMappings.keySet().stream()
				.sorted(Comparator.comparingInt(String::length).reversed().thenComparing(Comparator.naturalOrder()))
				.map(Pattern::quote).collect(Collectors.joining("|", "(", ")")));
		// System.out.println(this.pattern.toString());
	}

	protected String runRegExpression(String units) {
		// String patternString = "(" + StringUtils.join(pangUcumMappings.keySet(), "|")
		// + ")";
		// Pattern pattern = Pattern.compile(patternString);
		
		Matcher matcher = this.pattern.matcher(units);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			// Avoids throwing a NullPointerException in the case that you
			// Don't have a replacement defined in the map for the match
			String repString = pangUcumMappings.get(matcher.group(1));
			if (repString != null)
				matcher.appendReplacement(sb, repString);
		}
		String formattedStr = matcher.appendTail(sb).toString();
		
		String t1 = replaceWithPattern(pattern1, formattedStr, "$1$3");
		String t2 = replaceWithPattern(pattern2, t1, "$1^$3.$5");
		String t3 = replaceWithPattern(pattern3, t2, "$1.$3");
		String t4 = replaceWithPattern(pattern4, t3, "$1.$3");
		return t4;
	}

	private String replaceWithPattern(Pattern pattern, String str, String replace) {
		// Pattern ptn = Pattern.compile(pattern);
		Matcher mtch = pattern.matcher(str);
		return mtch.replaceAll(replace);
	}

}
