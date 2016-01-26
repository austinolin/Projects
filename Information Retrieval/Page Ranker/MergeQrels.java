import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class MergeQrels {
	
	public static void main(String[] args) throws IOException {
		String austin = "C:/Users/Austin/workspace/HW3/qrel_austin.txt";
		String moses = "C:/Users/Austin/workspace/HW3/qrel_moses.txt";
		String steve = "C:/Users/Austin/workspace/HW3/qrel_steve.txt";
		String qrelResults = "C:/Users/Austin/workspace/HW3/qrel_results.txt";
		File file = new File(qrelResults);
		FileWriter writer = new FileWriter(file);
		BufferedReader a = null;
		BufferedReader m = null;
		BufferedReader s = null;
		
		a = new BufferedReader(new FileReader(austin));
		m = new BufferedReader(new FileReader(moses));
		s = new BufferedReader(new FileReader(steve));
		String lineA;
		String lineM;
		String lineS;
		while (((lineA = a.readLine()) != null) && (lineA.length() != 0)) {
			lineM = m.readLine();
			lineS = s.readLine();
			writer.write(lineA + "\n");
			writer.write(lineM + "\n");
			writer.write(lineS + "\n");
//			String[] austinArray = lineA.split(" ");
//			String[] mosesArray = lineM.split(" ");
//			String[] steveArray = lineS.split(" ");
//			String aValue = austinArray[2];
//			String mValue = mosesArray[2];
//			String sValue = steveArray[2];
//			if (aValue.equals(mValue) || aValue.equalsIgnoreCase(sValue)) {
//				// print a
//			} else if (mValue.equals(sValue)) {
//				// print m
//			} else {
//				// print 1
//			}
			
		}
		writer.close();
		
		
	}

}
