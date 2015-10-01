import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;


/** R-precision,
 *  Average Precision,
 *   nDCG,
 *    precision@k and
 *     recall@k and
 *      F1@k (k=5,10, 20, 50, 100).
 * @author Austin
 *
 */
public class Trec_Eval {
	
	static Integer numReleventQ1 = 0;
	static Integer numReleventQ2 = 0;
	static Integer numReleventQ3 = 0;

	BufferedReader qrel = null;
	static Map<String, String> q1 = new HashMap<String, String>();
	static Map<String, String> q2 = new HashMap<String, String>();
	static Map<String, String> q3 = new HashMap<String, String>();
	
	SortedSet sortq1;
	SortedSet sortq2;
	SortedSet sortq3;
	
	static Map<Integer, Double> precQ1 = new HashMap<Integer, Double>();
	static Map<Integer, Double> precQ2 = new HashMap<Integer, Double>();
	static Map<Integer, Double> precQ3 = new HashMap<Integer, Double>();
	
	static Map<Integer, Double> recallQ1 = new HashMap<Integer, Double>();
	static Map<Integer, Double> recallQ2 = new HashMap<Integer, Double>();
	static Map<Integer, Double> recallQ3 = new HashMap<Integer, Double>();
	
	static Map<Integer, Double> aPrecQ1 = new HashMap<Integer, Double>();
	static Map<Integer, Double> aPrecQ2 = new HashMap<Integer, Double>();
	static Map<Integer, Double> aPrecQ3 = new HashMap<Integer, Double>();
	
	static Map<Integer, Double> rprecQ1 = new HashMap<Integer, Double>();
	static Map<Integer, Double> rprecQ2 = new HashMap<Integer, Double>();
	static Map<Integer, Double> rprecQ3 = new HashMap<Integer, Double>();
	
	static Map<Integer, Double> f1Q1 = new HashMap<Integer, Double>();
	static Map<Integer, Double> f1Q2 = new HashMap<Integer, Double>();
	static Map<Integer, Double> f1Q3 = new HashMap<Integer, Double>();
	
	static Map<Integer, Double> dcgQ1 = new HashMap<Integer, Double>();
	static Map<Integer, Double> dcgQ2 = new HashMap<Integer, Double>();
	static Map<Integer, Double> dcgQ3 = new HashMap<Integer, Double>();
	
	static Map<Integer, Double> ndcgQ1 = new HashMap<Integer, Double>();
	static Map<Integer, Double> ndcgQ2 = new HashMap<Integer, Double>();
	static Map<Integer, Double> ndcgQ3 = new HashMap<Integer, Double>();
	
	static Map<String, Double> query1Final = new HashMap<String, Double>();
	static Map<String, Double> query2Final = new HashMap<String, Double>();
	static Map<String, Double> query3Final = new HashMap<String, Double>();
	static Map<String, Double> queryAverages = new HashMap<String, Double>();
	
	static String resultsFile;
	
	
	
	public Trec_Eval(String resultsFile, String qrelFile, boolean averages) throws IOException {
		qrel = new BufferedReader(new FileReader("C:/Users/Austin/workspace/HW3/qrel_results.txt"));//qrelFile));
		this.resultsFile = "C:/Users/Austin/workspace/HW3/testFile.txt"; //resultsFile;

		handleQrel(qrel);
		getPrecisions();
		getRecalls();
		getRPrecisions();
		getF1();
		getDCG();
		getNDCG();
		
		
		computeFinals(1);
		computeFinals(2);
		computeFinals(3);
		computeAverages();
		
//		if (averages) {
//			System.out.println("Query 1");
//			SortedSet<String> keys1 = new TreeSet<String>(query1Final.keySet());
//			for (String key : keys1) { 
//				System.out.println(key + query1Final.get(key));
//			}
//			System.out.println("");
//			System.out.println("Query 2");
//			SortedSet<String> keys2 = new TreeSet<String>(query2Final.keySet());
//			for (String key : keys2) { 
//				System.out.println(key + query2Final.get(key));
//			}
//			System.out.println("");
//			System.out.println("Query 3");
//			SortedSet<String> keys3 = new TreeSet<String>(query3Final.keySet());
//			for (String key : keys3) { 
//				System.out.println(key + query3Final.get(key));
//			}
//			System.out.println("");
//		}
//		System.out.println("Query Averages");
//		
//		SortedSet<String> keysAll = new TreeSet<String>(queryAverages.keySet());
//		for (String key : keysAll) { 
//			System.out.println(key + queryAverages.get(key));
//		}
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("Q1 Precisions");
		for (int i = 1; i <= 200; i++) {
			System.out.println(precQ1.get(i));
		}
		System.out.println("");
		System.out.println("Q1 Recalls");
		for (int i = 1; i <= 200; i++) {
			System.out.println(recallQ1.get(i));
		}
		System.out.println("");
		System.out.println("Q2 Precisions");
		for (int i = 1; i <= 200; i++) {
			System.out.println(precQ2.get(i));
		}
		System.out.println("");
		System.out.println("Q2 Recalls");
		for (int i = 1; i <= 200; i++) {
			System.out.println(recallQ2.get(i));
		}
		System.out.println("");
		System.out.println("Q3 Precisions");
		for (int i = 1; i <= 200; i++) {
			System.out.println(precQ3.get(i));
		}
		
		System.out.println("");
		System.out.println("Q3 Recalls");
		for (int i = 1; i <= 200; i++) {
			System.out.println(recallQ3.get(i));
		}

		
		
		
		
	}
	
	private void computeAverages() {
		queryAverages.put("Precision @ 5: ", (double) 0);
		queryAverages.put("Precision @ 10: ", (double) 0);
		queryAverages.put("Precision @ 20: ", (double) 0);
		queryAverages.put("Precision @ 50: ", (double) 0);
		queryAverages.put("Precision @ 100: ", (double) 0);
		
		queryAverages.put("Recall @ 5: ", (double) 0);
		queryAverages.put("Recall @ 10: ", (double) 0);
		queryAverages.put("Recall @ 20: ", (double) 0);
		queryAverages.put("Recall @ 50: ", (double) 0);
		queryAverages.put("Recall @ 100: ", (double) 0);
		
		queryAverages.put("Average Precision @ 5: ", (double) 0);
		queryAverages.put("Average Precision @ 10: ", (double) 0);
		queryAverages.put("Average Precision @ 20: ", (double) 0);
		queryAverages.put("Average Precision @ 50: ", (double) 0);
		queryAverages.put("Average Precision @ 100: ", (double) 0);
		
		queryAverages.put("F1 @ 5: ", (double) 0);
		queryAverages.put("F1 @ 10: ", (double) 0);
		queryAverages.put("F1 @ 20: ", (double) 0);
		queryAverages.put("F1 @ 50: ", (double) 0);
		queryAverages.put("F1 @ 100: ", (double) 0);
		
		queryAverages.put("NCDG @ 5: ", (double) 0);
		queryAverages.put("NCDG @ 10: ", (double) 0);
		queryAverages.put("NCDG @ 20: ", (double) 0);
		queryAverages.put("NCDG @ 50: ", (double) 0);
		queryAverages.put("NCDG @ 100: ", (double) 0);
		
		queryAverages.put("R-Precision: ", (double) 0);
		
		for (String k : query1Final.keySet()) {
			queryAverages.put(k, queryAverages.get(k) + query1Final.get(k));
		}
		for (String k : query2Final.keySet()) {
			queryAverages.put(k, queryAverages.get(k) + query2Final.get(k));
		}
		for (String k : query3Final.keySet()) {
			//System.out.println(query3Final.get(k));
			queryAverages.put(k, queryAverages.get(k) + query3Final.get(k));
		}
		for (String k : queryAverages.keySet()) {
			queryAverages.put(k, queryAverages.get(k) / 3);
		}
		
		
	}

	private void computeFinals(int query) {
		if (query == 1) {
			query1Final.put("Precision @ 5: ", precQ1.get(5));
			query1Final.put("Precision @ 10: ", precQ1.get(10));
			query1Final.put("Precision @ 20: ", precQ1.get(20));
			query1Final.put("Precision @ 50: ", precQ1.get(50));
			query1Final.put("Precision @ 100: ", precQ1.get(100));
			
			query1Final.put("Recall @ 5: ", recallQ1.get(5));
			query1Final.put("Recall @ 10: ", recallQ1.get(10));
			query1Final.put("Recall @ 20: ", recallQ1.get(20));
			query1Final.put("Recall @ 50: ", recallQ1.get(50));
			query1Final.put("Recall @ 100: ", recallQ1.get(100));
			
			query1Final.put("Average Precision @ 5: ", aPrecQ1.get(5));
			query1Final.put("Average Precision @ 10: ", aPrecQ1.get(10));
			query1Final.put("Average Precision @ 20: ", aPrecQ1.get(20));
			query1Final.put("Average Precision @ 50: ", aPrecQ1.get(50));
			query1Final.put("Average Precision @ 100: ", aPrecQ1.get(100));
			
			query1Final.put("F1 @ 5: ", f1Q1.get(5));
			query1Final.put("F1 @ 10: ", f1Q1.get(10));
			query1Final.put("F1 @ 20: ", f1Q1.get(20));
			query1Final.put("F1 @ 50: ", f1Q1.get(50));
			query1Final.put("F1 @ 100: ", f1Q1.get(100));
			
			query1Final.put("NCDG @ 5: ", ndcgQ1.get(5));
			query1Final.put("NCDG @ 10: ", ndcgQ1.get(10));
			query1Final.put("NCDG @ 20: ", ndcgQ1.get(20));
			query1Final.put("NCDG @ 50: ", ndcgQ1.get(50));
			query1Final.put("NCDG @ 100: ", ndcgQ1.get(100));
			
			for (Integer i : rprecQ1.keySet()) {
				query1Final.put("R-Precision: ", rprecQ1.get(i));
			}
			
			
		} else if (query == 2) {
			query2Final.put("Precision @ 5: ", precQ2.get(5));
			query2Final.put("Precision @ 10: ", precQ2.get(10));
			query2Final.put("Precision @ 20: ", precQ2.get(20));
			query2Final.put("Precision @ 50: ", precQ2.get(50));
			query2Final.put("Precision @ 100: ", precQ2.get(100));
			
			query2Final.put("Recall @ 5: ", recallQ2.get(5));
			query2Final.put("Recall @ 10: ", recallQ2.get(10));
			query2Final.put("Recall @ 20: ", recallQ2.get(20));
			query2Final.put("Recall @ 50: ", recallQ2.get(50));
			query2Final.put("Recall @ 100: ", recallQ2.get(100));
			
			query2Final.put("Average Precision @ 5: ", aPrecQ2.get(5));
			query2Final.put("Average Precision @ 10: ", aPrecQ2.get(10));
			query2Final.put("Average Precision @ 20: ", aPrecQ2.get(20));
			query2Final.put("Average Precision @ 50: ", aPrecQ2.get(50));
			query2Final.put("Average Precision @ 100: ", aPrecQ2.get(100));
			
			query2Final.put("F1 @ 5: ", f1Q2.get(5));
			query2Final.put("F1 @ 10: ", f1Q2.get(10));
			query2Final.put("F1 @ 20: ", f1Q2.get(20));
			query2Final.put("F1 @ 50: ", f1Q2.get(50));
			query2Final.put("F1 @ 100: ", f1Q2.get(100));
			
			query2Final.put("NCDG @ 5: ", ndcgQ2.get(5));
			query2Final.put("NCDG @ 10: ", ndcgQ2.get(10));
			query2Final.put("NCDG @ 20: ", ndcgQ2.get(20));
			query2Final.put("NCDG @ 50: ", ndcgQ2.get(50));
			query2Final.put("NCDG @ 100: ", ndcgQ2.get(100));
			
			for (Integer i : rprecQ2.keySet()) {
				query2Final.put("R-Precision: ", rprecQ2.get(i));
			}
		} else {
			query3Final.put("Precision @ 5: ", precQ3.get(5));
			query3Final.put("Precision @ 10: ", precQ3.get(10));
			query3Final.put("Precision @ 20: ", precQ3.get(20));
			query3Final.put("Precision @ 50: ", precQ3.get(50));
			query3Final.put("Precision @ 100: ", precQ3.get(100));
			
			query3Final.put("Recall @ 5: ", recallQ3.get(5));
			query3Final.put("Recall @ 10: ", recallQ3.get(10));
			query3Final.put("Recall @ 20: ", recallQ3.get(20));
			query3Final.put("Recall @ 50: ", recallQ3.get(50));
			query3Final.put("Recall @ 100: ", recallQ3.get(100));
			
			query3Final.put("Average Precision @ 5: ", aPrecQ3.get(5));
			query3Final.put("Average Precision @ 10: ", aPrecQ3.get(10));
			query3Final.put("Average Precision @ 20: ", aPrecQ3.get(20));
			query3Final.put("Average Precision @ 50: ", aPrecQ3.get(50));
			query3Final.put("Average Precision @ 100: ", aPrecQ3.get(100));
			
			query3Final.put("F1 @ 5: ", f1Q3.get(5));
			query3Final.put("F1 @ 10: ", f1Q3.get(10));
			query3Final.put("F1 @ 20: ", f1Q3.get(20));
			query3Final.put("F1 @ 50: ", f1Q3.get(50));
			query3Final.put("F1 @ 100: ", f1Q3.get(100));
			
			query3Final.put("NCDG @ 5: ", ndcgQ3.get(5));
			query3Final.put("NCDG @ 10: ", ndcgQ3.get(10));
			query3Final.put("NCDG @ 20: ", ndcgQ3.get(20));
			query3Final.put("NCDG @ 50: ", ndcgQ3.get(50));
			query3Final.put("NCDG @ 100: ", ndcgQ3.get(100));
			
			for (Integer i : rprecQ3.keySet()) {
				query3Final.put("R-Precision: ", rprecQ3.get(i));
			}
		}
		
	}

	private void getNDCG() {

		for (int i = 1; i <= 200; i++) {
			int twos1 = 0;
			int ones1 = 0;
			int zeros1 = 0;
			int twos2 = 0;
			int ones2 = 0;
			int zeros2 = 0;
			int twos3 = 0;
			int ones3 = 0;
			int zeros3 = 0;
			for (String docid : q1.keySet()) {
				String score = q1.get(docid);
				if (score.equals("2")) {
					twos1++;
				} else if (score.equals("1")) {
					ones1++;
				} else {
					zeros1++;
				}
			}
			for (String docid : q2.keySet()) {
				String score = q2.get(docid);
				if (score.equals("2")) {
					twos2++;
				} else if (score.equals("1")) {
					ones2++;
				} else {
					zeros2++;
				}
			}
			for (String docid : q3.keySet()) {
				String score = q3.get(docid);
				if (score.equals("2")) {
					twos3++;
				} else if (score.equals("1")) {
					ones3++;
				} else {
					zeros3++;
				}
			}
//			if (i == 200) {
//				System.out.println(numReleventQ1 + " 2: " + twos1 + " 1: " + ones1 + " 0: " + zeros1);
//			}
			double dcgSort1 = getSortDCG(twos1, ones1, zeros1, i);
			double dcgSort2 = getSortDCG(twos2, ones2, zeros2, i);
			double dcgSort3 = getSortDCG(twos3, ones3, zeros3, i);
			
			double ndcg1 = dcgQ1.get(i) / dcgSort1;
			double ndcg2 = dcgQ2.get(i) / dcgSort2;
			double ndcg3 = dcgQ3.get(i) / dcgSort3;
			
			ndcgQ1.put(i, ndcg1);
			ndcgQ2.put(i, ndcg2);
			ndcgQ3.put(i, ndcg3);
		}
		
	}

	private void getDCG() throws IOException {
		BufferedReader results = new BufferedReader(new FileReader(resultsFile));
		String line;
		String oldQuery = "";
		Integer totalCount = 0;
		double dcg = 0;
		while (((line = results.readLine()) != null) && (line.length() != 0)) {
			String[] splitLine = line.split("\\s+");
			// 1 is query, 2 is docid, 3 is count, 4 is score, 1 and 5 useless
			String query = splitLine[0].toLowerCase().trim();//splitLine[1].toLowerCase().trim();//splitLine[1].toLowerCase().trim();
			String docid = splitLine[2].toLowerCase().trim();
			if (!oldQuery.equals(query)) {
				totalCount = 0;
				dcg = 0;
			}
			int whereToPutPrec;
			totalCount++;
			Map<String, String> qrelList;
			if (query.toLowerCase().equals("q1")) {
				qrelList = q1;
				whereToPutPrec = 1;
				
			} else if (query.toLowerCase().equals("q2")) {
				qrelList = q2;
				whereToPutPrec = 2;
			} else {
				qrelList = q3;
				whereToPutPrec = 3;
			}
			String textScore = qrelList.get(docid);
			double score = 0.0 + Integer.parseInt(textScore);
			
			if (totalCount.equals(1)) {
				dcg = score;
			} else {
				double numerator = score / Math.log(totalCount);
				dcg = dcg + numerator;
			}
			
			// PUT IN APPROPRIATE PRECISION MAP
			if (whereToPutPrec == 1) {
				dcgQ1.put(totalCount, dcg);
			} else if (whereToPutPrec == 2) {
				dcgQ2.put(totalCount, dcg);
			} else {
				dcgQ3.put(totalCount, dcg);
			}
			oldQuery = query;
		}
	}

	private double getSortDCG(int twos, int ones, int zeros, int i) {
		double dcg = 0;
		if (twos > 0) {
			dcg = 2;
			twos--;
		} else if (ones > 0) {
			dcg = 1;
			ones--;
		} else {
			zeros--;
		}
		
		if (i > 1) {
			for (int j = 2; j <= i; j++) {
				double numerator = 0;
				if (twos > 0) {
					numerator = 2;
					twos--;
				} else if (ones > 0) {
					numerator = 1;
					ones--;
				} else {
					zeros--;
				}
				numerator = numerator / Math.log(i);
				dcg = dcg + numerator;
			}
		}
		return dcg;
		
	}

	private void getF1() {
		for (int i = 1; i <= 200; i++) {
			double f1_1 = (2 * precQ1.get(i) * recallQ1.get(i)) / (precQ1.get(i) + recallQ1.get(i)) ;
			double f1_2 = (2 * precQ2.get(i) * recallQ2.get(i)) / (precQ2.get(i) + recallQ2.get(i)) ;
			double f1_3 = (2 * precQ3.get(i) * recallQ3.get(i)) / (precQ3.get(i) + recallQ3.get(i)) ;
			//System.out.println("2 * " + precQ1.get(i) + " * " + recallQ1.get(i) + " = " + f1_1);
			f1Q1.put(i, f1_1);
			f1Q2.put(i, f1_2);
			f1Q3.put(i, f1_3);
			
		}
		
	}

	private void getRPrecisions() {
		for(int i = 1; i <= 200; i++) {
			//System.out.println(precQ1.size());
			double q1Prec = precQ1.get(i);
			double q2Prec = precQ2.get(i);
			double q3Prec = precQ3.get(i);
			
			double q1Recall = recallQ1.get(i);
			double q2Recall = recallQ2.get(i);
			double q3Recall = recallQ3.get(i);
			
			if (q1Prec == q1Recall) {
				rprecQ1.put(i, q1Prec);
			}
			if (q2Prec == q2Recall) {
				rprecQ2.put(i, q2Prec);
			}
			if (q3Prec == q3Recall) {
				rprecQ3.put(i, q3Prec);
			}
			
			
		}
//		System.out.println(rprecQ1.size());
//		System.out.println(rprecQ2.size());
//		System.out.println(rprecQ3.size());
		
	}



	private void getRecalls() throws IOException {
		BufferedReader results = new BufferedReader(new FileReader(resultsFile));
		String line;
		String oldQuery = "";
		double relCount = 0;
		Integer totalCount = 0;
		while (((line = results.readLine()) != null) && (line.length() != 0)) {
			String[] splitLine = line.split("\\s+");
			// 1 is query, 2 is docid, 3 is count, 4 is score, 1 and 5 useless
			String query = splitLine[0].toLowerCase().trim();//splitLine[1].toLowerCase().trim();//splitLine[1].toLowerCase().trim();
			String docid = splitLine[2].toLowerCase().trim();
			if (!oldQuery.equals(query)) {
				relCount = 0;
				totalCount = 0;
			}
			int whereToPutPrec;
			totalCount++;
			Map<String, String> qrelList;
			Integer releventDocs;
			if (query.toLowerCase().equals("q1")) {
				qrelList = q1;
				whereToPutPrec = 1;
				releventDocs = numReleventQ1;
				
			} else if (query.toLowerCase().equals("q2")) {
				qrelList = q2;
				whereToPutPrec = 2;
				releventDocs = numReleventQ2;
			} else {
				qrelList = q3;
				whereToPutPrec = 3;
				releventDocs = numReleventQ3;
			}
			if (qrelList.get(docid).equals("2") || qrelList.get(docid).equals("1")) {
				relCount++;
			} 
			
			double recall = relCount/releventDocs;
			//System.out.println(query + " " + docid + " " + relCount + "/" + releventDocs + " = "+ recall); /////////////////////////////////////////
			
			// PUT IN APPROPRIATE PRECISION MAP
			if (whereToPutPrec == 1) {
				recallQ1.put(totalCount, recall);
			} else if (whereToPutPrec == 2) {
				recallQ2.put(totalCount, recall);
			} else {
				recallQ3.put(totalCount, recall);
			}
			
			oldQuery = query;
		}
		
	}

	public static void getPrecisions() throws IOException {
		BufferedReader results = new BufferedReader(new FileReader(resultsFile));
		String line;
		String oldQuery = "";
		double relCount = 0;
		Integer totalCount = 0;
		double precisionSum = 0;
		while (((line = results.readLine()) != null) && (line.length() != 0)) {
			String[] splitLine = line.split("\\s+");
			// 1 is query, 2 is docid, 3 is count, 4 is score, 1 and 5 useless
			String query = splitLine[0].toLowerCase().trim();//splitLine[1].toLowerCase().trim();
			String docid = splitLine[2].toLowerCase().trim();
			if (!oldQuery.equals(query)) {
				if (oldQuery.equals("q1")) {
					numReleventQ1 = (int) relCount;
					
				} else if (oldQuery.equals("q2")) {
					numReleventQ2 = (int) relCount;
					
				} else {
					numReleventQ3 = (int) relCount;
				}
				relCount = 0;
				totalCount = 0;
				precisionSum = 0;
			}
			int whereToPutPrec;
			totalCount++;
			Map<String, String> qrelList;
			if (query.toLowerCase().equals("q1")) {
				qrelList = q1;
				whereToPutPrec = 1;
				
			} else if (query.toLowerCase().equals("q2")) {
				qrelList = q2;
				whereToPutPrec = 2;
			} else {
				qrelList = q3;
				whereToPutPrec = 3;
			}
			if (qrelList.get(docid).equals("2") || qrelList.get(docid).equals("1")) {
				relCount++;
				
			} 
			double precision = relCount/totalCount;
			
			// If it is relevent, we add the precision to our precision total, to then
			// change the average precision
			if (qrelList.get(docid).equals("2") || qrelList.get(docid).equals("1")) {
				precisionSum = precisionSum + precision;
			} 
			double avgPrec = precisionSum/relCount;
			//System.out.println(query + " " + totalCount + " " + avgPrec);
			//System.out.println(query + " " + docid + " " + relCount + "/" + totalCount + " = "+ precision); /////////////////////////////////////////
			//System.out.println(query + " " + totalCount);
			// PUT IN APPROPRIATE PRECISION MAP
			if (whereToPutPrec == 1) {
				precQ1.put(totalCount, precision);
				aPrecQ1.put(totalCount, avgPrec);
				numReleventQ1 = (int) relCount;
			} else if (whereToPutPrec == 2) {
				precQ2.put(totalCount, precision);
				aPrecQ2.put(totalCount, avgPrec);
				numReleventQ2 = (int) relCount;
			} else {
				precQ3.put(totalCount, precision);
				aPrecQ3.put(totalCount, avgPrec);
				numReleventQ3 = (int) relCount;
			}
			
			oldQuery = query;
			
		}
	}
	
	
	public void handleQrel(BufferedReader qrel) throws IOException {
		String lineA;
		String lineM;
		String lineS;
		while (((lineA = qrel.readLine()) != null) && (lineA.length() != 0)) {
			lineM = qrel.readLine();
			lineS = qrel.readLine();
			String[] austinArray = lineA.split("\\s+");
			String[] mosesArray = lineM.split("\\s+");
			String[] steveArray = lineS.split("\\s+");
			String query = austinArray[0].toLowerCase().trim();
			String docid = austinArray[2].toLowerCase().trim();
			String aValue = austinArray[3];
			String mValue = mosesArray[3];
			String sValue = steveArray[3];
			String finalValue;
			
			// FIND AGREED ON VALUE
			if (aValue.equals(mValue) || aValue.equalsIgnoreCase(sValue)) {
				// print a
				finalValue = aValue;
			} else if (mValue.equals(sValue)) {
				// print m
				finalValue = mValue;
			} else {
				// print 1
				finalValue = "1";
			}
			//System.out.println(finalValue);
			
			
			// PUT INTO QUERY MAPS
			if (query.equals("q1")) {
				q1.put(docid, finalValue);
			} else if (query.equalsIgnoreCase("q2")) {
				q2.put(docid, finalValue);
			} else {
				q3.put(docid, finalValue);
			}
			
		}
		sortq1 = entriesSortedByValues(q1);
		sortq2 = entriesSortedByValues(q2);
		sortq3 = entriesSortedByValues(q3);
	}
    // Method found on StackOverflow that sorts Hashmaps by their values
    // http://stackoverflow.com/questions/8119366/sorting-hashmap-by-values
    public static <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> entriesSortedByValues(
            Map<K, V> map) {
        SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<Map.Entry<K, V>>(
                new Comparator<Map.Entry<K, V>>() {
                    @Override
                    public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                        int res = e1.getValue().compareTo(e2.getValue()) * -1;
                        return res != 0 ? res : 1;
                    }
                });
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }
}
