

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

public class NaiveBayes {
	
	public static void main(String[] args) {
		
//////////////////////////////////////////////////////////////////////////////
/// FILES
//////////////////////////////////////////////////////////////////////////////
		
		// Train-files.txt: Contains "folder/emailTitleNumber"
		String train_files = "train-files.txt";	
		
		// Folder/Classes containing "word count"
		String articles = "articles.train.txt";
		String corporate = "corporate.train.txt";
		String enron_t_s = "enron_t_s.train.txt";
		String enron_travel_club = "enron_travel_club.train.txt";
		String hea_nesa = "hea_nesa.train.txt";
		String personal = "personal.train.txt";
		String systems = "systems.train.txt";
		String tw_commercial_group = "tw_commercial_group.train.txt";
						
		// Vocabulary.txt: Contains all words and their counts over the whole corpus
		// "word count"
		String vocabulary_file = "vocabulary.txt";
				
		
//////////////////////////////////////////////////////////////////////////////
/// DATA STRUCTURES
//////////////////////////////////////////////////////////////////////////////
		// Map linking the different folders/classes to their label number
				HashMap<String, Integer> folderToLabel = new HashMap<String, Integer>();
				folderToLabel.put("articles", 0);
				folderToLabel.put("corporate", 1);
				folderToLabel.put("enron_t_s", 2);
				folderToLabel.put("enron_travel_club", 3);
				folderToLabel.put("hea_nesa", 4);
				folderToLabel.put("personal", 5);
				folderToLabel.put("systems", 6);
				folderToLabel.put("tw_commercial_group", 7);
		
		
		// Map representation of the vocabulary
		// This is our "V"
		HashMap<String, Integer> vocabulary = generateVocabulary(vocabulary_file);
		int vocabCount = vocabulary.keySet().size();
		// This will be our blank HashMap template that we will use to fill our
		HashMap<String, Double> wordBank = generateWordBank(vocabulary_file);
		
		
		// This will contain our "Sy's" (number of emails in each folder)
		HashMap<String, Integer> folderEmailCounts = new HashMap<String, Integer>();
		folderEmailCounts.put("articles", getClassEmailCount("articles"));
		folderEmailCounts.put("corporate", getClassEmailCount("corporate"));
		folderEmailCounts.put("enron_t_s", getClassEmailCount("enron_t_s"));
		folderEmailCounts.put("enron_travel_club", getClassEmailCount("enron_travel_club"));
		folderEmailCounts.put("hea_nesa", getClassEmailCount("hea_nesa"));
		folderEmailCounts.put("personal", getClassEmailCount("personal"));
		folderEmailCounts.put("systems", getClassEmailCount("systems"));
		folderEmailCounts.put("tw_commercial_group", getClassEmailCount("tw_commercial_group"));
		
		
		double totalEmailCount = 0;
		for (String folder : folderEmailCounts.keySet()) {
			totalEmailCount = totalEmailCount + folderEmailCounts.get(folder);
		}

		// This will contain our priors for each folder/class
		// "Sy / S"
		HashMap<String, Double> priors = new HashMap<String, Double>();
		priors.put("articles", Math.log(folderEmailCounts.get("articles")/totalEmailCount));
		priors.put("corporate", Math.log(folderEmailCounts.get("corporate")/totalEmailCount));
		priors.put("enron_t_s", Math.log(folderEmailCounts.get("enron_t_s")/totalEmailCount));
		priors.put("enron_travel_club", Math.log(folderEmailCounts.get("enron_travel_club")/totalEmailCount));
		priors.put("hea_nesa", Math.log(folderEmailCounts.get("hea_nesa")/totalEmailCount));
		priors.put("personal", Math.log(folderEmailCounts.get("personal")/totalEmailCount));
		priors.put("systems", Math.log(folderEmailCounts.get("systems")/totalEmailCount));
		priors.put("tw_commercial_group", Math.log(folderEmailCounts.get("tw_commercial_group")/totalEmailCount));
		
		
		// Conditional probabilities
		HashMap<String, HashMap<String, Double>> folderToWordCounts = new HashMap<String, HashMap<String, Double>>();
		folderToWordCounts.put("articles", getWordCounts(articles, wordBank));
		folderToWordCounts.put("corporate", getWordCounts(corporate, wordBank));
		folderToWordCounts.put("enron_t_s", getWordCounts(enron_t_s, wordBank));
		folderToWordCounts.put("enron_travel_club", getWordCounts(enron_travel_club, wordBank));
		folderToWordCounts.put("hea_nesa", getWordCounts(hea_nesa, wordBank));
		folderToWordCounts.put("personal", getWordCounts(personal, wordBank));
		folderToWordCounts.put("systems", getWordCounts(systems, wordBank));
		folderToWordCounts.put("tw_commercial_group", getWordCounts(tw_commercial_group, wordBank));
		
		// Creates a map linking each folder to a map of words --> conditional probabilities
		HashMap<String, HashMap<String, Double>> folderToWordCondProbs = new HashMap<String, HashMap<String, Double>>();
		for (String folder : folderToWordCounts.keySet()) {
			folderToWordCondProbs.put(folder, calculateCondProbs(folderToWordCounts.get(folder), vocabCount));
		}

		
		// Make predictions
		getPredictions("test.txt", priors, folderToWordCondProbs, folderToLabel, vocabulary);
		
	}
	
	
	// Takes in the file for the vocabulary containing all the words in the entire corpus
	// and their total counts
	// Will convert it into a map of word --> total count
	public static HashMap<String, Integer> generateVocabulary(String vocabulary_file) {
		HashMap<String, Integer> vocabulary = new HashMap<String, Integer>();
		try {
			// Reads the vocabulary file, adds to map of word-to-totalCount
			BufferedReader br = new BufferedReader(new FileReader(vocabulary_file));
			String line = null;
			while ((line = br.readLine()) != null) {
				String word = line.split("\\s+")[0];
				int count = Integer.parseInt(line.split("\\s+")[1]);
				vocabulary.put(word, count);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return vocabulary;
	}
	
	// Will read through a file which contains the list of emails
		// and return the total number of emails 
		public static int getClassEmailCount(String folder) {
			int totalEmailCount = 0;
			try {
				BufferedReader br = new BufferedReader(new FileReader("train-files.txt"));
				String line = null;
				while ((line = br.readLine()) != null) {
					String foldername = line.split("/")[0];
					if (folder.equals(foldername)) {
						totalEmailCount++;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return totalEmailCount;
		}

	// Generates map of all vocab words with counts initialized to zero
	public static HashMap<String, Double> generateWordBank(String vocabulary_file) {
		HashMap<String, Double> wordBank = new HashMap<String, Double>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(vocabulary_file));
			String line = null;
			while ((line = br.readLine()) != null) {
				String word = line.split("\\s+")[0];
				wordBank.put(word, 0.0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return wordBank;
	}
	
	
	// calculates Naive Bayes output for each folder
		public static HashMap<String,Double> scoreLines(String line, HashMap<String, Double> priors, 
				HashMap<String, HashMap<String, Double>> folderToWordCondProbs, 
				HashMap<String,Integer> folderToLabel, HashMap<String,Integer> vocabulary) {
			// splits line up into words
			String[] words = line.split("\\s+");
			HashMap<String,Double> finalScore = new HashMap<String,Double>();
			// for each folder
			for (String folder : folderToLabel.keySet()) {
				// get the prior for that folder
				double score = priors.get(folder);
				
				// Iterate through the words in the line
				// Multiplying the score by the conditional probability of the word in the given
				// folder/label
				for (int i = 1; i < words.length; i++) {
					score = score + folderToWordCondProbs.get(folder).get(words[i]);
				}
				finalScore.put(folder, score);
			}
			return finalScore;
		}
	
	
	// Will read through a file which contains the list of emails
	// and return the total number of emails 
	public static int getEmailCount(String file) {
		int totalEmailCount = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = br.readLine()) != null)
				totalEmailCount++;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return totalEmailCount;
	}
	
// Will calculate the conditional probabilties for each word in a folder and return
	// a map storing those probabilities
	public static HashMap<String, Double> calculateCondProbs(HashMap<String, Double> folderWordCounts, int vocabSize) {
		HashMap<String, Double> condProbs = new HashMap<String, Double>();
		double totalWordCount = 0;
		// Counts the total word count in the folder (|Cy|)
		for (String word : folderWordCounts.keySet()) {
			totalWordCount = totalWordCount + folderWordCounts.get(word);
		}
		//System.out.println(totalWordCount);
		for (String word : folderWordCounts.keySet()) {
			double probability = Math.log((folderWordCounts.get(word) + 1) / (totalWordCount + vocabSize));
			condProbs.put(word, probability);
		}
		return condProbs;
	}
		
	// Will take a file/folder and a blank word bank
	// Will iterate through folder's words and record their counts in a map
	// of Word --> Count
	public static HashMap<String, Double> getWordCounts(String file, HashMap<String, Double> wordCounts) {
		HashMap<String, Double> results = new HashMap<String, Double>();
		for (String word : wordCounts.keySet()) {
			results.put(word, 0.0);
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] wordAndCount = line.split("\\s+");
				results.put(wordAndCount[0], (double) Integer.parseInt(wordAndCount[1]));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}
	
	
	public static void getPredictions(String test_file, HashMap<String, Double> priors, 
			HashMap<String, HashMap<String, Double>> folderToWordCondProbs, 
			HashMap<String,Integer> folderToLabel, HashMap<String,Integer> vocabulary) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(test_file));
			String line = null;
			FileWriter output = new FileWriter("predictions.nb");
			// Iterate through each line of the test file
			while ((line = br.readLine()) != null) {
				double max = Double.NEGATIVE_INFINITY;
				// Get Naive Bayes scores for it
				HashMap<String,Double> results = scoreLines(line, priors, folderToWordCondProbs, 
						folderToLabel, vocabulary);
				// No initial label
				String resultFolder = null;
				//  Go through each of the results, and keep track of max 
				for (String folder : results.keySet()) {
					if (results.get(folder).doubleValue() > max) {
						max = results.get(folder).doubleValue();
						resultFolder = folder;
					}
				}
				// Output into file
				System.out.println(resultFolder + " " + (folderToLabel.get(resultFolder) + 1) + ".0");
				output.write(folderToLabel.get(resultFolder) + 1 + ".0\n");
			}
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	
}
