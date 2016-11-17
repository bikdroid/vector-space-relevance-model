package homework2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class RelevanceEngine {
	/*
	 * 1. Get the statistics for the tokens inside the docs. The
	 * docTermDetailMap, Document Details. 2. Form vectors - maps or lists. 3.
	 * Calculate the statistics and find cosine similarities.
	 */

	public static List<Query> allQueries = new ArrayList<Query>();
	public static List<Query> updatedQueries = new ArrayList<Query>();
	public static TreeMap<String, DocumentDetail> documentsMap = new TreeMap<String, DocumentDetail>();
	public static TreeMap<String, DocDetails> newDocumentsMap = new TreeMap<String, DocDetails>();
	public static TreeMap<String, DictionaryDetail> termsMap = new TreeMap<String, DictionaryDetail>();
	public String stopwords_path = null;

	public void processQueries(String querypath) {
		this.readAndUpdateQueries(querypath);
	}

	public void readAndUpdateQueries(
			String queryfile) { /*
								 * Function helps to read the query file and
								 * update the weights of terms inside Query
								 */

		String queries_file = queryfile;
		BufferedReader bfr = null;
		TreeMap<Integer, String> querymap = new TreeMap<Integer, String>(new Comparator<Integer>() {

			@Override
			public int compare(Integer o1, Integer o2) {
				// TODO Auto-generated method stub
				return o1.compareTo(o2);
			}

		});

		/*
		 * Reading the Queries.
		 */
		System.out.println("Reading the Query File\n------------------------------------------------------------");
		try {
			bfr = new BufferedReader(new FileReader(queries_file));
			String line;
			boolean queryreading = false;
			int querynumber = 0;
			StringBuilder sb = new StringBuilder();
			while ((line = bfr.readLine()) != null) {
				if (!line.isEmpty()) {
					queryreading = true;
					if (line.contains("Q") && line.contains(":")) {
						queryreading = true;
						querynumber++;
						continue;
					}

				} else {
					queryreading = false;
					querymap.put(querynumber, sb.toString());
					sb = new StringBuilder();

				}
				if (queryreading == true) {
					sb = sb.append(" " + line);
				}

			}
			querymap.put(querynumber, sb.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("<<< END >>>\n----------------------------------------------------------------------------");
		/*
		 * Processing the Queries. Calculating the statistics for each query and
		 * document.
		 */

		System.out.println("<<< QUERY TERM WEIGHTS >>>\n Calculating query specific term statistics \n");
		Iterator iter = querymap.entrySet().iterator();
		String[][] queries = new String[querymap.size()][];
		int k = 0;
		Query query;
		while (iter.hasNext()) {

			Entry en = (Entry) iter.next();
			// System.out.println((Integer) en.getKey() + "--" + (String)
			// en.getValue());
			String filteredLine = TokenizerAndStemmer.filterText((String) en.getValue());
			// det.removeStopWords(docfulltext, "E:\\IR\\stopwords");
			DocDetails dt = new DocDetails();
			filteredLine = dt.removeStopWords((String) en.getValue(), stopwords_path);
			filteredLine = getLemmatized(TokenizerAndStemmer.filterText((String) en.getValue()));
			// Put in the query maps.
			queries[k] = filteredLine.split(" ");
			query = new Query();
			query.text = filteredLine; // save the text for the query.
			query.number = (Integer) en.getKey(); // sequence number saved
			query.doclen = queries[k].length; // doclen saved.
			query.avgdoclen = query.doclen;
			query.collectionsize = 1; // Total Number of Documents in the
										// entire collection.

			TermQueryDetails td = new TermQueryDetails();
			for (String each : queries[k]) {

				if (query.queryVector.containsKey(each)) {
					td = query.queryVector.get(each);
					// increase raw wt by 1
					Integer rawwt = td.getRawWt();
					td.rawWt = rawwt + 1;
				} else {
					td = new TermQueryDetails();
					td.term = each;
					td.rawWt = 1;
					td.tfWt = 1;
					td.Df = 1;
					query.queryVector.put(each, td); // added to the query's map
														// of terms.
				}

			}
			// System.out.println("Q:" + query.number + "," + "doclen:" +
			// query.doclen + ",collectionsize:"
			// + query.collectionsize + " : ");
			Iterator its = query.queryVector.entrySet().iterator();
			Integer maxtf = 0;
			while (its.hasNext()) {
				Entry e = (Entry) its.next();
				TermQueryDetails tds = (TermQueryDetails) e.getValue();

				// System.out.print((String) e.getKey() + ":" + tds.getRawWt() +
				// " ");
				if (maxtf < tds.getRawWt())
					maxtf = tds.getRawWt();
			}
			query.maxtf = maxtf;
			// System.out.println("\n");

			Query q1 = calculateW1(query);
			if (q1 == null)
				System.out.println("");

			Query q = calculateW2(q1);
			if (q == null)
				System.out.println("");

			allQueries.add(q); // Added to global queries list

			k++;
		}

		System.out.println("<<< END >>>\n----------------------------------------------------------------------");
		System.out.println("<<< QUERY SCORES >>> \n Calculating Query scores ");
		Iterator it = allQueries.listIterator();
		System.out.println("Query Map Size : " + allQueries.size());
		while (it.hasNext()) {
			Query q = (Query) it.next();
			if (q == null)
				System.out.println("");
			// System.out.println("Q" + q.number + " : " + q.text);
			Iterator it1 = q.queryVector.entrySet().iterator();
			while (it1.hasNext()) {
				Entry e = (Entry) it1.next();
				TermQueryDetails td = (TermQueryDetails) e.getValue();
				Double w1 = td.getW1();
				Double w2 = td.w2;
				// System.out.print((String) e.getKey() + "==> W1:" + w1 + ",
				// W2:" + w2 + "\n");

			}
			Query updatedq = calculateScore(q);
			updatedQueries.add(updatedq);
		}
		System.out.print("<<< END >>>\n");
	}

	static String getLemmatized(String line) {
		StringBuilder sb = new StringBuilder();
		for (String each : line.split(" ")) {
			sb.append(Lemmatizer.getInstance().getLemma(each));
		}
		return sb.toString().trim();

	}

	static Query calculateW1(Query query) { // Function to calculate the Weight
											// 1 for each term in the query.
		/*
		 * Compute the weights of the query vector.
		 */
		Double weight1 = 0.0;
		Iterator itrs = query.queryVector.entrySet().iterator();
		// System.out.println("Query : " + query.text);
		while (itrs.hasNext()) {
			Entry e = (Entry) itrs.next();
			TermQueryDetails termdetail = (TermQueryDetails) e.getValue();
			/*
			 * W1 formula
			 */
			Double part1 = 0.4 + 0.6 * Math.log10(0.5 + termdetail.rawWt) / Math.log10(query.maxtf + 1.0);
			// System.out.print(termdetail.term+"==> part1, numerator:" +
			// Math.log10(0.5 + termdetail.rawWt) + ", denominator:"
			// + Math.log10(query.maxtf + 1.0)+"\n");
			Double part2 = 1.0;
			if (query.collectionsize == 1 && termdetail.Df == 1) {
				part2 = 1.0;
			}
			// Double part2 = Math.log10(query.collectionsize / termdetail.Df) /
			// Math.log10(query.collectionsize);
			weight1 = part1 * part2;
			termdetail.w1 = weight1;

			/* Calculate the W2 score as well */

			e.setValue(termdetail);
		}
		return query;
	}

	static Query calculateW2(Query query) { // Function to calculate the Weight
											// 2 (Okapi), for each term in the
											// query.

		Double weight2 = 0.0;
		int doclen = query.doclen;
		int avgdoclen = query.avgdoclen;
		int collectionsize = query.collectionsize;
		int maxtf = query.maxtf;

		Iterator itrs = query.queryVector.entrySet().iterator();

		while (itrs.hasNext()) {
			Entry e = (Entry) itrs.next();
			TermQueryDetails termdetail = (TermQueryDetails) e.getValue();
			int df = termdetail.getDf();
			int tf = termdetail.getTFWt();

			Double w2 = getW2Score(tf, maxtf, collectionsize, df, doclen, avgdoclen);
			termdetail.w2 = w2;
			e.setValue(termdetail);
		}
		return query;
	}

	public static Query calculateScore(Query query) {

		Set<String> query_terms = query.queryVector.keySet();
		for (String each : query_terms) {
			String term = Lemmatizer.getInstance().getLemma(each);
			// System.out.print(term + ",");
			DictionaryDetail dict = termsMap.get(term);
			boolean isNotInCollection = false;
			if (dict == null) {
				isNotInCollection = true;
				continue;

			} else {
			}
			/*
			 * Get the details for the term :
			 */
			TermQueryDetails td = query.queryVector.get(each);
			Double wq1 = td.w1;
			Double wq2 = td.w2;

			/*
			 * weight is already calculated here.
			 * 
			 * for each pair(d, tf[t,d]) in postings list
			 */

			TreeMap<String, Integer> postingList = dict.getPostingList();
			Iterator its = postingList.entrySet().iterator();
			while (its.hasNext()) {
				Entry e = (Entry) its.next();
				/* check the document detail for the document */
				DocumentDetail documentDetail = documentsMap.get((String) e.getKey());
				DocDetails docdet = newDocumentsMap.get((String) e.getKey());
				String docid = (String) e.getKey();
				int doclen = docdet.getDOCLEN(); // get the document length that
													// is after removing
													// stopwords.
				int maxtf = documentDetail.getMAXTF();
				int averagedoclen = getAverageDocLength();

				/* Calculate weight [t,d] */
				int tf = documentDetail.getTermFrequencyList().get(term);
				int df = postingList.size();
				int collectionsize = 1400;

				/* We have the docid, score .. so, next we update */
				/*
				 * Calculation of W1 score and update
				 */
				Double wtd;
				if (isNotInCollection)
					wtd = 0.0;
				else
					wtd = getW1Score(tf, maxtf, collectionsize, df);

				query.updateW1Score(docid, wtd * wq1);

				/*
				 * Calculation of W2 score and update
				 */
				Double wtd2;

				if (isNotInCollection)
					wtd2 = 0.0;
				else
					wtd2 = getW2Score(tf, maxtf, collectionsize, df, doclen, averagedoclen);
				query.updateW2Score(docid, wtd2 * wq2);

			}
		}

		TreeMap<String, Double> w1scores = query.w1scores;
		TreeMap<String, Double> w2scores = query.w2scores;

		Iterator iter1 = w1scores.entrySet().iterator();
		// System.out.println("W1 Scores : ");
		while (iter1.hasNext()) {
			Entry e = (Entry) iter1.next();
			// System.out.print("[" + (String) e.getKey() + " : " + (Double)
			// e.getValue() + "] ");
		}
		System.out.println("");
		iter1 = w2scores.entrySet().iterator();
		// System.out.println("W2 Scores : ");
		while (iter1.hasNext()) {
			Entry e = (Entry) iter1.next();
			// System.out.print("[" + (String) e.getKey() + " : " + (Double)
			// e.getValue() + "] ");
		}
//		System.out.println("");
		return query;

	}

	static Double getW1Score(int tf, int maxtf, int collectionsize, int df) {
		if (df == 0) {
			return 0.0;
		}
		Double w1score = (0.4 + 0.6 * Math.log10(tf + 0.5) / Math.log10(maxtf + 1.0))
				* (Math.log10(collectionsize / df) / Math.log10(collectionsize));
		return w1score;
	}

	static Double getW2Score(int tf, int maxtf, int collectionsize, int df, int doclen, int averagedoclen) {
		if (df == 0) {
			return 0.0;
		}
		Double w2score = 0.0;
		w2score = (0.4 + 0.6 * (tf / (tf + 0.5 + 1.5 * (doclen / averagedoclen)))) * (1);
		// System.out.println("w2 calculated : " + w2score);

		return w2score;
	}

	public static int getAverageDocLength() {
		Iterator iter = newDocumentsMap.entrySet().iterator();
		int sum = 0;
		while (iter.hasNext()) {
			Entry e = (Entry) iter.next();
			DocDetails dt = (DocDetails) e.getValue();
			sum = sum + dt.getDOCLEN();
		}

		return sum / newDocumentsMap.size();

	}

	/* Below Functions help get data from Indexer and Others */

	public void updateDocumentsData(TreeMap<String, DocumentDetail> documentsMap, String pathtodocs,
			String path_to_stopwords) {
		this.documentsMap = documentsMap;
		this.stopwords_path = path_to_stopwords;
		File f = null;
		File[] paths;
		f = new File(pathtodocs);
		FileFilter filter = new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.isFile();
			}
		};

		// returns pathnames for files and directory
		paths = f.listFiles(filter);
		BufferedReader br = null;

		for (File path : paths) {

			String docId = path.getName().split("cranfield")[1];
			try {
				br = new BufferedReader(new FileReader(path));
				StringBuilder title = new StringBuilder();
				StringBuilder doctext = new StringBuilder();
				String line;
				boolean titleflag = false;
				while ((line = br.readLine()) != null) {
					// System.out.println(line);
					if (line.contains("</TITLE>")) {
						titleflag = false;
					}
					if (titleflag) {
						title = title.append(line);
					}
					if (line.contains("<TITLE>")) {
						titleflag = true;
					}
					doctext.append(line);

				}
				String titletext = title.toString().replaceAll("<(\"[^\"]*\"|'[^']*'|[^'\">])*>", "");
				String docfulltext = doctext.toString().replaceAll("<(\"[^\"]*\"|'[^']*'|[^'\">])*>", "");
				DocDetails det = new DocDetails();
				det.setTITLE(titletext); // setting the TITLE
				det.setDatasetPath(path.getName()); // setting the dataset path
				det.setEXTPOINTER("cranfield" + docId); // setting the extermnal
														// pointer
				String filteredline = det.removeStopWords(docfulltext, path_to_stopwords);
				det.setDOCLEN(filteredline);
				newDocumentsMap.put(docId, det);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Iterator its = newDocumentsMap.entrySet().iterator();
		System.out.println("Documents Saved  ");
		while (its.hasNext()) {
			Entry e = (Entry) its.next();
			DocDetails det = (DocDetails) e.getValue();
//			System.out.println((String) e.getKey() + "==>" + det.TITLE);
		}
		System.out.println("<< end >>");

	}

	public void updateIndexedData(TreeMap<String, DictionaryDetail> termsMap) {
		/*
		 * Runs the Indexer and prepares the Document Mapping from document IDs
		 * to the term frequencies.
		 */
		this.termsMap = termsMap;

	}

	public TreeMap<String, DocDetails> getNewDocumentsMap() {
		return this.newDocumentsMap;
	}

}

class Query {

	String text; // the query text.
	Integer number; // Sequence number of the query.
	Integer maxtf; // Maximum tf among the terms in the query.
	Integer doclen; // number of words in the query.
	Integer avgdoclen; // same as doclen.
	Integer collectionsize; // for a query it is 1.
	public TreeMap<String, Double> w1scores = new TreeMap<String, Double>(); // docid
																				// -->
																				// w1
																				// score,
																				// keep
																				// increasing
	// score as and when calculated.
	public TreeMap<String, Double> w2scores = new TreeMap<String, Double>(); // docid
																				// -->
																				// w2
																				// score

	TreeMap<String, Score> scoreMap = new TreeMap<String, Score>(new Comparator<String>() {

		@Override
		public int compare(String o1, String o2) {
			// TODO Auto-generated method stub

			return o1.compareTo(o2);
		}

	}); // Mapping docid ==> cosine-similarity

	HashMap<String, TermQueryDetails> queryVector = new HashMap<String, TermQueryDetails>();

	public void updateW1Score(String docid, Double wtd) {
		if (w1scores.containsKey(docid)) {
			Double score = w1scores.get(docid);
			score = score + wtd;
			w1scores.put(docid, score);
		} else {
			w1scores.put(docid, wtd);
		}
	}

	public void updateW2Score(String docid, Double wtd) {
		if (w2scores.containsKey(docid)) {
			Double score = w2scores.get(docid);
			score = score + wtd;
			w2scores.put(docid, score);
		} else {
			w2scores.put(docid, wtd);
		}
	}

	private static class ValueComparator implements Comparator<String> {

		private TreeMap<String, Double> _data = null;

		public ValueComparator(TreeMap<String, Double> data) {
			super();
			_data = data;
		}

		@Override
		public int compare(String o1, String o2) {
			// TODO Auto-generated method stub
			Double d1 = (Double) _data.get(o1);
			Double d2 = (Double) _data.get(o2);
			if (d1.equals(d2))
				return 1;
			return d2.compareTo(d1);
		}

	}

	public TreeMap<String, Double> getTopKDocuments(TreeMap<String, Double> data, int K) {

		TreeMap<String, Double> w1map = new TreeMap<String, Double>(new ValueComparator(data));// =
		// w1scores;
		w1map.putAll(data);
		String fkey = w1map.firstKey();
		TreeMap<String, Double> w1map2 = new TreeMap<String, Double>(new ValueComparator(data));// =
		w1map2.clear();

		Iterator it = w1map.entrySet().iterator();
		while (it.hasNext() && K > 0) {
			K--;
			Entry e1 = (Entry) it.next();
			w1map2.put((String) e1.getKey(), (Double) e1.getValue());
		}
		return w1map2;
	}

}

class Score {
	Double w1score;
	Double w2score;

	public Double getW1Score() {
		return this.w1score;
	}

	public Double getW2Score() {
		return this.w2score;
	}

}

class TermQueryDetails {

	// Details for inside query.
	String term;
	Integer rawWt;
	Integer tfWt;
	Integer Df;
	Double w1;
	Double w2;

	public Integer getRawWt() {
		return this.rawWt;
	}

	public Integer getTFWt() {
		return this.tfWt;
	}

	public Integer getDf() {
		return this.Df;
	}

	public Double getW1() {
		return this.w1;
	}

	public String getTerm() {
		return this.term;
	}

}

class TermCollectionDetails {
	// Details for a query.
	String term;
	Integer rawWt;
	Integer tfWt;
	Integer Df;
	Double w1;
	Double w2;

	public Integer getRawWt() {
		return this.rawWt;
	}

	public Integer getTFWt() {
		return this.tfWt;
	}

	public Integer getDf() {
		return this.Df;
	}

	public Double getW1() {
		return this.w1;
	}

	public String getTerm() {
		return this.term;
	}
}

class DocDetails {

	String docid;
	Integer DOCLEN; // discounting stopwords.
	String TITLE; // title of the document.
	String EXTPOINTER; // external pointer.
	String datasetpath;

	public Integer getDOCLEN() {

		return this.DOCLEN;
	}

	public void setDOCLEN(String text) {
		this.DOCLEN = text.split(" ").length;
	}

	public void setEXTPOINTER(String extpointer) {
		this.EXTPOINTER = extpointer;
	}

	public void setTITLE(String title) {
		this.TITLE = title;
	}

	public void setDatasetPath(String datasetpath) {
		this.datasetpath = datasetpath;
	}

	public void setDOCID(String docid) {
		this.docid = docid;
	}

	public String removeStopWords(String text, String stopwordspath) { // needs
																		// the
																		// line
																		// and
																		// the
																		// stopwords
																		// file
																		// path

		String line = text.replaceAll("<(\"[^\"]*\"|'[^']*'|[^'\">])*>", "");
		/* Only removed Stop Words. */

		BufferedReader br = null;
		List<String> l = new ArrayList<String>();
		try {
			br = new BufferedReader(new FileReader(stopwordspath));
			String stopline;
			while ((stopline = br.readLine()) != null) {

				l.add(stopline.trim());
				line.replaceAll(stopline.trim(), "");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		line = line.replaceAll("\\s+", " ");
		return line;
	}
}