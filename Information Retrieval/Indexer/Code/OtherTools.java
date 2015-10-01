package B;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;


import java.util.regex.Pattern;

/**
 *  Series of helper methods for parsing or serializing/reading/writing to files
 * @author Austin
 *
 */
public class OtherTools {

    private static ArrayList<String> stopList;


    //Takes in the 
    public OtherTools() {
    	//Will generate the list of stopwords
    	stopList = generateStopList();
    }

    
    /**
	 * Will take an object that we want to write to a file, and
	 * convert it into a byte array
	 */
	public static byte[] serialize(Object o) {
		// Initialize the output streams
        ByteArrayOutputStream byteOutput = null;
        ObjectOutputStream objOutput = null;

        try {
        	//Convert object into bytes
        	byteOutput = new ByteArrayOutputStream();
            objOutput = new ObjectOutputStream(byteOutput);
            objOutput.writeObject(o);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
            	objOutput.close();
            	byteOutput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // return byteArray to write file
        return byteOutput.toByteArray();
    }

	/**
	 * Will take a byte array representing an object, and
	 * will convert it back to an object and return it to the
	 * read method
	 */
    public static Object deserialize(byte[] byteInfo) {
    	// Initialize our results to null
    	Object results = null;
    	
    	// Initialize input streams 
        ByteArrayInputStream byteInput = null;
        ObjectInputStream objInput = null;

        try {
        	// convert byte array to an object
        	byteInput = new ByteArrayInputStream(byteInfo);
        	objInput = new ObjectInputStream(byteInput);
            results = objInput.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
            	objInput.close();
                byteInput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // return object to read method
        return results;
    }

    /**
     * Will take a byte array representing an object, and a file to
     * write to, and will write the object to the file. It will return
     * the long[] containing the start position of the byte offset and the length
     */
    public static long[] write(byte[] byteArray, File outputFile) {
    	//Initialize variables
    	long startingPosition = 0;
        FileChannel fileCh = null;
        FileOutputStream fileOutput = null;

        try {
        	fileCh = FileChannel.open(outputFile.toPath());
            fileOutput = new FileOutputStream(outputFile, true);
            // Starting position is the last written spot of the file, so the size
            startingPosition = fileCh.size();
            // write the byte array to the file
            fileOutput.write(byteArray);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
            	fileCh.close();
                fileOutput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // create the long[] containing the byte offset info
        long[] byteOffsetInfo = new long[2];
        byteOffsetInfo[0] = startingPosition;
        byteOffsetInfo[1] = byteArray.length;
        
        // return byte offset info
        return byteOffsetInfo;
    }

    /**
     * Will take a long[] containing the byte offset starting position and the
     * byte length, as well as a file to read from. It will reat the given byte info
     * position out of the file, and convert it to an object and return it
     */
    public static Object read(long[] byteInfo, File inputFile) {
    	// Initialize results to be the length of the byte offset
        ByteBuffer results = ByteBuffer.allocate((int) byteInfo[1]);
        FileChannel fileCh = null;

        try {
        	fileCh = FileChannel.open(inputFile.toPath());
        	// read the bytes in the file at the starting position and
        	// put them into the byte buffer
        	fileCh.read(results, byteInfo[0]);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
            	fileCh.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // will convert the bytes in the byte buffer to an object and return it
        return deserialize(results.array());
    }
    
    /**
     * Will take a file and parse through it, returning a list of String[]
     * containing all the docids and text pairings
     */
    public ArrayList<String[]> getDocuments(File file) {
    	ArrayList<String[]> documentList = new ArrayList<String[]>();
        BufferedReader br = null;
        try {
        	// Create new buffered reader to read the file
            br = new BufferedReader(new FileReader(file));

            String line;
            String docId = "";
            // if true, we are currently looking at text for a doc
            boolean isText = false;
            StringBuilder text = new StringBuilder();
            // While there are lines left
            while ((line = br.readLine()) != null) {
            	// get rid of white space
                line = line.trim();
                if (line.equals(""))
                    continue;
                // Split the terms into an array based on spaces
                String[] splitLine = line.split("\\s+");
                // the docid is at index 1
                if (splitLine[0].equals("<DOCNO>")) {
                    docId = splitLine[1];
                }
                // we are now looking at text
                if (splitLine[0].equals("<TEXT>")) {
                    isText = true;
                    continue;
                }
                // we are no longer looking at text
                if (splitLine[0].equals("</TEXT>")) {
                    isText = false;
                    continue;
                }
                // if we are looking at text, add the line
                // to our Stringbuilder
                if (isText) {
                    text.append(line);
                    text.append(" ");
                    continue;
                }
                // We are at the end of a document, so create the String[] and 
                // add it to our list
                if (splitLine[0].equals("</DOC>")) {
                	String[] doc = new String[2];
                	doc[0] = docId;
                	doc[1] = text.toString();
                	documentList.add(doc);
                    text = new StringBuilder();
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return documentList;
    }

    /**
     * Will parse through a string of text. Will tokenize it, then if indicated, stem and/or
     * get rid of stopwords. It will put each token into a TermAndPositions class that holds
     * the term name and the position in the string and put it in a list
     */
    public ArrayList<TermAndPosition> parse(String text, boolean stop, boolean stem) {
        ArrayList<String> tokens = tokenize(text);
        ArrayList<TermAndPosition> parsedTokens = new ArrayList<TermAndPosition>();
        int position = 1;
        //For each token in the tokens list
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            // if true, then we skip the word if its a stopword
            if (stop) {
                if (stopList.contains(token)) {
                    position++;
                    continue;
                }
            }
            // if true, we stem the word before adding it
            if (stem) {
                Stemmer stemmer = new Stemmer();
                char[] ch = token.toCharArray();
         
                stemmer.add(token.toCharArray(), token.length());
                stemmer.stem();
                parsedTokens.add(new TermAndPosition(stemmer.toString(), position));
                position++;
                continue;
            }
            // create a TermAndPosition object and then add it to the list
            parsedTokens.add(new TermAndPosition(token, position));
        }
        return parsedTokens;
    }

    /**
     * Will tokenize the text based on the regex.
     */
    private static ArrayList<String> tokenize(String text) {
        Pattern pattern = Pattern.compile("\\w+(\\.?\\w+)*");
        Matcher matcher = pattern.matcher(text.toLowerCase());
        ArrayList<String> result = new ArrayList<String>(matcher.groupCount());
        while (matcher.find()) {
            for (int i = 0; i < matcher.groupCount(); i++) {
                result.add(matcher.group(i));
            }
        }
        return result;
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
    
    /**
     * Will read the stoplist file and create a list of stopwords
     */
    private ArrayList<String> generateStopList() {
        ArrayList<String> stopList = new ArrayList<String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("C:/Users/Austin/Desktop/elasticsearch-1.4.2/AP89_DATA/AP_DATA/stoplist.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.equals("")) {
                	stopList.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stopList;
    }
}