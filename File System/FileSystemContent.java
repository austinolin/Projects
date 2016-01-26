package edu.neu.ccs.cs8674.sp15.seattle.assignment4.problem2;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * This is an abstract class for a FileSystemContent, which can be either a Folder or a File
 * @author Austin
 *
 */
public abstract class FileSystemContent {

	/**
	 * Will get the name of the File or Folder
	 * @return the String name
	 */
	public abstract String getName();
	
	
	/**
	 * Will get the size of the file or folder
	 * @return the Integer size
	 */
	public abstract Integer getSize();
	

	/**
	 * Asks for the creation date of the contents
	 * @return the Date creationDate
	 */
	public abstract Date getCreationDate();

	/**
	 * Asks for the date the content was last updated
	 * @return the Date lastModified
	 */
	public abstract Date getLastModified();
	
	/**
	 * Asks if this is a File
	 * @return true if it is a File, else false
	 */
	public abstract boolean isFile();
	
	/**
	 * Asks if this is a Folder
	 * @return true if it is a Folder, else false
	 */
	public abstract boolean isFolder();
	
	/**
	 * Will return a String of the FileSystemContent and all of its
	 * subfolders and files
	 * @param space, the non-null String indent we have from the previous folder level
	 * @return a String version of the FileSystemContent
	 * @pre: space != null
	 */
	public abstract String print(String space);
	
	/**
	 * Will convert the bytes given to Gb/Kb/Mb if reasonable (if
	 * the bytes given can equal at least 1 of any unit, it will convert
	 * to the highest possible choice
	 * @return, a String array containing the Integer value and the String
	 * unit
	 * @Pre: size is non-null and in bytes
	 * @Post: Will change to the highest unit that the size is
	 * equal to at least 1 of. EX: size in bytes == 1GB, convert to [1, GB]
	 */
	public abstract ArrayList<String> convertSize();
	
	/**
	 * Will return a String of the data usage of the FileSystemContent 
	 * and all of its subfolders and files
	 * @param path, the non-null String showing the filepath so far
	 * @return a String version of the FileSystemContent
	 * @pre: path != null
	 */
	public abstract String printDiskUsage(String path);

	/**
	 * Will add current Folder or File path and size into TreeMap<Path,Size>,
	 * and add all subfolders and files as well
	 * @param fsc, a non-null TreeMap
	 * @param filePath, a non-null String of the path to this Folder/File
	 * @return, the TreeMap with the folder (and all subcontent) or the File
	 * added to it
	 * @pre: Params != null
	 * @post: Key = filePath + toString(), Value = size (in bytes)
	 */
	public abstract TreeMap<String, Integer> buildListOfFileSystem(
			TreeMap<String, Integer> fsc, String filePath);
}
