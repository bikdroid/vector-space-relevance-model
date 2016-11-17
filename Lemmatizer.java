package homework2;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class Lemmatizer {
	static File stopwordsfile = new File("stopwords");
	static Set<String> stopWords = new HashSet<String>();
	static Lemmatizer m_lemmatizer;
	Properties props;
	StanfordCoreNLP pipeline;
	private Map<String, Integer> dictionary;

	private Lemmatizer() {
		props = new Properties();
		props.put("annotators", "tokenize,ssplit, pos,  lemma");
		pipeline = new StanfordCoreNLP(props, false);
	}

	public static Lemmatizer getInstance() {
		if (m_lemmatizer == null) {
			m_lemmatizer = new Lemmatizer();
		}
		return m_lemmatizer;

	}

	public String getLemma(String text) {
		String lemma = "";
		Annotation document = pipeline.process(text);
		for (CoreMap sentence : document.get(SentencesAnnotation.class)) {
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				lemma += token.get(LemmaAnnotation.class) + " ";
			}
		}
		return lemma;
	}

	public Map<String, Integer> buildLemmaDictionary(File file) {
		try {
			dictionary = new HashMap<String, Integer>();
			FileInputStream finstream = null;
			DataInputStream dinstream = null;
			BufferedReader br = null;
			if (file.isFile()) {
				String strCurrent = "";
				finstream = new FileInputStream(file);
				dinstream = new DataInputStream(finstream);
				br = new BufferedReader(new InputStreamReader(dinstream));
				String strline = null;
				boolean flag = false;
				while ((strline = br.readLine()) != null) {
					StringTokenizer st = new StringTokenizer(strline, " ");

					// System.out.println(line);
					while (st.hasMoreTokens()) {

						if (flag) {

							strCurrent = st.nextToken();
							if (strCurrent.indexOf("</TEXT>") >= 0) {
								flag = false;
								break;
							}

							// remove xml tags and change to lower case
							strCurrent = strCurrent.replaceAll("<[^>]+>", "")
									.toLowerCase();
							// remove white spaces
							strCurrent = strCurrent.replaceAll("[0-9]", "");
							strCurrent = strCurrent.replaceAll(
									"[^\\w\\s-'.!:;]+", "");
							// not to include if it is stop word
							if (!stopWords.contains(strCurrent)) {
								strCurrent = strCurrent.replaceAll("['.`]+", "");
								tokenizeAndaddToLemmaDic(strCurrent, dictionary);
							}
							continue;
						}
						if (st.nextToken().indexOf("<TEXT>") >= 0) {
							flag = true;
							break;
						}

					}
				}
			}
		} catch (Exception e) {
			System.out.print(e);
		}
		return dictionary;
	}

	public static void tokenizeAndaddToLemmaDic(String tokenstring,
			Map<String, Integer> dicname) {

		if (tokenstring.trim().length() > 0) {

			// handle special cases
			if (tokenstring.endsWith("'s")) {
				tokenstring = tokenstring.replace("'s", "").trim();
				addToLemmaDic(tokenstring, dicname);
			} else if (tokenstring.contains("-")) {
				String[] newTokens = tokenstring.split("-");
				for (String newToken : newTokens) {
					addToLemmaDic(newToken, dicname);
				}
			} else if (tokenstring.contains("_")) {
				String[] newTokens = tokenstring.split("_");
				for (String newToken : newTokens) {
					addToLemmaDic(newToken, dicname);
				}
			} else {
				// default case
				addToLemmaDic(tokenstring, dicname);
			}
		}

	}

	public static void addToLemmaDic(String strToken, Map<String, Integer> dicname) {
		String lemma = Lemmatizer.getInstance()
				.getLemma(strToken);

		if (lemma.trim().length() > 0) {
			lemma=lemma.replace(" ", "").toLowerCase();
			if(!stopWords.contains(lemma)){
				// check if lemma already exits
				if (dicname.containsKey(lemma)) {
					dicname.put(lemma, dicname.get(lemma) + 1);
				} else
					// if does not exists enter new row
					dicname.put(lemma, 1);
			}
		}

	}

	public static void main(String[] args) {
		System.out.println(Lemmatizer.getInstance().getLemma("insured"));
	}
}