package edu.neu.ccs.cs8674.sp15.seattle.assignment4.problem2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This is the Class for a File System. With this, a user can create a root
 * folder, and add files/folders to them. They can also save files.
 * 
 * @author Austin
 */
public class FileSystem {
	Folder root;

	/**
	 * Creates a new FileSystem
	 */
	public FileSystem() {
	}

	/**
	 * Creates the empty root folder
	 * 
	 * @param creationDate
	 *            , Date that represents when the folder is being created
	 * @Pre: creationDate is non-null
	 * @Post: Will create a Root Folder named "/"
	 */
	public void createRootFolder(Date creationDate) {
		root = new Folder("/", creationDate, creationDate);
	}

	/**
	 * Will add the Folder f by following the filePath array, and then update
	 * all of the sizes and dates. IF: - filePath is empty, add to current
	 * folder if there are no duplicate names - filePath not empty, search
	 * folder for name in index 1 (next part of path), and if it is a folder,
	 * pass rest of path to it. Continue until we reach filePath is empty - if
	 * an index cannot find a match or there is a duplicate name where the
	 * folder should be added, throw an exception.
	 * 
	 * @param f
	 *            , the non-null Folder we want to add
	 * @param filePath
	 *            , a non-null ArrayList of Strings representing File/Folder
	 *            names. EXAMPLE: ["folder1", "folder2", "destinationfolder"]
	 * @param currentDate
	 *            , the non-null Date that we are currently adding the folder on
	 * @Pre: Root folder is already made, all params are non-null
	 * @Post: Folder added unless path is wrong or there is a name conflict
	 */
	public void addFolder(Folder f, ArrayList<String> filePath, Date currentDate) {
		root.addFolder(f, filePath, currentDate);
	}

	/**
	 * Will add the File f by following the filePath array, and then update all
	 * of the sizes and dates. IF: - filePath is empty, add to current folder if
	 * there are no duplicate names - filePath not empty, search folder for name
	 * in index 1 (next part of path), and if it is a folder, pass rest of path
	 * to it. Continue until we reach filePath is empty - if the path is invalid
	 * or there is a duplicate name where the File should be added, throw an
	 * exception.
	 * 
	 * @param f
	 *            , the non-null File we want to add
	 * @param filePath
	 *            , a non-null ArrayList of Strings representing File/Folder
	 *            names. EXAMPLE: ["folder1", "folder2", "destinationfolder"]
	 * @param currentDate
	 *            , the non-null Date that we are currently adding the file on
	 * @Pre: Root folder is already made, all params are non-null
	 * @Post: File added unless path is wrong or there is a name conflict
	 */
	public void addFile(File f, ArrayList<String> filePath, Date currentDate) {
		root.addFile(f, filePath, currentDate);
	}

	/**
	 * Will save to a File following the filePath array, and then update all of
	 * the sizes and dates. If the path is invalid, throw an exception.
	 * 
	 * @param filePath
	 *            , a non-null, non-empty ArrayList of Strings representing the
	 *            path
	 * @param updatedSize
	 *            , the non-null Integer representing the file size after the
	 *            save
	 * @param saveDate
	 *            , the Date representing the date we are saving on
	 * @Pre: Root folder is already made, all params are non-null
	 * @Post: File updated with new size and dateModified unless path is wrong
	 *        or the new size is not allowed
	 */
	public void saveFile(ArrayList<String> filePath, Integer updatedSize,
			Date saveDate) {
		root.saveFile(filePath, updatedSize, saveDate);
	}

	/**
	 * Will print the FileSystem out, with all of the folders and files nested
	 * correctly
	 * 
	 * @Pre: Root folder is already made
	 * @Post: Subfolders/files will be indented 2 spaces from the parent folder
	 */
	public void printFileSystem() {
		System.out.println(root.print(""));
	}

	/**
	 * Will print out the Disk Usage of each file/folder in a non-sorted way
	 * 
	 * @Pre: Root folder is already made
	 * @Post: All byte sizes will be converted to Gb/Kb/Mb if reasonable
	 */
	public void printDiskUsage() {
		System.out.println("## disk usage with no sorting criterion\n");
		System.out.println(root.printDiskUsage(""));
	}

	/**
	 * Will print out the Disk Usage of each file/folder in a sorted way
	 * 
	 * @param criteria
	 *            , a String indicating whether you want to order by "Name" or
	 *            "Size"
	 * @Pre: Root folder is already made, criteria is non-null and either "Name"
	 *       or "Size"
	 * @Post: Orders by size if indicated, otherwise assumes "Name"
	 */
	public void printDiskUsage(String criteria) {
		TreeMap<String, Integer> sizeMap = root.buildListOfFileSystem(
				new TreeMap<String, Integer>(), "");
		if (criteria.equals("Size")) {
			System.out
					.println("## disk usage sorted by size (smallest to largest)\n");
			SortedSet<Map.Entry<String, Integer>> updatedMap = entriesSortedByValues(sizeMap);
			for (Map.Entry<String, Integer> pair : updatedMap) {
				ArrayList<String> conversion = convertSize(pair.getValue());
				System.out.println(conversion.get(0) + "   "
						+ conversion.get(1) + pair.getKey());
			}
		} else {
			System.out
					.println("## disk usage sorted lexicographically (ignore case)\n");
			for (String path : sizeMap.keySet()) {
				ArrayList<String> conversion = convertSize(sizeMap.get(path));
				System.out.println(conversion.get(0) + "   "
						+ conversion.get(1) + path);
			}
		}

	}

	/**
	 * Will convert the bytes given to Gb/Kb/Mb if reasonable (if the bytes
	 * given can equal at least 1 of any unit, it will convert to the highest
	 * possible choice
	 * 
	 * @param size
	 *            , the Integer size of a file in bytes
	 * @return, a String array containing the Integer value and the String unit
	 * @Pre: size is non-null and in bytes
	 * @Post: Will change to the highest unit that the size is equal to at least
	 *        1 of. EX: size in bytes == 1GB, convert to [1, GB]
	 */
	public ArrayList<String> convertSize(Integer size) {
		String newSize = size + "";
		String unit = "bytes  ";
		ArrayList<String> sizeAndType = new ArrayList<String>();
		if (size >= 536870912) {
			newSize = Math.round((size / 536870912) * 100) / 100 + "";
			unit = "Gb     ";
		} else if (size >= 1048576) {
			newSize = Math.round((size / 1048576) * 100) / 100 + "";
			unit = "Mb     ";
		} else if (size >= 1024) {
			newSize = Math.round((size / 1024) * 100) / 100 + "";
			unit = "Kb      ";
		}
		int spaces = 6 - newSize.length();
		for (int i = 0; i < spaces; i++) {
			newSize += " ";
		}
		sizeAndType.add(newSize);
		sizeAndType.add(unit);
		return sizeAndType;
	}

	/**
	 * Will sort through a given Map and sort it by value
	 * 
	 * @param map
	 *            , a non-null Map
	 * @return the Map, sorted by Value
	 */
	private static <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> entriesSortedByValues(
			Map<K, V> map) {
		SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<Map.Entry<K, V>>(
				new Comparator<Map.Entry<K, V>>() {
					@Override
					public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
						int res = e1.getValue().compareTo(e2.getValue());
						return res != 0 ? res : 1;
					}
				});
		sortedEntries.addAll(map.entrySet());
		return sortedEntries;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((root == null) ? 0 : root.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileSystem other = (FileSystem) obj;
		if (root == null) {
			if (other.root != null)
				return false;
		}
		TreeMap<String, Integer> sizeMapthis = root.buildListOfFileSystem(
				new TreeMap<String, Integer>(), "");
		TreeMap<String, Integer> sizeMapthat = other.root
				.buildListOfFileSystem(new TreeMap<String, Integer>(), "");
		return sizeMapthis.equals(sizeMapthat);
	}

	/**
	 * Will add a series of nested folders starting at the file path destination
	 * 
	 * @param names
	 *            , the non-null ArrayList<Folder> of folders we want to add, in
	 *            order
	 * @param creationDate
	 *            , a non-null Date
	 * @param filePath
	 *            , a non-Null String that is the file path
	 */
	public void addNestedFolders(ArrayList<Folder> names, Date creationDate,
			ArrayList<String> filePath) {
		for (int i = 0; i < names.size(); i++) {
			addFolder(names.get(i), filePath, creationDate);
			filePath.add(names.get(i).getName());
		}
	}

}
