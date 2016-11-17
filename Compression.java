package homework2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import java.util.TreeMap;

public class Compression {

	public static void main(String args[]) throws IOException {
		Map<byte[], String> newmap = new HashMap<byte[], String>();
		TreeMap<String, Integer> p = null;
		p = new TreeMap<String, Integer>(new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				// TODO Auto-generated method stub

				Integer i1 = Integer.parseInt(o1);
				Integer i2 = Integer.parseInt(o2);
				return i1.compareTo(i2);
			}

		});

		p.put("1400", 2);
		p.put("110", 2);
		p.put("123", 2);
		p.put("3333", 2);
		p.put("1", 2);

		Iterator it = p.entrySet().iterator();
		it.next();
		while (it.hasNext()) {
			Entry e = (Entry) it.next();

			System.out.println(e.getKey().toString());
		}
	}

	public static TreeMap<String, DictionaryDetailBytes> getCompressedTermIndex(
			TreeMap<String, DictionaryDetail> termMap) {

		TreeMap<String, DictionaryDetailBytes> compressedMap = new TreeMap<String, DictionaryDetailBytes>();
		Iterator it = termMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry entry = (Entry) it.next();
			String term = entry.getKey().toString();
			DictionaryDetail detail = (DictionaryDetail) entry.getValue();
			Integer docfreq = detail.getDocumentFrequency();

			/*
			 * Below I first convert the entire String,Integer => docid,tf map
			 * to an Integer,Integer => docid,tf map. This will help to sort the
			 * keys.
			 */
			TreeMap<String, Integer> posList = detail.getPostingList();
			

			List<PostingEntry> posByteList = new ArrayList<PostingEntry>();
			Iterator it1 = posList.entrySet().iterator(); //
			while (it1.hasNext()) {
				Entry entry1 = (Entry) it1.next();
				// Getting the gamma and delta codes.
				String docidgamma = getGammaCodes(Integer.parseInt(entry1.getKey().toString()));
				byte[] docid = convertToByteArray(docidgamma);
				String freqgamma = getGammaCodes((Integer) entry1.getValue());
				byte[] frequency = convertToByteArray(freqgamma);
				posByteList.add(new PostingEntry(docid, frequency));

			}
			DictionaryDetailBytes compressedDictionary = new DictionaryDetailBytes(term, docfreq, posByteList);
			compressedMap.put(term, compressedDictionary);

		}

		return compressedMap;

	}

	public static TreeMap<String, DictionaryDetailBytes> getCompressedStemIndex(
			TreeMap<String, DictionaryDetail> stemMap) {
		TreeMap<String, DictionaryDetailBytes> compressedMap = new TreeMap<String, DictionaryDetailBytes>();
		Iterator it = stemMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry entry = (Entry) it.next();
			String term = entry.getKey().toString();
			DictionaryDetail detail = (DictionaryDetail) entry.getValue();
			Integer docfreq = detail.getDocumentFrequency();
			TreeMap<String, Integer> posList = detail.getPostingList();
			List<PostingEntry> posByteList = new ArrayList<PostingEntry>();

			Iterator it1 = posList.entrySet().iterator();
			while (it1.hasNext()) {
				Entry entry1 = (Entry) it1.next();
				// Getting the gamma and delta codes.
				String docidgamma = getDeltaCodes(Integer.parseInt(entry1.getKey().toString()));
				byte[] docid = convertToByteArray(docidgamma);
				String freqgamma = getDeltaCodes((Integer) entry1.getValue());
				byte[] frequency = convertToByteArray(freqgamma);
				posByteList.add(new PostingEntry(docid, frequency));

			}
			DictionaryDetailBytes compressedDictionary = new DictionaryDetailBytes(term, docfreq, posByteList);
			compressedMap.put(term, compressedDictionary);

		}

		return compressedMap;
	}

	public static void storeCompressedIndex1(TreeMap<String, DictionaryDetailBytes> lemmaIndexCompressed,
			TreeMap<String, DocumentDetail> docTermDetailMap, String FileName) {

		/*
		 * Steps : 1. Creating a RandomFile 2. Getting the Compressed Term Index
		 * 3. Storing the Index
		 *
		 * 
		 */
		System.out.println("Storing the Compressed Term Index");
		try {
			Trie fcdict = new Trie();
			RandomAccessFile outputFile = new RandomAccessFile(FileName, "rw");
			ArrayList<String> pointer = new ArrayList<String>();

			String prefix = "", oldprefix = "", suffix = "";
			ArrayList<String> keyarray = new ArrayList<String>(lemmaIndexCompressed.keySet());
			String key = lemmaIndexCompressed.keySet().toArray()[0].toString();
			System.out.println("Term : " + key);
			// Iterator iter = lemmaIndexCompressed.entrySet().iterator();

			if (key.length() > 3) {
				fcdict.insert(key);
			}
			outputFile.write((key.length() + key).getBytes());

			final int k = 8; // k blocks for block coding.
			String[] words = new String[8];
			for (int i = 1; i < keyarray.size(); i++) {

				String firstword = null;
				StringBuilder sb = new StringBuilder();

				for (int j = 0; j < k; j++) {
					if (i > keyarray.size())
						break;
					words[j] = keyarray.get(i);
					i++;
					sb.append(words[j].length());
					sb.append(words[j]);

				}

				String sbStr = sb.toString();
				outputFile.write((sbStr).getBytes());
				outputFile.write(System.getProperty("line.separator").getBytes());
				String p = Long.toString(outputFile.getFilePointer());
				pointer.add(p);

			}

			// The front coding encoding
			// for (int i = 1; i < keyarray.size(); i++) {
			// if (key.length() > 3 && keyarray.get(i).length() > 3 && fcdict !=
			// null) {
			// if (fcdict.getMatchingPrefix(keyarray.get(i)).length() > 0) {
			// prefix = fcdict.getMatchingPrefix(keyarray.get(i));
			// if (!oldprefix.equals(prefix) && oldprefix.contains(prefix)) {
			// // If there is a possibility for new prefix
			// key = keyarray.get(i);
			// suffix = keyarray.get(i).replace(prefix, "").trim();
			//
			// // A new prefix is started.
			// outputFile.write((key.length() + prefix + "*" +
			// suffix).getBytes());
			//
			// // The dict is now fresh
			// fcdict = null;
			// } else {
			// suffix = keyarray.get(i).replace(prefix, "").trim();
			// outputFile.write((suffix.length() + "$" + suffix).getBytes());
			//
			// }
			// } else {
			// fcdict = null;
			// key = keyarray.get(i);
			// fcdict = new Trie();
			// fcdict.insert(key);
			// if (i < (keyarray.size() - 1) &&
			// fcdict.getMatchingPrefix(keyarray.get(i + 1)).length() > 0) {
			// prefix = fcdict.getMatchingPrefix(keyarray.get(i + 1));
			// suffix = keyarray.get(i).replace(prefix, "").trim();
			// outputFile.write((key.length() + prefix + "*" +
			// suffix).getBytes());
			//
			// } else {
			// outputFile.write((key.length() + key).getBytes());
			// }
			//
			// }
			//
			// } else if (key.length() > 3 && keyarray.get(i).length() > 3 &&
			// fcdict == null) {
			// fcdict = new Trie();
			// fcdict.insert(key);
			//
			// if (fcdict.getMatchingPrefix(keyarray.get(i)).length() > 0) {
			// prefix = fcdict.getMatchingPrefix(keyarray.get(i));
			// if (!oldprefix.equals(prefix) && oldprefix.contains(prefix)) {
			// key = keyarray.get(i);
			// suffix = keyarray.get(i).replace(prefix, "").trim();
			// outputFile.write((key.length() + prefix + "*").getBytes());
			// outputFile.write((suffix.length() + "$" + suffix).getBytes());
			// fcdict = null;
			// } else {
			// suffix = keyarray.get(i).replace(prefix, "").trim();
			// outputFile.write((suffix.length() + "$" + suffix).getBytes());
			//
			// }
			// } else {
			// fcdict = null;
			// key = keyarray.get(i);
			// fcdict = new Trie();
			// fcdict.insert(key);
			// if (i < (keyarray.size() - 1) &&
			// fcdict.getMatchingPrefix(keyarray.get(i + 1)).length() > 0) {
			// prefix = fcdict.getMatchingPrefix(keyarray.get(i + 1));
			// suffix = keyarray.get(i).replace(prefix, "").trim();
			// outputFile.write((key.length() + prefix + "*").getBytes());
			// outputFile.write((suffix).getBytes());
			//
			// } else {
			// outputFile.write((key.length() + key).getBytes());
			// }
			//
			// }
			// } else {
			// key = keyarray.get(i);
			// outputFile.write((key.length() + key).getBytes());
			// fcdict = null;
			// }
			// oldprefix = prefix;
			// // Storing the location for the Random Access File, so as to
			// // seek later.
			// String p = Long.toString(outputFile.getFilePointer());
			// pointer.add(p);
			// }

			Iterator iter = lemmaIndexCompressed.entrySet().iterator();
			DictionaryDetailBytes detailbytes = new DictionaryDetailBytes();

			for (int j = 0; j < pointer.size(); j++) {
				outputFile.write((pointer.get(j) + "-").getBytes());
				Integer gap=0, prev_docid=0, curr_docid=0;
				if (iter.hasNext()) {
					Entry e = (Entry) iter.next();
					detailbytes = (DictionaryDetailBytes) e.getValue();
					outputFile.write(("" + detailbytes.DOCFREQ).getBytes());
					Iterator its = detailbytes.postingList.iterator();
					while (its.hasNext()) {
						PostingEntry e1 = (PostingEntry) its.next();
						
						outputFile.write(e1.docId);

						byte[] c = ("-").getBytes();

						outputFile.write(c);
						outputFile.write(e1.frequency);

					}
				}
				outputFile.write(System.getProperty("line.separator").getBytes());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void storeCompressedIndex2(TreeMap<String, DictionaryDetailBytes> stemIndexCompressed,
			TreeMap<String, DocumentDetail> docStemDetailMap, String FileName) {
		/*
		 * Steps : 1. Creating a RandomFile 2. Getting the Compressed Term Index
		 * 3. Storing the Index
		 *
		 * 
		 */
		System.out.println("Storing the Compressed Stem Index");
		try {
			Trie fcdict = new Trie();
			RandomAccessFile outputFile = new RandomAccessFile(FileName, "rw");
			ArrayList<String> pointer = new ArrayList<String>();

			String prefix = "", oldprefix = "", suffix = "";
			ArrayList<String> keyarray = new ArrayList<String>(stemIndexCompressed.keySet());
			String key = stemIndexCompressed.keySet().toArray()[0].toString();
			System.out.println("Term : " + key);
			// Iterator iter = lemmaIndexCompressed.entrySet().iterator();

			if (key.length() > 3) {
				fcdict.insert(key);
			}
			outputFile.write((key.length() + key).getBytes());

			for (int i = 1; i < keyarray.size(); i++) {
				if (key.length() > 3 && keyarray.get(i).length() > 3 && fcdict != null) {
					if (fcdict.getMatchingPrefix(keyarray.get(i)).length() > 0) {
						prefix = fcdict.getMatchingPrefix(keyarray.get(i));
						if (!oldprefix.equals(prefix) && oldprefix.contains(prefix)) {
							// If there is a possibility for new prefix
							key = keyarray.get(i);
							suffix = keyarray.get(i).replace(prefix, "").trim();

							// A new prefix is started.
							outputFile.write((key.length() + prefix + "*" + suffix).getBytes());

							// The dict is now fresh
							fcdict = null;
						} else {
							suffix = keyarray.get(i).replace(prefix, "").trim();
							outputFile.write((suffix.length() + "$" + suffix).getBytes());

						}
					} else {
						fcdict = null;
						key = keyarray.get(i);
						fcdict = new Trie();
						fcdict.insert(key);
						if (i < (keyarray.size() - 1) && fcdict.getMatchingPrefix(keyarray.get(i + 1)).length() > 0) {
							prefix = fcdict.getMatchingPrefix(keyarray.get(i + 1));
							suffix = keyarray.get(i).replace(prefix, "").trim();
							outputFile.write((key.length() + prefix + "*" + suffix).getBytes());

						} else {
							outputFile.write((key.length() + key).getBytes());
						}

					}

				} else if (key.length() > 3 && keyarray.get(i).length() > 3 && fcdict == null) {
					fcdict = new Trie();
					fcdict.insert(key);

					if (fcdict.getMatchingPrefix(keyarray.get(i)).length() > 0) {
						prefix = fcdict.getMatchingPrefix(keyarray.get(i));
						if (!oldprefix.equals(prefix) && oldprefix.contains(prefix)) {
							key = keyarray.get(i);
							suffix = keyarray.get(i).replace(prefix, "").trim();
							outputFile.write((key.length() + prefix + "*").getBytes());
							outputFile.write((suffix.length() + "$" + suffix).getBytes());
							fcdict = null;
						} else {
							suffix = keyarray.get(i).replace(prefix, "").trim();
							outputFile.write((suffix.length() + "$" + suffix).getBytes());

						}
					} else {
						fcdict = null;
						key = keyarray.get(i);
						fcdict = new Trie();
						fcdict.insert(key);
						if (i < (keyarray.size() - 1) && fcdict.getMatchingPrefix(keyarray.get(i + 1)).length() > 0) {
							prefix = fcdict.getMatchingPrefix(keyarray.get(i + 1));
							suffix = keyarray.get(i).replace(prefix, "").trim();
							outputFile.write((key.length() + prefix + "*").getBytes());
							outputFile.write((suffix).getBytes());

						} else {
							outputFile.write((key.length() + key).getBytes());
						}

					}
				} else {
					key = keyarray.get(i);
					outputFile.write((key.length() + key).getBytes());
					fcdict = null;
				}
				oldprefix = prefix;
				// Storing the location for the Random Access File, so as to
				// seek later.
				String p = Long.toString(outputFile.getFilePointer());
				pointer.add(p);
			}

			Iterator iter = stemIndexCompressed.entrySet().iterator();
			DictionaryDetailBytes detailbytes = new DictionaryDetailBytes();

			for (int j = 0; j < pointer.size(); j++) {
				outputFile.write((pointer.get(j) + "-").getBytes());
				if (iter.hasNext()) {
					Entry e = (Entry) iter.next();
					detailbytes = (DictionaryDetailBytes) e.getValue();
					outputFile.write(("" + detailbytes.DOCFREQ).getBytes());
					Iterator its = detailbytes.postingList.iterator();
					while (its.hasNext()) {
						PostingEntry e1 = (PostingEntry) its.next();

						outputFile.write(e1.docId);

						byte[] c = ("-").getBytes();

						outputFile.write(c);
						outputFile.write(e1.frequency);

					}
				}
				outputFile.write(System.getProperty("line.separator").getBytes());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static long getCompressedPostingListSize(List<PostingEntry> postingList) {
		long length = 0;
		for (PostingEntry postingEntry : postingList) {
			length += postingEntry.docId.length + postingEntry.frequency.length;
		}
		return length;
	}

	static long GetFileSize(String FileName) {
		File f = new File(FileName);
		return f.length();

	}

	private static byte[] convertToByteArray(String gammacode) {
		BitSet bitSet = new BitSet(gammacode.length());
		for (int i = 0; i < gammacode.length(); i++) {
			Boolean value = gammacode.charAt(i) == '1' ? true : false;
			bitSet.set(i, value);
		}
		return bitSet.toByteArray();
	}

	private static String getGammaCodes(int frequency) {
		String binary = Integer.toBinaryString(frequency);
		String gammaCode = "";
		binary = binary.substring(1);
		for (int i = 0; i < binary.length(); i++) {
			gammaCode = gammaCode + "1";
		}
		gammaCode = gammaCode + "0";
		gammaCode = gammaCode + binary;
		return gammaCode;
	}

	private static String getDeltaCodes(int documentID) {
		String deltaCode = "";
		String binary = Integer.toBinaryString(documentID);
		String gammaCode = getGammaCodes(binary.length());
		binary = binary.substring(1);
		deltaCode = gammaCode + binary;
		return deltaCode;
	}

	static class PostingEntry implements Serializable {
		byte[] docId;
		byte[] frequency;

		public PostingEntry(byte[] docID, byte[] frequency) {
			this.docId = docID;
			this.frequency = frequency;
		}

		public PostingEntry() {

		}
	}

	static class DictionaryDetailBytes implements Serializable {
		String term;
		Integer DOCFREQ;
		// private TreeMap<byte[], byte[]> postingList;

		List<PostingEntry> postingList;

		public DictionaryDetailBytes() {

		}

		public DictionaryDetailBytes(String term, Integer docfreq, List<PostingEntry> postingList) {
			this.term = term;
			this.DOCFREQ = docfreq;
			this.postingList = postingList;
		}
	}

	static class DocumentDetailBytes implements Serializable {
		String DOC_ID;
		int MAX_TF;
		int DOCLEN;
		TreeMap<byte[], byte[]> termFrequencyList = new TreeMap<byte[], byte[]>();

		public void setDocId(String docId) {
			this.DOC_ID = docId;
		}

		public void setMaxTf(Integer maxTf) {
			this.MAX_TF = maxTf;
		}

		public void setDocLen(Integer docLen) {
			this.DOCLEN = docLen;
		}

		public void setTermFrequencyList(TreeMap<byte[], byte[]> tfList) {
			this.termFrequencyList = tfList;
		}
	}
}

/* Implemented Prefix Encoding */
class TrieNode {
	public TrieNode(char ch) {
		value = ch;
		children = new HashMap<>();
		bIsEnd = false;
	}

	public HashMap<Character, TrieNode> getChildren() {
		return children;
	}

	public char getValue() {
		return value;
	}

	public void setIsEnd(boolean val) {
		bIsEnd = val;
	}

	public boolean isEnd() {
		return bIsEnd;
	}

	private char value;
	private HashMap<Character, TrieNode> children;
	private boolean bIsEnd;
}

// Implements the actual Trie
class Trie {
	// Constructor
	public Trie() {
		root = new TrieNode((char) 0);
	}

	// Method to insert a new word to Trie
	public void insert(String word) {

		// Find length of the given word
		int length = word.length();
		TrieNode crawl = root;

		// Traverse through all characters of given word
		for (int level = 0; level < length; level++) {
			HashMap<Character, TrieNode> child = crawl.getChildren();
			char ch = word.charAt(level);

			// If there is already a child for current character of given word
			if (child.containsKey(ch))
				crawl = child.get(ch);
			else // Else create a child
			{
				TrieNode temp = new TrieNode(ch);
				child.put(ch, temp);
				crawl = temp;
			}
		}

		// Set IsEnd true for last character
		crawl.setIsEnd(true);
	}

	// The main method that finds out the longest string 'input'
	public String getMatchingPrefix(String input) {
		String result = ""; // Initialize resultant string
		int length = input.length(); // Find length of the input string

		// Initialize reference to traverse through Trie
		TrieNode crawl = root;

		// Iterate through all characters of input string 'str' and traverse
		// down the Trie
		int level, prevMatch = 0;
		for (level = 0; level < length; level++) {
			// Find current character of str
			char ch = input.charAt(level);

			// HashMap of current Trie node to traverse down
			HashMap<Character, TrieNode> child = crawl.getChildren();

			// See if there is a Trie edge for the current character
			if (child.containsKey(ch)) {
				result += ch; // Update resultz
				crawl = child.get(ch); // Update crawl to move down in Trie

				// If this is end of a word, then update prevMatch
				// if( crawl.isEnd() )
				prevMatch = level + 1;
			} else
				break;
		}

		// If the last processed character did not match end of a word,
		// return the previously matching prefix
		if (!crawl.isEnd() && prevMatch > 3)
			return result.substring(0, prevMatch);

		else if (crawl.isEnd() && prevMatch > 3)
			return result;
		else
			return "";
	}

	private TrieNode root;
}