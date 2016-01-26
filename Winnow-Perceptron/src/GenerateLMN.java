
import java.util.HashSet;
import java.util.Random;

public class GenerateLMN {

    public static int l = 10;
    public static int m = 100;
    public static int n = 500;
    
    public static int instances = 50000;
    
    public static double labelNoise = 0.0;	// 0.05 for part 3
    public static double featureNoise = 0.0;	// 0.001 for part 3
    public static double irrelevantActive = 0.5;	// probability of irrelevant feature being active
    public static double labelRatioPos = 0.5;		// ratio of postive to negative labels
    
    public static long rngSeed = 239485235295L;
    //public static long rngSeed = System.currentTimeMillis();
    
    public static void main(String[] args) {
	
    	// Take customized parameters
    	l = Integer.parseInt(args[0]);
    	m = Integer.parseInt(args[1]);
    	n = Integer.parseInt(args[2]);
    	instances = Integer.parseInt(args[3]);
    	boolean noisy = Boolean.getBoolean(args[4]);
    	if (noisy) {
    		labelNoise = .05;
    		featureNoise = .001;
    	}
    	
    	
	Random rng = new Random(rngSeed);
	
	int positive = (int) Math.ceil((double) instances * labelRatioPos); 
	int negative = instances - positive;
	//System.out.println(positive + "," + negative);
	
	int current_pos = 0;
	int current_neg = 0;
	
	//System.out.println(generatePositions(rng,3,5));
	
	//System.exit(0);
	
	while ((current_pos < positive) || (current_neg < negative)) {
	    if ((current_neg < negative) && 
		((current_pos >= positive) ||
		 (rng.nextDouble() >= labelRatioPos))) {
		generateNegative(rng);
		current_neg++;
	    }
	    else {
		generatePositive(rng);
		current_pos++;
	    }
	}
	//System.out.println(current_pos + "," + current_neg);
    }
    
    public static void generatePositive(Random rng) {
	int num_active = rng.nextInt(m-l+1) + l;
	//System.out.println(num_active);
	if (rng.nextDouble() < labelNoise)
	    System.out.print("-");
	else
	    System.out.print("+");
	System.out.print("1");
	HashSet<Integer> active = generatePositions(rng, num_active, m);
	for (int i = 1; i <= m; i++) {
	    double fNoise = rng.nextDouble();
	    if ((active.contains(i) && (fNoise >= featureNoise)) ||
		(!active.contains(i) && (fNoise < featureNoise)))
		System.out.print(" " + i + ":1");
        }
	for (int i = (m + 1); i <= n; i++) {
	    double fNoise = rng.nextDouble();
	    if (((fNoise < irrelevantActive) &&
		 (rng.nextDouble() >= featureNoise)) ||
		((fNoise >= irrelevantActive) &&
		 (rng.nextDouble() < featureNoise)))
		System.out.print(" " + i + ":1");
	}
	System.out.println();
    }
    
    public static void generateNegative(Random rng) {
	int num_active = rng.nextInt(l);
	//System.out.println(num_active);
	if (rng.nextDouble() < labelNoise)
	    System.out.print("+");
	else
	    System.out.print("-");
	System.out.print("1");
	HashSet<Integer> active = generatePositions(rng, num_active, m);
	for (int i = 1; i <= m; i++) {
	    double fNoise = rng.nextDouble();
	    if ((active.contains(i) && (fNoise >= featureNoise)) ||
		(!active.contains(i) && (fNoise < featureNoise)))
		System.out.print(" " + i + ":1");
	}
	for (int i = (m + 1); i <= n; i++) {
	    double fNoise = rng.nextDouble();
	    if (((fNoise < irrelevantActive) &&
		 (rng.nextDouble() >= featureNoise)) ||
		((fNoise >= irrelevantActive) &&
		 (rng.nextDouble() < featureNoise)))
		System.out.print(" " + i + ":1");
	}
	System.out.println();
    }
    
    public static HashSet<Integer> generatePositions(Random rng, int a, int m) {
	HashSet<Integer> result = new HashSet<Integer>();
	int remainder = a;
	int possible = m;
	for (int i = 1; i <= m; i++) {
	    if (rng.nextDouble() < ((double) remainder / possible)) {
		result.add(i);
		remainder--;
	    }
	    possible--;
	}
	return result;
    }
}