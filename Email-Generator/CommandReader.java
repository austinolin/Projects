package edu.neu.ccs.cs8674.sp15.seattle.assignment5.problem1;

import java.util.*;
import java.io.*;

/**
 * This is the CommandReader class, which will read a series of commands from
 * the command prompt and if they are the correct commands, will run a Generator
 * that will generate emails/letters for all contacts in a CSV file
 * 
 * @author Austin
 *
 */
public class CommandReader {

	public static void main(String args[]) throws IOException {

		Console c = System.console();

		if (c == null) {
			System.err.println("No console.");
			System.exit(1);
		}
		// Create list of commands from line (must be at least 3)
		ArrayList<String> commands = new ArrayList<String>();
		while (commands.size() < 3) {
			String command = c.readLine();
			String[] commandSplit = command.split("--");
			for (int i = 1; i < commandSplit.length; i++) {
				commands.add(commandSplit[i]);
			}
		}

		// If commands are valid, generate emails/letters
		if (testValid(commands)) {
			Generator g = new Generator();
			String template = "";
			String outputFolder = "";
			String signature = "";
			for (int i = 0; i < commands.size(); i++) {
				String[] command = commands.get(i).split(" ");
				if (command.length == 2) {
					if (command[0].equals("email-template")
							|| command[0].equals("letter-template")) {
						template = command[1];
					} else if (command[0].equals("output-dir")) {
						outputFolder = command[1];
					} else {
						signature = command[1];
					}
				}
			}
			g.run(template, outputFolder, false, false, signature);
		} else {
			// Throw an error if commands are invalid, with instructions
			throw new Error(
					"Error: --email provided but no --email-template was given.\n\n"
							+ "Usage:\n\n"
							+ "--email                  only generate email messages\n"
							+ "--email-template <file>  accepts a filename that holds the email "
							+ "template. Required if --email is used\n\n"
							+ "--letter                 only generate letters\n"
							+ "--letter-template <file> accepts a filename that "
							+ "holds the email template. Required if --letter is used\n\n"
							+ "--output-dir <path>      accepts the name of a folder, "
							+ "all output is placed in this folder\n\n"
							+ "Examples:\n\n"
							+ "--email --email-template email-template.txt "
							+ "--output-dir emails\n"
							+ "--letter --letter-template letter-template.txt "
							+ "--output-dir letters");
		}
	}

	/**
	 * Will test to see if a list of commands from the command prompt are valid
	 * for running our email/letter generator. *
	 * 
	 * @param commands
	 *            , a non-null ArrayList<String> with the commands from the
	 *            prompt
	 * @return true if they are valid, else false
	 * @pre: commands.size() >= 3
	 * @post: [email, email-template, output-dir, signature] == true, [letter,
	 *        letter-template, output-dir, signature] == true, [letter,
	 *        letter-template, output-dir] == true, [email, email-template,
	 *        output-dir] == true (Order doesn't matter)
	 */
	public static boolean testValid(ArrayList<String> commands) {
		// false if more than 4 commands
		if (commands.size() > 4) {
			return false;
		}
		// if 4 commands, one must be a signature file,
		// all commands except "email" or "letter" must have
		// a filepath as well
		if (commands.size() == 4) {
			Integer signatureCommandCount = 0;
			for (int i = 0; i < 4; i++) {
				String[] commandSplit = commands.get(i).split(" ");
				if (commandSplit.length > 2) {
					return false;
				}
				if (commandSplit[0].equals("signature")) {
					signatureCommandCount++;
				}
			}
			if (!signatureCommandCount.equals(1)) {
				return false;
			}
		}
		// makes sure commands match (ex. no [email, letter-template]
		// combinations
		return (hasThreeMatchingEmailCommands(commands) || hasThreeMatchingLetterCommands(commands));

	}

	/**
	 * Will make sure the list of commands is a --email --email-template <path>
	 * --output-dir <path>
	 * 
	 * @param commands
	 *            , a non-null ArrayList<String> of commands from the command
	 *            console
	 * @return true if they go together correctly, else false if invalid
	 *         combination
	 * @pre: commands.size() == 3 (or 4 if one is a signature command) or less
	 *       commands
	 * @post: runs through list, returns true if there are no duplicates and one
	 *        match for "email", "email-template", and "output-dir"
	 */
	public static boolean hasThreeMatchingEmailCommands(
			ArrayList<String> commands) {
		boolean isEmail = false;
		boolean hasTemplate = false;
		boolean hasOutputDir = false;
		for (int i = 0; i < commands.size(); i++) {
			String[] command = commands.get(i).split(" ");
			if (command.length == 1 && command[0].equals("email") && !isEmail) {
				isEmail = true;
			}
			if (command[0].equals("email-template") && !hasTemplate) {
				hasTemplate = true;
			}
			if (command[0].equals("output-dir") && !hasOutputDir) {
				hasOutputDir = true;
			}
		}
		return isEmail && hasTemplate && hasOutputDir;
	}

	/**
	 * Will make sure the list of commands is a --letter --letter-template
	 * <path> --output-dir <path>
	 * 
	 * @param commands
	 *            , a non-null ArrayList<String> of commands from the command
	 *            console
	 * @return true if they go together correctly, else false if invalid
	 *         combination
	 * @pre: commands.size() == 3 (or 4 if one is a signature command) or less
	 *       commands
	 * @post: runs through list, returns true if there are no duplicates and one
	 *        match for "letter", "letter-template", and "output-dir"
	 */
	public static boolean hasThreeMatchingLetterCommands(
			ArrayList<String> commands) {
		boolean isLetter = false;
		boolean hasTemplate = false;
		boolean hasOutputDir = false;
		for (int i = 0; i < commands.size(); i++) {
			String[] command = commands.get(i).split(" ");
			if (command.length == 1 && command[0].equals("letter") && !isLetter) {
				isLetter = true;
			}
			if (command[0].equals("letter-template") && !hasTemplate) {
				hasTemplate = true;
			}
			if (command[0].equals("output-dir") && !hasOutputDir) {
				hasOutputDir = true;
			}
		}
		return isLetter && hasTemplate && hasOutputDir;
	}
}
