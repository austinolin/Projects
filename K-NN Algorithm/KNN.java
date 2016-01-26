// Austin Olin
// KNN Algorithm
// PS1
// Assumptions: In case of ties, will randomly select one of the tied up choices

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class KNN {

	static ArrayList<String[]> instances = new ArrayList<String[]>();
	static ArrayList<String> results = new ArrayList<String>();
	static ArrayList<String[]> queries = new ArrayList<String[]>();
	static ArrayList<String> queryResults = new ArrayList<String>();
	

		
		public static void main(String[] args) throws IOException {
			int k = Integer.parseInt(args[0]);
			String filename = "results_" + k + "_" + args[2];
			PrintWriter out = new PrintWriter(filename);
			String training = args[1];
			String testing = args[2];
			
			// Parse through Training
			for (String line : Files.readAllLines(Paths.get(training))) {
				String[] attributes = line.split(",");
				String[] instance = new String[attributes.length - 1];
				for (int i=0; i < attributes.length - 1; i++) {
					instance[i] = attributes[i];
				}
				instances.add(instance);
				results.add(attributes[attributes.length - 1]);
			} 
			List<Label_Attributes> labelList = new ArrayList<Label_Attributes>();
			List<Label_Distance> resultList = new ArrayList<Label_Distance>();
			for (int i = 0; i < instances.size(); i++) {
				labelList.add(new Label_Attributes(instances.get(i), results.get(i)));
			}
			
			// Parse through Testing
			for (String line : Files.readAllLines(Paths.get(testing))) {
				String[] attributes = line.split(",");
				String[] query = new String[attributes.length - 1];
				for (int i=0; i < attributes.length - 1; i++) {
					query[i] = attributes[i];
				}
				queries.add(query);
				queryResults.add(attributes[attributes.length - 1]);
			}
			
			
			//For each of the queries, find the distances, find the majority label,
			// and print results in new file
			for(int i = 0; i < queries.size(); i++) {
				String[] query = queries.get(i);
				for(Label_Attributes label : labelList){
					double dist = 0.0;  
					for(int j = 0; j < label.labelAttributes.length; j++){ 
						if (isDouble(label.labelAttributes[j])) {
							dist += Math.pow(Double.parseDouble(label.labelAttributes[j]) - Double.parseDouble(query[j]), 2) ;
						} else {
							if (!(label.labelAttributes[j].equals(query[j]))) {
								dist += 1;
							} else {
								//distance is zero, so no change
							}
						} 	     
					}
					double distance = Math.sqrt(dist);
					resultList.add(new Label_Distance(distance,label.labelName));
				} 
	
				Collections.sort(resultList, new DistanceComparator());
				String[] topKResults = new String[k];
				for(int x = 0; x < k; x++){
					topKResults[x] = resultList.get(x).labelName;
				}
				String majClass = decideLabel(topKResults);
				System.out.println("Test " + i);
				System.out.println("Algorithm says: "+majClass);
				System.out.println("Testing says: " +queryResults.get(i));
				
				for (String attr : queries.get(i)) {
					out.print(attr + ",");
				}
				out.print(queryResults.get(i) + ",");
				out.println(majClass);
			}
			out.close();	

		}
		
		// Class representing a single line of attributes and its label.
		// Will store the attributes in String form, as well as the label 
		static class Label_Attributes {	
			String[] labelAttributes;
			String labelName;
			
			public Label_Attributes(String[] labelAttributes, String labelName){
				this.labelName = labelName;
				this.labelAttributes = labelAttributes;	    	    
			}
		}
		
		// Class that will represent a result of the KNN distance calculation
		// Will store the label of the line of attributes, and the computed distance
		static class Label_Distance {	
			String labelName;
			double distance;
			
			public Label_Distance(double distance, String labelName){
				this.labelName = labelName;
				this.distance = distance;	    	    
			}
		}
		
		
		// Returns true if the given string is a double
		// Source: http://stackoverflow.com/questions/3133770/how-to-find-out-if-the-value-contained-in-a-string-is-double-or-not
		static boolean isDouble(String str) {
	        try {
	            Double.parseDouble(str);
	            return true;
	        } catch (NumberFormatException e) {
	            return false;
	        }
	    }
		
		// A comparator for distance
				static class DistanceComparator implements Comparator<Label_Distance> {
					@Override
					public int compare(Label_Distance a, Label_Distance b) {
						if (a.distance < b.distance) {
							return -1;
						} else if (a.distance == b.distance) {
							return 0;
						} else {
							return 1;
						}
					}
				}
		
		// Will go through the top K results, find the one with the highest count
		// and return it.
		// In case of ties, will randomly choose amongst the tied choices
		private static String decideLabel(String[] topKResults) {
			//makes all values unique
			Set<String> tempRidOfDuplicates = new HashSet<String>(Arrays.asList(topKResults));
			//convert the HashSet back to array
			String[] uniqueResults = tempRidOfDuplicates.toArray(new String[0]);
			//counts for unique strings
			int[] counts = new int[uniqueResults.length];  
			
			for (int i = 0; i < uniqueResults.length; i++) {
				for (int j = 0; j < topKResults.length; j++) {
					if(topKResults[j].equals(uniqueResults[i])){
						counts[i]++;
					}
				}        
			}

			// Find the highest count(s)
			// Also keep track of if there are ties
			int maxCount = counts[0];
			int ties = 0;
			int maxIndex = 0;
			String maxLabel = uniqueResults[0];
			
			// Will go through and find the label that has the highest number
			// of appearances and record that number
			for (int i = 1; i < counts.length; i++) {
				if (maxCount < counts[i]) {
					maxCount = counts[i];
					maxIndex = i;
					maxLabel = uniqueResults[i];
				}
			}

			// Since there can be ties, we also go through the different counts
			// and count how many times that maxCount value appears.
			for (int i = 0; i < counts.length; i++) {
				if (counts[i] == maxCount) {
					ties++;
				}
			}
			
			// If there is a tie, we will get the indices and randomly select
			// one of them
			if (ties > 1) {
				ArrayList<Integer> tieIndices = new ArrayList<Integer>();
				for (int i = 0; i < counts.length; i++) {
					if (counts[i] == maxCount) {
						tieIndices.add(i);
					}
				}
				Random r = new Random();        
				int tieWinnerIndex = tieIndices.get(r.nextInt(tieIndices.size()));
				maxLabel = uniqueResults[tieWinnerIndex];
			}
			return maxLabel;

		}
}
