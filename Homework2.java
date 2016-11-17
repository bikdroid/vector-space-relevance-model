package homework2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import homework2.Compression.DictionaryDetailBytes;
import homework2.Compression.PostingEntry;

public class Homework2 {

	public static void main(String args[]) throws IOException {
		TokenizerAndStemmer ts = new TokenizerAndStemmer();
		Homework2 hw = new Homework2();
		Indexer indexer = null;

		if (args[0] == "" || args[1] == "") {
			System.out.println(
					"Arguments are : <path-to-output-directory> <path-to-cranfield-dataset> <path-to-stopwords> <path-to-query-file>, please check if you missed any.");
		}
		String path = args[0]; // "E:\\Eclipse" + " " +
								// "Projects\\IRHomeworks\\src\\homework2";
		String path_to_cranfield = args[1]; // "C:\\Users\\Bikramjit\\Documents\\Fall
											// 2016\\Information" + " "
		String path_to_stopwords = args[2];
		String path_to_query_file = args[3];
		Map<String, HashMap<String, Integer>> tokenmap = ts.docsreader(path_to_cranfield, path);

		HashMap<String, HashMap<String, Integer>> stemMap = ts.getStemMap();

		String output_file = path + "\\output_rankings";
		File newTextFile = new File(output_file);
		BufferedWriter bfw = new BufferedWriter(new FileWriter(newTextFile, true));

		// System.out.println("token map size : " + tokenmap.size());
		Iterator entries = tokenmap.entrySet().iterator();

		// System.out.println("stem amp size : " + stemMap.size());
		indexer = new Indexer(tokenmap, stemMap);

		String filepath = path + "\\Index1.Uncompressed";
		// System.out.println("Index 1 creation in progress .. ");
		final long startTime1 = System.currentTimeMillis();
		indexer.printIndexVersion1(filepath);
		final long duration1 = System.currentTimeMillis() - startTime1;
		// System.out.println("Index 1 creation completed.");

		filepath = path + "\\Index2.Uncompressed";
		// System.out.println("Index 2 creation in progress .. ");
		final long startTime2 = System.currentTimeMillis();
		indexer.printIndexVersion2(filepath);
		final long duration2 = System.currentTimeMillis() - startTime2;
		// System.out.println("Index 2 creation completed.");

		TreeMap<String, DictionaryDetail> termsFullMap = indexer.getUncompressedTermIndex();
		// System.out.println("Size of termsFullMap : " + termsFullMap.size());
		TreeMap<String, DocumentDetail> documentsMap = indexer.getDocuments();
		// System.out.println("Size of DocumentsDetail : " +
		// documentsMap.size());
		RelevanceEngine relengine = new RelevanceEngine();

		relengine.updateIndexedData(termsFullMap); // Index updated
		relengine.updateDocumentsData(documentsMap, path_to_cranfield, path_to_stopwords); //
		// Document
		// details
		// updated.

		TreeMap<String, DocDetails> docsMap = relengine.newDocumentsMap;

		String queryfilepath = path_to_query_file;
		relengine.readAndUpdateQueries(queryfilepath);

		System.out.println("<<<< TOP K Values for each Query >>>>");
		List<Query> queryset = relengine.allQueries;
		Iterator its = queryset.listIterator();
		while (its.hasNext()) {
			Query q = (Query) its.next();

			bfw.write("Q" + q.number + " : " + q.text + "\n");
			HashMap<String, TermQueryDetails> queryVector = q.queryVector;
			bfw.write("Weight 1 Scheme\n");
			bfw.write("Vector Representation : ");
			Iterator itq = queryVector.entrySet().iterator();
			while (itq.hasNext()) {
				Entry e = (Entry) itq.next();
				TermQueryDetails termDetails = (TermQueryDetails) e.getValue();
				bfw.write("[" + (String) e.getKey() + ":" + termDetails.w1 + "]");
			}
			bfw.write("\n");

			// System.out.println("Q" + q.number + " : " + q.text);
			// System.out.println("Weight 1 Scheme");
			bfw.write("---- TOP K ranked docs ----\n");

			TreeMap<String, Double> w1map = q.getTopKDocuments(q.w1scores, 5);
			Iterator it1 = w1map.entrySet().iterator();
			int rank = 1;
			while (it1.hasNext()) {
				Entry e = (Entry) it1.next();
				DocDetails dt = docsMap.get((String) e.getKey());

				bfw.write(rank + ", " + dt.EXTPOINTER + ", " + (Double) e.getValue() + ", " + dt.TITLE + "\n");
				rank++;
			}
			bfw.write("\n");

			bfw.write("Weight 2 Scheme\n");
			bfw.write("Vector Representation : ");

			itq = queryVector.entrySet().iterator();
			while (itq.hasNext()) {
				Entry e = (Entry) itq.next();
				TermQueryDetails termDetails = (TermQueryDetails) e.getValue();
				bfw.write("[" + (String) e.getKey() + ":" + termDetails.w1 + "]");
			}
			bfw.write("\n");

			bfw.write("---- TOP K ranked docs ----\n");

			TreeMap<String, Double> w2map = q.getTopKDocuments(q.w2scores, 5);
			rank = 1;
			Iterator it2 = w2map.entrySet().iterator();
			while (it2.hasNext()) {
				Entry e = (Entry) it2.next();
				DocDetails dt = docsMap.get((String) e.getKey());

				bfw.write(rank + ", " + dt.EXTPOINTER + ", " + (Double) e.getValue() + ", " + dt.TITLE + "\n");
				rank++;
			}
			bfw.write("\n");

		}
		System.out.println("<<<< Please check output_ranking file under output. END >>>>");
		bfw.write("\n");
		bfw.close();

	}

}
