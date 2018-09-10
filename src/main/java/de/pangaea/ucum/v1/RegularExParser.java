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
	// private final Pattern pattern2 =
	// Pattern.compile("(\\d+)+(\\*){2}(-?[1-9]\\d*|0)(\\s)?([a-zA-Z]+)");
	private final Pattern pattern2 = Pattern.compile("(\\d+)+(\\*){2}(-?[1-9]\\d*|0)(\\s)(\\[*[a-zA-Z]+)"); //10**6 ru/g ->10^6.[arb'U]/g , 10**9 atoms/g - >10^9.n{atom}/g
	private final Pattern pattern3 = Pattern.compile("([a-zA-Z]+)(\\*){1}([a-zA-Z]+)");
	private final Pattern pattern4 = Pattern.compile("(\\d+)+(\\s)+([a-zA-Z]+)"); // 100 km -> 100.km
	private final Pattern pattern5 = Pattern.compile("(\\d+)+(\\*){2}(-?[1-9]\\d*|0)") ;//10**2 -> 10*2

	public static String[] stopWords = { "about", "above", "above", "across", "after", "afterwards", "again", "against",
			"all", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst",
			"amoungst", "amount", "an", "and", "another", "any", "anyhow", "anyone", "anything", "anyway", "anywhere",
			"are", "around", "as", "at", "back", "be", "became", "because", "become", "becomes", "becoming", "been",
			"before", "beforehand", "behind", "being", "below", "beside", "besides", "between", "beyond", "bill",
			"both", "but", "by", "call", "can", "cannot", "cant", "co", "con", "could", "couldnt", "cry", "de",
			"describe", "detail", "do", "done", "down", "due", "during", "each", "eg", "eight", "either", "eleven",
			"else", "elsewhere", "empty", "enough", "etc", "even", "ever", "every", "everyone", "everything",
			"everywhere", "except", "few", "fifteen", "fify", "fill", "find", "fire", "first", "five", "for", "former",
			"formerly", "forty", "found", "four", "from", "front", "full", "further", "get", "give", "go", "had", "has",
			"hasnt", "have", "he", "hence", "her", "here", "hereafter", "hereby", "herein", "hereupon", "hers",
			"herself", "him", "himself", "his", "how", "however", "hundred", "ie", "if", "in", "inc", "indeed",
			"interest", "into", "is", "it", "its", "itself", "keep", "last", "latter", "latterly", "least", "less",
			"ltd", "made", "many", "may", "me", "meanwhile", "might", "mill", "mine", "more", "moreover", "most",
			"mostly", "move", "much", "must", "my", "myself", "namely", "neither", "never", "nevertheless", "next",
			"nine", "no", "nobody", "none", "noone", "nor", "not", "nothing", "now", "nowhere", "of", "off", "often",
			"on", "once", "only", "onto", "or", "other", "others", "otherwise", "our", "ours", "ourselves", "out",
			"over", "own", "part", "per\\sunit", "per", "perhaps", "please", "put", "rather", "re", "same", "see",
			"seem", "seemed", "seeming", "seems", "serious", "several", "she", "should", "show", "side", "since",
			"sincere", "six", "sixty", "so", "some", "somehow", "someone", "something", "sometime", "sometimes",
			"somewhere", "still", "such", "take", "ten", "than", "that", "the", "their", "them", "themselves", "then",
			"thence", "there", "thereafter", "thereby", "therefore", "therein", "thereupon", "these", "they", "thickv",
			"thin", "third", "this", "those", "though", "three", "through", "throughout", "thru", "thus", "to",
			"together", "too", "toward", "towards", "twelve", "twenty", "two", "un", "under", "until", "up", "upon",
			"us", "very", "via", "was", "we", "were", "what", "whatever", "when", "whence", "whenever", "where",
			"whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while",
			"whither", "who", "whoever", "whole", "whom", "whose", "why", "will", "with", "within", "without", "would",
			"yet", "you", "your", "yours", "yourself", "yourselves", "the",
			// pangaea specifics
			"at", "downward", "upward", "plus", "var.", "spp.", "sp.", "gen.", "cf.", "forma" };

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
		
		//remove stopwords
		for (int i=0; i<stopWords.length;i++) {
			units = units.replaceAll("\\s+"+stopWords[i]+"\\s+", " ").trim();
		}

		Matcher matcher = this.pattern.matcher(units);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			// Avoids throwing a NullPointerException in the case that you
			// Don't have a replacement defined in the map for the match
			String repString = pangUcumMappings.get(matcher.group(1));
			//System.out.println(repString+" "+matcher.group(1));
			if (repString != null)
				matcher.appendReplacement(sb, repString);
		}
		String formattedStr = matcher.appendTail(sb).toString();
		String t1 = replaceWithPattern(pattern1, formattedStr, "$1$3");
		String t2 = replaceWithPattern(pattern2, t1, "$1*$3.$5");
		String t3 = replaceWithPattern(pattern3, t2, "$1.$3");
		String t4 = replaceWithPattern(pattern4, t3, "$1.$3");
		String t5 = replaceWithPattern(pattern5, t4, "$1*$3");
		//t5 = t5.replace(" ", ""); // ug {C}/l/d -> ug{C}/l/d
		t5 = t5.replaceAll("[ ,]" , "");
		return t5;
	}

	private String replaceWithPattern(Pattern pattern, String str, String replace) {
		// Pattern ptn = Pattern.compile(pattern);
		Matcher mtch = pattern.matcher(str);
		return mtch.replaceAll(replace);
	}

}
