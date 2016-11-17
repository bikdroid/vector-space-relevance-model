package homework2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import homework2.Compression.DictionaryDetailBytes;
import homework2.Compression.PostingEntry;

import java.util.TreeMap;

public class Indexer {

	static TreeMap<String, DictionaryDetail> stemIndexUncompressed = new TreeMap<String, DictionaryDetail>();
	static TreeMap<String, DictionaryDetailBytes> stemIndexCompressed = new TreeMap<String, DictionaryDetailBytes>();
	static TreeMap<String, DictionaryDetail> lemmaIndexUncompressed = new TreeMap<String, DictionaryDetail>();
	static TreeMap<String, DictionaryDetailBytes> lemmaIndexCompressed = new TreeMap<String, DictionaryDetailBytes>();
	HashMap<String, Integer> termCountMap = new HashMap<String, Integer>();
	HashMap<String, Integer> stemCountMap = new HashMap<String, Integer>();
	HashMap<String, Integer> t = new HashMap<String, Integer>();
	static TreeMap<String, DocumentDetail> docTermDetailMap = new TreeMap<String, DocumentDetail>();
	static TreeMap<String, DocumentDetail> docStemDetailMap = new TreeMap<String, DocumentDetail>();
	static String index1maxdf = "";
	static String index1mindf = "";
	static String index2maxdf = "";
	static String index2mindf = "";

	public Indexer() {

	}

	public Indexer(Map<String, HashMap<String, Integer>> tokenmap, HashMap<String, HashMap<String, Integer>> stemMap) {
		Iterator it = tokenmap.entrySet().iterator();
		termCountMap = new HashMap<String, Integer>();

		while (it.hasNext()) {

			Entry entry = (Entry) it.next();
			DictionaryDetail dicDetail = null;
			DocumentDetail docTermDetail = new DocumentDetail();

			t = (HashMap<String, Integer>) entry.getValue();
			Iterator it1 = t.entrySet().iterator();

			if (entry.getKey().toString().equals("")) {
				continue;
			}
			TreeMap<String, Integer> temp_tf = new TreeMap<String, Integer>();
			int sum = 0;
			while (it1.hasNext()) {

				Entry entry1 = (Entry) it1.next();
				String lemma = Lemmatizer.getInstance().getLemma(entry1.getKey().toString());
				Integer count = Integer.parseInt(entry1.getValue().toString());
				sum += count;
				termCountMap.put(lemma, count);
				if (lemmaIndexUncompressed.containsKey(lemma)) {
					dicDetail = lemmaIndexUncompressed.get(lemma);
					dicDetail.setTerm(lemma);
					dicDetail.updatePostingList(entry.getKey().toString(),
							Integer.parseInt(entry1.getValue().toString()));
					lemmaIndexUncompressed.put(lemma, dicDetail);
				} else {
					dicDetail = new DictionaryDetail();
					dicDetail.setTerm(lemma);
					dicDetail.updatePostingList(entry.getKey().toString(),
							Integer.parseInt(entry1.getValue().toString()));
					lemmaIndexUncompressed.put(lemma, dicDetail);
				}

				temp_tf.put(lemma, (Integer) entry1.getValue());

			}
			int maxtf = this.getDocumentMaxTF(entry.getKey().toString(), temp_tf);

			docTermDetail.setDOCID(entry.getKey().toString());
			docTermDetail.setDOCLEN(sum);
			docTermDetail.setMAXTF(maxtf);
			docTermDetail.storeTermFrequency(temp_tf);

			docTermDetailMap.put(entry.getKey().toString(), docTermDetail);
		}

		Iterator its = stemMap.entrySet().iterator();
		stemCountMap = new HashMap<String, Integer>();

		while (its.hasNext()) {

			Entry entry = (Entry) its.next();

			DictionaryDetail dicDetail = null;
			DocumentDetail docStemDetail = new DocumentDetail();

			t = (HashMap<String, Integer>) entry.getValue();
			Iterator its1 = t.entrySet().iterator();

			if (entry.getKey().toString().equals("")) {
				continue;
			}
			// System.out.println("Doc Id : " + entry.getKey().toString());

			TreeMap<String, Integer> temp_tf = new TreeMap<String, Integer>();
			int doclen = 0;
			while (its1.hasNext()) {
				Entry entry1 = (Entry) its1.next();
				String theStem = entry1.getKey().toString();
				Integer count = Integer.parseInt(entry1.getValue().toString());
				doclen += count;
				stemCountMap.put(theStem, count);

				// System.out.print("" + theStem + ",");
				if (stemIndexUncompressed.containsKey(theStem)) {
					dicDetail = stemIndexUncompressed.get(theStem);
					dicDetail.setTerm(theStem);
					dicDetail.updatePostingList(entry.getKey().toString(),
							Integer.parseInt(entry1.getValue().toString()));
					stemIndexUncompressed.put(theStem, dicDetail);

				} else {
					dicDetail = new DictionaryDetail();
					dicDetail.setTerm(theStem);
					dicDetail.updatePostingList(entry.getKey().toString(),
							Integer.parseInt(entry1.getValue().toString()));
					stemIndexUncompressed.put(theStem, dicDetail);

				}
				temp_tf.put(theStem, (Integer) entry1.getValue());

			}

			int maxtf = this.getDocumentMaxTF(entry.getKey().toString(), temp_tf);

			docStemDetail.setDOCID(entry.getKey().toString());
			docStemDetail.setDOCLEN(doclen);
			docStemDetail.setMAXTF(maxtf);
			docStemDetail.storeTermFrequency(temp_tf);

			docStemDetailMap.put(entry.getKey().toString(), docStemDetail);

			// System.out.println("\n");

		}

	}

	public void printIndexVersion1(String filepath) {
		Indexer.printTermIndexUncompressed(this.lemmaIndexUncompressed, filepath);
	}

	public void printIndexVersion2(String filepath) {
		Indexer.printStemIndexUncompressed(this.stemIndexUncompressed, filepath);
	}

	public void printCompressedIndexVersion1(String filepath) throws IOException {
		Compression compr = new Compression();
		TreeMap<String, DictionaryDetailBytes> t = Compression.getCompressedTermIndex(lemmaIndexUncompressed);
		this.lemmaIndexCompressed = t;

		Compression.storeCompressedIndex1(t, docTermDetailMap, filepath);

	}

	public void printCompressedIndexVersion2(String filepath) {
		Compression compr = new Compression();
		TreeMap<String, DictionaryDetailBytes> t = Compression.getCompressedStemIndex(stemIndexUncompressed);
		this.stemIndexCompressed = t;

		Compression.storeCompressedIndex2(t, docStemDetailMap, filepath);
	}

	public TreeMap<String, DictionaryDetailBytes> getCompressedTermIndex() {
		return this.lemmaIndexCompressed;
	}

	public TreeMap<String, DictionaryDetailBytes> getCompressedStemIndex() {
		return this.stemIndexCompressed;
	}

	public TreeMap<String, DictionaryDetail> getUncompressedTermIndex() {
		return this.lemmaIndexUncompressed;
	}

	public TreeMap<String, DictionaryDetail> getUncompressedStemIndex() {
		return this.stemIndexUncompressed;
	}

	public void answerQuery1(String[] forTerms) {
		// System.out.println(
		// "<=================================Details for { \"Reynolds\",
		// \"NASA\", \"Prandtl\", \"flow\", \"pressure\", \"boundary\",
		// \"shock\" }===================================>\n\t");
		for (String term : forTerms) {
			TreeMap<String, DictionaryDetailBytes> d = this.getCompressedTermIndex();
			String lemmaTerm = Lemmatizer.getInstance().getLemma(term.toLowerCase());
			lemmaTerm = lemmaTerm.trim();
			DictionaryDetailBytes dictionary = new DictionaryDetailBytes();
			dictionary = d.get(lemmaTerm);
			// System.out.println("Lemma value : " + lemmaTerm);
			if (dictionary.postingList != null) {
				long postlistsize = Compression.getCompressedPostingListSize(dictionary.postingList);
				System.out.println(String.format("\t %-10s  %-10d %-10d", lemmaTerm, dictionary.DOCFREQ, postlistsize));
			}
		}
	}

	private static void printTermIndexUncompressed(TreeMap<String, DictionaryDetail> termMap, String Filename) {
		// Print the Uncompressed Lemma
		BufferedWriter bfw = null;
		try {
			File newTextFile = new File(Filename);
			bfw = new BufferedWriter(new FileWriter(newTextFile, true));
			bfw.write("term=>document frequency=>postingList");
			bfw.newLine();
			Iterator iter = termMap.keySet().iterator();
			while (iter.hasNext()) {
				String key = (String) iter.next();
				if (key.toString().equals("")) {
					continue;
				}
				DictionaryDetail d = (DictionaryDetail) termMap.get(key);
				bfw.write(key + "=>" + d.getDocumentFrequency() + "=>");
				// System.out.println(key + "=>" + d.getDocumentFrequency() +
				// "=>");

				Iterator iter1 = d.getPostingList().entrySet().iterator();
				while (iter1.hasNext()) {
					Entry e = (Entry) iter1.next();
					bfw.write(e.getKey() + ":" + e.getValue() + ",");
					// System.out.println(e.getKey() + ":" + e.getValue() +
					// ",");
				}
				bfw.newLine();
				// System.out.println("\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (bfw != null)
					bfw.close();
			} catch (IOException e) {
			}
		}
	}

	public static void printStemIndexUncompressed(TreeMap<String, DictionaryDetail> stemMap, String Filename) {
		// Print the Uncompressed Stem
		BufferedWriter bfw = null;
		try {
			File newTextFile = new File(Filename);
			bfw = new BufferedWriter(new FileWriter(newTextFile, true));
			bfw.write("stem=>document frequency=>postingList");
			bfw.newLine();
			Iterator iter = stemMap.keySet().iterator();
			while (iter.hasNext()) {
				String key = (String) iter.next();
				if (key.toString().equals("")) {
					continue;
				}
				DictionaryDetail d = (DictionaryDetail) stemMap.get(key);
				bfw.write(key + "=>" + d.getDocumentFrequency() + "=>");
				// System.out.println(key + "=>" + d.getDocumentFrequency() +
				// "=>");

				Iterator iter1 = d.getPostingList().entrySet().iterator();
				while (iter1.hasNext()) {
					Entry e = (Entry) iter1.next();
					bfw.write(e.getKey() + ":" + e.getValue() + ",");
					// System.out.println(e.getKey() + ":" + e.getValue() +
					// ",");
				}
				bfw.newLine();
				// System.out.println("\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (bfw != null)
					bfw.close();
			} catch (IOException e) {
			}
		}
	}

	private static int getDocumentMaxTF(String docid, TreeMap<String, Integer> map) {
		List list = new LinkedList(map.entrySet());

		// Defined Custom Comparator here
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return (-1) * ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
			}
		});

		// Here I am copying the sorted list in HashMap
		// using LinkedHashMap to preserve the insertion order
		HashMap sortedHashMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedHashMap.put(entry.getKey(), entry.getValue());
		}
		return Collections.max(map.values());

	}

	public static void main(String args[]) {
		// For testing the functions
		Indexer indx = new Indexer();
		String s1 = "1", s2 = "2", s3 = "3";
		Integer i1 = 123, i2 = 234, i3 = 456;
		HashMap<String, Integer> mapf = new HashMap<String, Integer>();
		mapf.put(s1, i1);
		mapf.put(s2, i2);
		mapf.put(s3, i3);
		// System.out.println("Maximum in the map :
		// "+indx.getDocumentMaxTF("12324", mapf));
	}

	public TreeMap<String, DocumentDetail> getDocuments() {
		return docTermDetailMap;
	}

	public static TreeMap<String, DictionaryDetail> getTermsMap() {
		return lemmaIndexUncompressed;
	}

}

class DocumentDetail {
	private String DOC_ID;
	private int MAX_TF;
	private int DOCLEN;
	private TreeMap<String, Integer> termFrequencyList = new TreeMap<String, Integer>();

	public String getDOCID() {
		return this.DOC_ID;
	}

	public int getMAXTF() {
		return this.MAX_TF;
	}

	public int getDOCLEN() {
		return this.DOCLEN;
	}

	public boolean storeTermFrequency(TreeMap<String, Integer> treemap) {
		// boolean truth = false;
		// if (!this.termFrequencyList.containsKey(term)) {
		// this.termFrequencyList.put(term, frequency);
		// }
		// if (this.termFrequencyList.get(term).equals(frequency)) {
		// truth = true;
		// }
		boolean truth = false;
		this.termFrequencyList = treemap;
		if (this.termFrequencyList != null)
			truth = true;
		return truth;
	}

	public void setDOCID(String docid) {
		this.DOC_ID = docid;
	}

	public void setMAXTF(Integer maxTF) {
		this.MAX_TF = maxTF;
	}

	public void setDOCLEN(int DOCLEN) {
		this.DOCLEN = DOCLEN;
	}

	public TreeMap<String, Integer> getTermFrequencyList() {
		return this.termFrequencyList;
	}

}

class DictionaryDetail {
	String term; // the term
	Integer DOCFREQ;
	private TreeMap<String, Integer> postingList = null;
	private TreeMap<Integer, Integer> postingGapList = null;

	public DictionaryDetail() {
		postingList = new TreeMap<String, Integer>(new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {

				Integer i1 = Integer.parseInt(o1);
				Integer i2 = Integer.parseInt(o2);
				return i1.compareTo(i2);
				// return o1.compareTo(o2);
			}

		});
		postingGapList = new TreeMap<Integer, Integer>(new Comparator<Integer>() {

			@Override
			public int compare(Integer o1, Integer o2) {

				return o1.compareTo(o2);
			}

		});

		term = "";
		DOCFREQ = 0;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public void setDocumentFrequency(Integer DOCFREQ) {
		this.DOCFREQ = DOCFREQ;
	}

	public void updatePostingList(String docid, Integer count) {
		this.postingList.put(docid, count); // Doc Id and Term Doc Frequency
		this.DOCFREQ = this.postingList.size();
	}

	public Integer getDocumentFrequency() {
		return this.DOCFREQ;
	}

	public TreeMap<String, Integer> getPostingList() {
		return this.postingList;
	}

}
