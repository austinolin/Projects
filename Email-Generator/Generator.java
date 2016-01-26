package edu.neu.ccs.cs8674.sp15.seattle.assignment5.problem1;

import java.io.*;

/**
 * This is the Generator class, which will take in a CSV document with the
 * contact information and generate a file with the email or letter template
 * modified for each individual contact
 * 
 * @author Austin
 */
public class Generator {

	/**
	 * Creates a new Generator class
	 */
	public Generator() {
	}

	/**
	 * Will run the steps to create files with emails/letters to be sent to
	 * contacts from the CSV file. For each contact, will get their contact
	 * information, will generate the formatted text, and will print it in the
	 * given destination
	 * 
	 * @param source
	 *            , non-null String filepath to the template text file
	 * @param destination
	 *            , non-null String filepath to folder we want to print files in
	 * @param stateFlag
	 *            , non-null Boolean. True if we want to print files in
	 *            subfolders divided by States (ex. NY, TX)
	 * @param emailFlag
	 *            , non-null Boolean. True if we want to print files in
	 *            subfolders divided by the contacts' email addresses (ex.
	 *            gmail, hotmail, yahoo, aol, other)
	 * @param signatureSource
	 *            , a non-null String that will be "" if no signature is to be
	 *            printed, or will have the file path to the signature text file
	 *            if we want to print one
	 * @pre: params != null, stateFlag and emailFlag cannot both be true
	 * @post: Prints to files named after the contacts' full names (ex.
	 *        "JamesButt.txt")
	 */
	public void run(String source, String destination, boolean stateFlag,
			boolean emailFlag, String signatureSource) {
		String csvFile = "C:/Users/Austin/Desktop/Assignment5/theater-company-members.csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = "BARF";
		String text;
		int counter = 0;

		try {
			// 0 "first_name",
			// 1 "last_name","
			// 2 company_name",
			// 3 "address",
			// 4 "city",
			// 5 "county",
			// 6 "state",
			// 7 "zip",
			// 8 "phone1",
			// 9 "phone2",
			// 10 "email",
			// 11 "web"
			br = new BufferedReader(new FileReader(csvFile));
			br.readLine();
			while (((line = br.readLine()) != null) && (line.length() != 0)) {
				counter++;
				line = line.replace("\"" + "," + "\"", "BARF");
				line = line.replace("\"" + ",", "BARF");
				line = line.replace("," + "\"", "BARF");
				line = line.replace("\"", "");
				String[] contactInfo = line.split(cvsSplitBy);
				if (source.toLowerCase().contains("email-template.txt")) {
					text = generateEmailText(source, contactInfo);
				} else {
					text = generateLetterText(source, contactInfo);
				}
				if (!signatureSource.equals("")) {
					text = text + generateSignature(signatureSource);
				}
				if (stateFlag) {
					printToFile(destination + "/" + contactInfo[6] + "/"
							+ counter + ".txt", text);
				} else if (emailFlag) {
					String folder = "other";
					if (contactInfo[10].toLowerCase().contains("@gmail.com")) {
						folder = "gmail";
					} else if (contactInfo[10].toLowerCase().contains(
							"@aol.com")) {
						folder = "aol";
					} else if (contactInfo[10].toLowerCase().contains(
							"@yahoo.com")) {
						folder = "yahoo";
					} else if (contactInfo[10].toLowerCase().contains(
							"@hotmail.com")) {
						folder = "hotmail";
					}
					printToFile(destination + "/" + folder + "/"
							+ counter + ".txt", text);

				} else {
					printToFile(destination + "/" + counter + ".txt", text);
				}

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("Done");
	}

	/**
	 * Will print the text of the email or letter to a file in the destination
	 * folder
	 * 
	 * @param destination
	 *            , String that is the file path to the folder we want to print
	 *            the file in (non-null)
	 * @param text
	 *            , the non-null String of text we want to print
	 * @throws IOException
	 * @pre: params != null, destination is a valid path
	 * @post: print the given text into a file in the given path. Will create
	 *        folders for state or email provider if included in destination
	 */
	public void printToFile(String destination, String text) throws IOException {
		File file = new File(destination);
		file.getParentFile().mkdirs();
		FileWriter writer = new FileWriter(file);
		writer.write(text);
		writer.flush();
		writer.close();
	}

	/**
	 * Will take a file path to the email template text file and an array of
	 * contact information, and will generate the String for the email
	 * 
	 * @param source
	 *            , the String file path to the email template (non-null)
	 * @param contactInfo
	 *            , the String[] containing one person's contact information
	 *            (non-null)
	 * @return the email text as a single String
	 * @pre: params != null, contactInfo contains in this order: [first name,
	 *       last name, company name, address, city, county, state, zip, phone1,
	 *       phone2, email address, web page URL], source leads to correct email
	 *       template
	 * @post: creates a String that if printed will match the email-template.txt
	 *        with the current contacts information added
	 */
	public String generateEmailText(String source, String[] contactInfo) {
		String emailFile = source;
		BufferedReader br = null;
		String line = "";
		Integer lineCounter = 1;
		String text = "";
		try {
			br = new BufferedReader(new FileReader(emailFile));
			while ((line = br.readLine()) != null) {

				if (lineCounter.equals(2)) {
					text = text + "To:" + contactInfo[10] + "\n";
				} else if (lineCounter.equals(5)) {
					text = text + "Dear " + contactInfo[0] + " "
							+ contactInfo[1] + ",\n";
				} else {
					text = text + line + "\n";
				}
				lineCounter++;

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return text;
	}

	/**
	 * Will take a file path to the letter template text file and an array of
	 * contact information, and will generate the String for the email
	 * 
	 * @param source
	 *            , the String file path to the email template (non-null)
	 * @param contactInfo
	 *            , the String[] containing one person's contact information
	 *            (non-null)
	 * @return the email text as a single String
	 * @pre: params != null, contactInfo contains in this order: [first name,
	 *       last name, company name, address, city, county, state, zip, phone1,
	 *       phone2, email address, web page URL], source leads to correct email
	 *       template
	 * @post: creates a String that if printed will match the
	 *        letter-template.txt with the current contacts information added
	 */
	public String generateLetterText(String source, String[] contactInfo) {
		String letterFile = source;
		BufferedReader br = null;
		String line = "";
		Integer lineCounter = 1;
		String text = "";
		try {
			br = new BufferedReader(new FileReader(letterFile));
			while ((line = br.readLine()) != null) {

				if (lineCounter.equals(1)) {
					text = text + contactInfo[2] + ".\n";
				} else if (lineCounter.equals(2)) {
					text = text + contactInfo[0] + " " + contactInfo[1] + "\n";
				} else if (lineCounter.equals(3)) {
					text = text + contactInfo[3] + ", " + contactInfo[4]
							+ ",\n";
				} else if (lineCounter.equals(4)) {
					text = text + contactInfo[5] + ", " + contactInfo[6] + ", "
							+ contactInfo[7] + "\n";
				} else if (lineCounter.equals(5)) {
					text = text + "(" + contactInfo[10] + ")\n";
				} else if (lineCounter.equals(7)) {
					text = text + "Dear " + contactInfo[0] + " "
							+ contactInfo[1] + ",\n";
				} else {
					text = text + line + "\n";
				}
				lineCounter++;

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return text;
	}

	/**
	 * Will take a file path to the signature text file and will generate the
	 * String for the signature
	 * 
	 * @param source
	 *            , non-null String that is the file path to the signature text
	 *            file
	 * @return a formatted String ready to print that is the signature
	 * @pre: source != null, is valid path
	 * @post: will return a String for the signature to be printed at the end of
	 *        the file
	 */
	public String generateSignature(String source) {
		String letterFile = source;
		BufferedReader br = null;
		String line = "";
		String text = "";
		try {
			br = new BufferedReader(new FileReader(letterFile));
			while ((line = br.readLine()) != null) {
				text = text + line + "\n";
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return text;
	}

}
