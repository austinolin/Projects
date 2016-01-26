import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.io.FileWriter;


public class LogisticSGD {

    // Learning rate
    public static double rate = .0001;

    static double weight0 = 0.0;

    // iterations
    public static int t = 100;
    
    public static int vocabularySize = 6385; // Should end up as 6385

   // static HashMap<Double, String> vocabulary = new HashMap<Double, String>();
    public static HashMap<Integer, Double[]> emailToWords = new HashMap<Integer, Double[]>();
    public static HashMap<Integer, Double> emailToLabel = new HashMap<Integer, Double>();
    static ArrayList<Integer> emails = new ArrayList<Integer>();
    public static HashMap<Double, Double> wordToWeights = new HashMap<Double, Double>();
    
    //For test file
    public static HashMap<Integer, Double[]> testEmailToWords = new HashMap<Integer, Double[]>();
    public static HashMap<Integer, Double> testEmailToLabel = new HashMap<Integer, Double>();
    public static HashMap<Integer, Double> testEmailToResult = new HashMap<Integer, Double>();


    private static double sigmoid(double z) {
    	return 1.0 / (1.0 + Math.exp(z));
    }

    public static void createTrainingInfo() {
        for (int n = 0; n < t; n++) {
        	// Shuffle emails
        	Collections.shuffle(emails);
            //double lik = 0.0;
            for (int i=0; i< emails.size(); i++) {
            	// words contained in email
                Double[] x = emailToWords.get(emails.get(i));
                // Get prediction using sigmoid
                double predicted = classify(x);
                // actual label
                double label = emailToLabel.get(emails.get(i));
                // corporate == 2 == 1
                // personal == 6 == 0
                if (label == 2.0) {
                	label = 1;
                } else {
                	label = 0;
                }
                for (int j = 0; j < x.length; j++) {
                	// Update the weights for each word
                	wordToWeights.put(x[j], (wordToWeights.get(x[j]) + rate * (label - predicted)));// * x[j]); always 1, so no need
                }
                weight0 = weight0 + (label-predicted);
            }
        }
    }

    private static double classify(Double[] x) {
        double logit = .0;
        for (int i = 0; i < x.length; i++)  {
            logit += (double) wordToWeights.get(x[i]); //* x[i]; // no need to multiply by x[i] since it will always be one.
        }
        return 1.0 / (1.0 + Math.exp((-1 * logit) - weight0));
        		
        		//sigmoid((-1 * logit) - weight0);
    }
    

    // Creates initial list of all words and their weights at 0.0
    public static void createWeightList() {
    	try {
    		BufferedReader br = new BufferedReader(new FileReader("features.lexicon"));
    		String line = null;
    		while ((line = br.readLine()) != null) {
    			String[] splitLine = line.split("\\s+");
    			wordToWeights.put(Double.parseDouble(splitLine[1]), 0.0);
    		}
    	}catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    
    public static void readFile(String file) {
		try {
			// Reads the vocabulary file, adds to map of word-to-totalCount
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;
			int emailCount = 0;
			while ((line = br.readLine()) != null) {
				emailCount++;
				String[] emailLine = line.split("\\s+"); // First is label, rest are "word:strength"
				double label = Double.parseDouble(emailLine[0]);
				emailToLabel.put(emailCount, label); // add email and label to map
				Double[] words = new Double[emailLine.length - 1];
				for (int i = 1; i < emailLine.length; i++) {
					String[] wordAndStrength = emailLine[i].split(":");
					words[i-1] = Double.parseDouble(wordAndStrength[0]);
				}
				
				emailToWords.put(emailCount, words); // add email and list of words to map
				emails.add(emailCount); // so we'll have a list of emails in order to shuffle
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    public static void readTestFile(String file) {
		try {
			// Reads the vocabulary file, adds to map of word-to-totalCount
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;
			int emailCount = 0;
			while ((line = br.readLine()) != null) {
				emailCount++;
				String[] emailLine = line.split("\\s+"); // First is label, rest are "word:strength"
				double label = Double.parseDouble(emailLine[0]);
				testEmailToLabel.put(emailCount, label); // add email and label to map
				Double[] words = new Double[emailLine.length - 1];
				for (int i = 1; i < emailLine.length; i++) {
					String[] wordAndStrength = emailLine[i].split(":");
					words[i-1] = Double.parseDouble(wordAndStrength[0]);
				}
				testEmailToWords.put(emailCount, words); // add email and list of words to map
				testEmailToResult.put(emailCount, 0.0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public static void main(String... args) throws FileNotFoundException {
    	try {
    	createWeightList();
    	readFile("train.libsvm");
        vocabularySize = wordToWeights.size();
        createTrainingInfo(); // go through training data to learn
        readTestFile("test.libsvm");
        FileWriter output = new FileWriter("predictions.lr");
        
        for (int email : testEmailToResult.keySet()) {
        	
        	testEmailToResult.put(email, classify(testEmailToWords.get(email)));
        	double result = testEmailToResult.get(email);
        	System.out.println("Email: " + email);
        	System.out.println("Label: " + testEmailToLabel.get(email));
        	System.out.println("Prediction: " + result);
        	if (result >= .5) {
        		result = 2.0;
        	} else {
        		result = 6.0;
        	}
        	System.out.println ("Predicted Label: " + result);
        	System.out.println();
        	output.write(result + "\n");
        	
        	
        }
        output.close();
    	} 
    	catch (Exception e) {
			e.printStackTrace();
		}

    }
}