import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class CreateResultsFile {

	public static void main(String[] args) throws IOException {
		String austin = "C:/Users/Austin/workspace/HW3/qrel_austin.txt";
		BufferedReader a = new BufferedReader(new FileReader(austin));
		String testFile = "C:/Users/Austin/workspace/HW3/testFile.txt";
		File file = new File(testFile);
		FileWriter writer = new FileWriter(file);
		String line;
		int count = 0;
		while (((line = a.readLine()) != null) && (line.length() != 0)) {
			
			count++;
			String[] splitLine = line.split(" ");
			writer.write(splitLine[0] + " Q0 " + splitLine[2] + " " + count + " " + splitLine[3] + " Exp" + "\n");
		}
		
		writer.close();
	}
}
