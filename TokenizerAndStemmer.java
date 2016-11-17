package homework2;

//import java.nio.file;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TokenizerAndStemmer {

	private static HashMap<String, HashMap<String, Integer>> tokenMap; // docId
																		// =>
																		// Map(token
																		// =>
																		// count)
	private static HashMap<String, HashMap<String, Integer>> stemMap; // docId
																		// =>
																		// Map(stem=>count)
	private static HashMap<String, Integer> allStems; // stem => count
	private static HashMap<String, Integer> allTokens; // token => count
	private static Stemmer stemmer;
	public static List<String> l;

	// Map<>
	public Map<String, HashMap<String, Integer>> docsreader(String path_to_dataset, String project_home_path) {
		File f = null;
		File[] paths;

		BufferedReader bfreader = null;
		String line = null;
		String[] line_tokens;
		allTokens = new HashMap<String, Integer>();
		allStems = new HashMap<String, Integer>();
		stemMap = new HashMap<String, HashMap<String, Integer>>();
		tokenMap = new HashMap<String, HashMap<String, Integer>>();

		// File file = new File("/home/bikram/output.txt");

		HashMap<String, Integer> stem_temp_map = new HashMap<String, Integer>();
		HashMap<String, Integer> token_temp_map = new HashMap<String, Integer>();

		int totalTokens = 0;
		int tokensOnlyOnce = 0;
		int totalStems = 0;
		int sumOfUniqueTokens = 0;
		int sumOfUniqueStems = 0;

		try {

			/*
			 * SETTING THE PATH TO DATASET
			 */

			f = new File(path_to_dataset);
			FileFilter filter = new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.isFile();
				}
			};

			// returns pathnames for files and directory
			paths = f.listFiles(filter);
			int file_count = 0;
			String docId = null;

			// for writing to the output.txt file.

			/*
			 * STOP WORDS
			 */
			BufferedReader br = null;
			l = new ArrayList<String>();
			try {
				br = new BufferedReader(new FileReader(project_home_path + "//stopwords"));
				String stopline;
				System.out.println("Stop Words");
				while ((stopline = br.readLine()) != null) {

					l.add(stopline.trim());
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			for (File path : paths) {

				docId = path.getName().split("cranfield")[1];

				file_count++;
				bfreader = new BufferedReader(new FileReader(path));
				List<String> temp = null;

				while ((line = bfreader.readLine()) != null) {

					/*
					 * 
					 * HANDLING SGML TAGS
					 */
					line = line.replaceAll("<(\"[^\"]*\"|'[^']*'|[^'\">])*>", "");

					// Checking for phone-numbers
					Pattern p = Pattern.compile("([(]{1}[0-9]{3}[)]{1}[0-9]{3}[-]{1}[0-9]{4})");
					Matcher m = p.matcher(line);
					if (m.find()) {
						for (int i = 1; i <= m.groupCount(); i++) {
							line = line.replaceAll(m.group(i), "");

							/*
							 * Update the count of the token.
							 */

							Integer count = allTokens.get(m.group(i));
							if (count == null) {
								count = 1;
							} else {
								count += 1;
							}
							if (!l.contains(m.group(i)))
								allTokens.put(m.group(i), count);
							addToTokenMap(m.group(i), docId);

							/*
							 * Update the big tokenMap.
							 */
							// addToTokenMap(m.group(i), docId);

							totalTokens++;
						}
					}

					// p = Pattern.compile("");

					/*
					 * WORDS WITH - , . / \ ! ? ( )
					 */
					line = line.replaceAll("[-.]", "");
					line = line.replaceAll("[,/\\!?()_]", " ");

					/*
					 * 
					 * */
					line = line.replaceAll("[^a-zA-Z\\s]+", "");

					/*
					 * POSESSIVES
					 */
					line = line.replaceAll("'", "");
					line = line.replaceAll("'s", "");
					line = line.replaceAll("o'", "");
					line = line.replaceAll("l'", "");

					// Removing single letters
					// line = line.replaceAll("(^[a-z][ ])|([ ][a-z][ ])|([
					// ][a-z][_|?.-])", " ");

					// Removing single digits
					// line = line.replaceAll("(^[0-9][ ])|([ ][0-9][ ])|([
					// ][0-9][_|?.-])", " ");

					line = line.replaceAll("[\\s]", " ");

					line_tokens = line.split(" ");

					token_temp_map = new HashMap<String, Integer>();
					stem_temp_map = new HashMap<String, Integer>();

					for (String each : line_tokens) {

						/*
						 * Update the docIds related to the token.
						 */

						each = each.toLowerCase().trim(); // to lower case and
															// trim from both
															// sides.

						Integer count;

						/*
						 * Update the count of the token.
						 */
						count = allTokens.get(each);
						if (count == null) {
							count = 1;
						} else {
							count += 1;
						}
						if (!l.contains(each))
							allTokens.put(each, count);

						totalTokens++;

						// Doc Id to Tokens Map

						count = 0;
						if (tokenMap.get(docId) == null) {
							count++;// increase count for the token
							if (!l.contains(each)) {
								if (each != "" || each.length() != 0) {
									token_temp_map.put(each, count);
									tokenMap.put(docId, token_temp_map);
								}
							}
						} else {
							token_temp_map = tokenMap.get(docId);
							if (token_temp_map.get(each) == null) {
								count = 1;
								if (!l.contains(each)) {
									if (each != "" || each.length() != 0) {
										token_temp_map.put(each, count);
										tokenMap.put(docId, token_temp_map);
									}
								}
							} else {
								count = token_temp_map.get(each);
								count += 1;
								if (!l.contains(each)) {
									if (each != "" || each.length() != 0) {
										token_temp_map.put(each, count);
										tokenMap.put(docId, token_temp_map);
									}
								}
							}
						}

						/*
						 * 
						 * STEM
						 */
						String stem = generateStem(each);

						// All Stems Map
						count = allStems.get(stem);
						if (count == null) {
							count = 1;
						} else {
							count = count + 1;
						}
						if (!l.contains(each))
							allStems.put(stem, count);

						// DocId to Stems Map
						count = 0;
						if (stemMap.get(docId) == null) {
							count++; // increase count for the stem.
							if (!l.contains(stem)) {
								if (stem != "" || stem.length() != 0) {
									stem_temp_map.put(stem, count);
									stemMap.put(docId, stem_temp_map); // new
																		// record
																		// stored
																		// in
																		// the
																		// stemMap
								}

							}
						} else {
							// docId has a record, so check the stem => count
							// map.
							stem_temp_map = stemMap.get(docId);
							if (stem_temp_map.get(stem) == null) {
								count = 1;
								if (!l.contains(stem)) {
									if (stem != "" || stem.length() != 0) {
										stem_temp_map.put(stem, count);
										stemMap.put(docId, stem_temp_map);
									}
								}

							} else {
								count = stem_temp_map.get(stem);
								count += 1;
								if (!l.contains(stem)) {
									if (stem != "" || stem.length() != 0) {
										stem_temp_map.put(stem, count);
										stemMap.put(docId, stem_temp_map);
									}
								}
							}
						}

					}
				}

				// Unique stems
				sumOfUniqueStems = sumOfUniqueStems + stemMap.get(docId).size();
				sumOfUniqueTokens = sumOfUniqueTokens + tokenMap.get(docId).size();

			}

			/*
			 * Calculating the average unique stems per doc
			 */
			System.out.println("Total Files : " + file_count);

			int averageWordStemsPerDoc = sumOfUniqueStems / file_count;
			int averageWordTokensPerDoc = sumOfUniqueTokens / file_count;

			/*
			 * Iterating over the created Tokens
			 */

			Iterator<Entry<String, Integer>> it1 = allStems.entrySet().iterator();
			Entry<String, Integer> count_entry;
			int countStemsWithValueOne = 0;
			int countTokensWithValueOne = 0;

			/*
			 * 
			 * Sorting the stem Map
			 */
			HashMap<String, Integer> hmap = new HashMap<String, Integer>();
			hmap = getSortedMap(allStems);
			it1 = hmap.entrySet().iterator();
			Entry<String, Integer> hmap_entry;
			int first30 = 30;

			while (it1.hasNext()) {
				hmap_entry = it1.next();
				if (hmap_entry.getValue() == 1) {
					countStemsWithValueOne++;
				}
			}

			hmap = getSortedMap(allTokens);
			it1 = hmap.entrySet().iterator();
			while (it1.hasNext()) {
				hmap_entry = it1.next();
				if (hmap_entry.getValue() == 1) {
					countTokensWithValueOne++;
				}
			}

			System.out.println("::: TOKENS :::");
			System.out.println("Total number of tokens : " + totalTokens);
			System.out.println("Total number of unique tokens : " + allTokens.size());
			System.out.println("The number of Tokens that occur only once : " + countTokensWithValueOne);
			System.out.println("The 30 most frequent word tokens with frequencies : ");

			hmap = getSortedMap(allTokens);
			System.out.println("First entry -" + hmap.values().toArray()[0] + "-");
			// hmap.values().remove(hmap.values().toArray()[0]);
			hmap.remove("");
			it1 = hmap.entrySet().iterator();

			while (it1.hasNext() && first30 > 0) {
				hmap_entry = it1.next();

				System.out.println(hmap_entry.getKey() + " : " + hmap_entry.getValue());
				first30--;
			}
			System.out.println("The average number of word Tokens per document : " + averageWordTokensPerDoc);

			System.out.println("\n");
			System.out.println("::: STEMS :::");
			System.out.println("Number of distinct stems in the Cranfield collection : " + allStems.size());
			System.out.println("Number of stems that occur only once : " + countStemsWithValueOne);
			System.out.println("The 30 most frequent stems in the Cranfield Text Document collection : ");

			hmap = getSortedMap(allStems);
			hmap.remove("");
			it1 = hmap.entrySet().iterator();
			hmap.remove(null);
			first30 = 30;
			while (it1.hasNext() && first30 > 0) {
				hmap_entry = it1.next();
				System.out.println(hmap_entry.getKey() + " : " + hmap_entry.getValue());
				first30--;
			}
			System.out.println("The average number of word stems per doc : " + averageWordStemsPerDoc);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return tokenMap;

	}

	private static void addToTokenMap(String each, String docId) {
		Integer count;
		count = 0;
		HashMap<String, Integer> token_temp_map = new HashMap<String, Integer>();

		if (tokenMap.get(docId) == null) {
			count++;// increase count for the token
			if (!each.equals(" "))
				token_temp_map.put(each, count);
			tokenMap.put(docId, token_temp_map);
		} else {
			token_temp_map = tokenMap.get(docId);
			if (token_temp_map.get(each) == null) {
				count = 1;
				if (!each.equals(" "))
					token_temp_map.put(each, count);
				tokenMap.put(docId, token_temp_map);
			} else {
				count = token_temp_map.get(each);
				count += 1;
				if (!each.equals(" "))
					token_temp_map.put(each, count);
				tokenMap.put(docId, token_temp_map);
			}
		}
	}

	private static String generateStem(String token) {
		stemmer = new Stemmer();
		stemmer.add(token.toCharArray(), token.length());
		stemmer.stem();
		return stemmer.toString();
	}

	public HashMap getSortedMap(HashMap map) {
		return sortByValues(map);
		// return null;
	}

	private static HashMap sortByValues(HashMap map) {
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
		return sortedHashMap;
	}

	public HashMap<String, HashMap<String, Integer>> getStemMap() {
		return stemMap;
	}

	public static String filterText(String line) {

		line = line.replaceAll("<(\"[^\"]*\"|'[^']*'|[^'\">])*>", "");

		// Checking for phone-numbers
		Pattern p = Pattern.compile("([(]{1}[0-9]{3}[)]{1}[0-9]{3}[-]{1}[0-9]{4})");
		Matcher m = p.matcher(line);
		if (m.find()) {
			for (int i = 1; i <= m.groupCount(); i++) {
				line = line.replaceAll(m.group(i), "");
				Integer count = allTokens.get(m.group(i));
				if (count == null) {
					count = 1;
				} else {
					count += 1;
				}
				if (!l.contains(m.group(i))) {

				}

			}
		}
		/*
		 * WORDS WITH - , . / \ ! ? ( )
		 */
		line = line.replaceAll("[-.]", "");
		line = line.replaceAll("[,/\\!?()_]", " ");
		line = line.replaceAll("[^a-zA-Z\\s]+", "");

		/*
		 * POSESSIVES
		 */
		line = line.replaceAll("'", "");
		line = line.replaceAll("'s", "");
		line = line.replaceAll("o'", "");
		line = line.replaceAll("l'", "");
		line = line.replaceAll("\\s+", " ");
		return line;
	}
}
