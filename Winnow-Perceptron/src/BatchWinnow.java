import java.io.BufferedReader;
import java.io.FileReader;

public class BatchWinnow {

		static double eta = 1.0;
		static double margin = 0;
		static int mistakes = 0;
		static int instances = 50000;
		static int positions = 0;
		static boolean halting = false;
		static double highestAccuracy = 0;
		static int T = 20;
		static int t = 0;
		static int theta = 0;
		static double[][] d1 = new double[5000][];
		static double[][] d2 = new double[5000][];
		
		static double[][] corpus = new double[instances][];
		static double[][] testing = new double[10000][];
		static double[] w;
		
		
		// Main
		public static void main(String[] args) {
			// Position count, file, margin, halting?
			positions = Integer.parseInt(args[0]);
			String dataFile = args[1];
			String testFile = args[2];
			margin = Integer.parseInt(args[3]);
			halting = Boolean.getBoolean(args[4]);
			
			// create weight array
			w = new double[positions];
			
			// theta = n
			theta = positions;
			
			//setup
			getCorpus(dataFile);
			getTesting(testFile);
			getD1();
			getD2();
			
			double[] etaChoices = getPossibleEtas();
			double[] marginChoices = getPossibleMargins();
			testEtaChoices(etaChoices, marginChoices);
			System.out.println("Final Eta: " + eta);
			System.out.println("Final Margin: " + margin);
			System.out.println("Final Accuracy: " + highestAccuracy);
			
			mistakes = 0;
			T = 1;
			winnow(corpus, halting);
			double accuracyTesting = getAccuracy(testing, 10000.0);
			System.out.println("Accuracy for Test File: " + accuracyTesting);
			
		}
		


		// Parameters: Array of double[]'s that have y as first element
		// and the x's as the rest
		public static void winnow(double[][] S, boolean halting) {
			// w[] <- 0, b <- 0, t <- 0
			t = 0;
			for (int i = 0; i < w.length; i++) {
				w[i] = 1;
			}
			// initialize halting count
			int haltingCount = 0;
			// while t < T (or until halt if halting is true), do
			while (t < T && haltingCount <= 1000) {
				// for all (x, y) ∈ S do
				for (double[] x : S) { //x[0] is label
					double y = x[0];
					double dotproduct = w_x_dotproduct(x);
					// check for mistakes
					if ((y * (dotproduct - theta)) <= 0) {
						mistakes++;
						haltingCount = 0;
					} else {
						if (halting) {
							haltingCount++;
						}
					}
					// if y(w·x−θ) ≤ margin then
					if ((y * (dotproduct - theta)) <= margin) {
						// w ← w ◦ ηyx
						updateWeights(y, x);
					} // end if
				} // end for
				if (!halting) {
					t = t + 1;
				}
			}// end while
		}
		
		//////////////////////
		// HELPER FUNCTIONS //
		//////////////////////
		
		// multiplies weights with x values, then sums up and
		// returns the sum (for the update conditional)
		public static double w_x_dotproduct(double[] x) {
			double dotproduct = 0;
			for (int i = 0; i < w.length; i++) {
				dotproduct = dotproduct + (w[i] * x[i+1]);
			}
			return dotproduct;
			
		}
		
		// given a y label and the entire x array of labels (x[0] being y)
		// will update each weight
		public static void updateWeights(double y, double[] x) {
			for (int i = 0; i < w.length; i++) {
				// w ← w ◦ ηyx
				w[i] = w[i] * Math.pow(eta, y * x[i+1]);
			}
		}
		
		// Parameters: file
		// will create a double[][] of each instance, where the first double
		// is the label y and the rest are the respective position labels
		public static void getCorpus(String file) {
			try {
				// Reads the file
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line = null;
				int count = 0;
				while (((line = br.readLine()) != null) && (count < instances)) {
					String[] instance = line.split("\\s+"); 
					// create instance array, initialize all positions to 0
					double[] X = new double[positions + 1];
					for (int i = 0; i < X.length; i++) {
						X[i] = 0;
					}
					//add label to first slot
					X[0] = Double.parseDouble(instance[0]);
					// add rest of position labels to correct spot
					for (int j = 1; j < instance.length; j++) {
						String[] splitPosition = instance[j].split(":");
						int position = Integer.parseInt(splitPosition[0]);
						double label = Double.parseDouble(splitPosition[1]);
						X[position] = label;
					}
					corpus[count] = X;
					count++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public static void getTesting(String file) {
			try {
				// Reads the file
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line = null;
				int count = 0;
				while (((line = br.readLine()) != null) && (count < 10000)) {
					String[] instance = line.split("\\s+"); 
					// create instance array, initialize all positions to 0
					double[] X = new double[positions + 1];
					for (int i = 0; i < X.length; i++) {
						X[i] = 0;
					}
					//add label to first slot
					X[0] = Double.parseDouble(instance[0]);
					// add rest of position labels to correct spot
					for (int j = 1; j < instance.length; j++) {
						String[] splitPosition = instance[j].split(":");
						int position = Integer.parseInt(splitPosition[0]);
						double label = Double.parseDouble(splitPosition[1]);
						X[position] = label;
					}
					testing[count] = X;
					count++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// will take first 5000 instances in corpus and 
		// put them in D1 for training hyperparameters
		public static void getD1() {
			for (int i = 0; i < 5000; i++) {
				d1[i] = corpus[i]; // start at 0, go to 4999
			}
		}
		
		// will take second 5000 instances in corpus and 
		// put them in D2 for testing hyperparameters
		public static void getD2() {
			for (int i = 0; i < 5000; i++) {
				d2[i] = corpus[5000+i]; // start at 5000, go to 9999
			}
		}
		
		// Will return the possible etas to choose from based on the margin
		public static double[] getPossibleEtas() {
			double[] etaChoices;
			if (margin == 0) {
				etaChoices = new double[] {1.1, 1.01, 1.005, 1.0005, 1.0001};
			} else {
				etaChoices = new double[] {1.1, 1.01, 1.005, 1.0005, 1.0001};
			}
			return etaChoices;
		}
		
		public static void testEtaChoices(double[] etaChoices, double[] marginChoices) {
			for (double etaChoice : etaChoices) {
				for (double marginChoice : marginChoices) {
					// Remember old in case new isn't better
					double previousEta = eta;
					eta = etaChoice;
					System.out.println("Testing eta = " + etaChoice + ", margin = " + marginChoice);
					// Remember old in case new isn't better
					double previousMargin = margin;
					margin = marginChoice;
					winnow(d1, false);
					double accuracy = getAccuracy(d2, 5000);
					System.out.println("Accuracy: " + accuracy);
					// higher accuracy replaces eta permanently
					if (accuracy > highestAccuracy) {
						highestAccuracy = accuracy;
						System.out.println(eta + " is new eta value, " + margin + " is the new margin value");
					} else { //lower accuracy returns previous eta value
						eta = previousEta;
						margin = previousMargin;
						System.out.println(eta + " remains the eta value, " + margin + " remains the margin value");
					}
					System.out.println();
				}
			}
		}
		
		public static double getAccuracy(double[][] d2, double total) {
			double correct = 0.0;
			// for every instance in testing sample
			for (double[] x : d2) {
				// y-value
				double actual = x[0];
				double predicted = 0.0;
				double sum = 0;
				// sum up w*x+b
				double dotproduct = w_x_dotproduct(x);
				sum = dotproduct - theta;
//				for (int i = 1; i < x.length; i++) {
//					sum = sum + (w[i - 1] * x[i] - theta);
//				}
				// evaluate sum to a label
				if (sum > 0) {
					predicted = 1.0;
				} else {
					predicted = -1.0;
				}
				// check if correct
				if (predicted == actual) {
					correct++;
				}
			}
			return correct/total;
		}
		
		public static double[] getPossibleMargins() {
			double[] marginChoices;
			if (margin == 0) {
				marginChoices = new double[] {0.0};
			} else {
				marginChoices = new double[] {2.0, 0.3, 0.04, 0.006, 0.001};
			}
			return marginChoices;
		}
		
		
		public static void printWeights() {
			for (int i = 0; i < w.length; i++) {
				System.out.print(i + 1 + ":" + w[i] + " ");
			}
			System.out.println();
		}
}