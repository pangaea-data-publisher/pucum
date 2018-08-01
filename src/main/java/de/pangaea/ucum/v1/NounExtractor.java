package de.pangaea.ucum.v1;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.BadRequestException;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;

public class NounExtractor {

	static Set<String> nounPhrases = new HashSet<>();
	static InputStream modelIn = null;
	static POSModel POSModel = null;
	static File file = null;

	public NounExtractor(String sentence) {
		String cleanStn = extractOriginal(sentence);
		try{
			String fileName = "en-pos-maxent.bin";
			ClassLoader classLoader = getClass().getClassLoader();
			if (file == null) {
				file = new File(classLoader.getResource(fileName).getFile());
			    modelIn = new FileInputStream(file);
			    POSModel = new POSModel(modelIn);
			}

		    POSTaggerME tagger = new POSTaggerME(POSModel);
		    @SuppressWarnings("deprecation")
			SimpleTokenizer tokenizer= new SimpleTokenizer();
		    String tokens[] = tokenizer.tokenize(cleanStn);
		    String[] tagged = tagger.tag(tokens);
		    for (int i = 0; i < tagged.length; i++){
		        if (tagged[i].equalsIgnoreCase("nn")){
		            System.out.println(tokens[i]);
		            nounPhrases.add(tokens[i]);
		        }
		    }
		}
		catch(IOException e){
		    throw new BadRequestException(e.getMessage());
		}

	}

	/*// recursively loop through tree, extracting noun phrases
	public static void getNounPhrases(Parse p) {
		if (p.getType().equals("NP")) { // NP=noun phrase
			nounPhrases.add(p.getCoveredText());
		}
		for (Parse child : p.getChildren())
			getNounPhrases(child);
	}*/

	// remove non-alphanumeric characters
	private String extractOriginal(String value) {
		return value.replaceAll("[^A-Za-z0-9 ]", " ");
	}

	public static Set<String> getNounPhrases() {
		return nounPhrases;
	}

	public static void setNounPhrases(Set<String> nounPhrases) {
		NounExtractor.nounPhrases = nounPhrases;
	}
}
