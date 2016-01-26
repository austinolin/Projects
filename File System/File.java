package edu.neu.ccs.cs8674.sp15.seattle.assignment4.problem2;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * This is a class for a File, which is a file in a file system
 * 
 * @author Austin
 */
public class File extends FileSystemContent {
	private String name;
	private Integer size;
	private String extension;
	private Date creationDate;
	private Date lastModified;

	/**
	 * Creates a new File
	 * 
	 * @param name
	 *            , the non-null String name of the File (<= 128 characters)
	 * @param size
	 *            , the non-null positive Integer size in bytes of the file (<
	 *            104857600 bytes)
	 * @param extension
	 *            , String that can be either "txt", "cfg", "res", or "bin"
	 * @param creationDate
	 *            , a non-null Date for when the file was created
	 * @pre: params != null, extension must match 4 described choices, size and
	 *       name within limit
	 * @post: lastModified == creationdate initially, exception thrown if name,
	 *        size, or extension are invalid
	 */
	public File(String name, Integer size, String extension, Date creationDate) {
		setName(name);
		setSize(size);
		setExtension(extension);
		this.creationDate = creationDate;
		this.lastModified = creationDate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Will set the name to the given name
	 * 
	 * @param name
	 *            , the non-null String name of the File (<= 128 characters)
	 * @pre: name <= 128 characters
	 * @post: Will change name, or throw exception if too long
	 */
	private void setName(String name) {
		if (name.length() <= 128) {
			this.name = name;
		} else {
			throw new RuntimeException(
					"Name must be no longer than 128 characters!");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer getSize() {
		return size;
	}

	/**
	 * Will set the size to the given size
	 * 
	 * @param size
	 *            , the non-null positive Integer size in bytes of the file (<
	 *            104857600 bytes)
	 * @pre: size != null and <= 104857600
	 * @post: changes size or throws exception if size is too big
	 */
	private void setSize(Integer size) {
		if (size <= 104857600) {
			this.size = size;
		} else {
			throw new RuntimeException(
					"Size cannot be more than 104857600 bytes!");
		}
	}

	/**
	 * Asks for the extension of the file
	 * 
	 * @return the String extension
	 */
	public String getExtension() {
		return extension;
	}

	/**
	 * Sets the extension of the file to the given extension
	 * 
	 * @param extension
	 *            , String that can be either "txt", "cfg", "res", or "bin"
	 * @pre: extension must match one of the 4 choices, extension != null
	 * @post: sets extension, or throws exception if invalid
	 */
	private void setExtension(String extension) {
		if (extension.equals("txt") || extension.equals("cfg")
				|| extension.equals("res") || extension.equals("bin")) {
			this.extension = extension;
		} else {
			throw new RuntimeException(extension
					+ " is not a valid file extension");
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date getLastModified() {
		return lastModified;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isFile() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isFolder() {
		return false;
	}

	/**
	 * Will save the file, altering its size and last modification date
	 * 
	 * @param newSize
	 *            , non-null positive Integer size of the file after the Save
	 * @param saveDate
	 *            , the non-null Date of the save
	 * @pre: params != null, size < limit in setSize()
	 * @post: will update File, or throw exception if size is too big
	 */
	public void saveFile(Integer newSize, Date saveDate) {
		setSize(newSize);
		lastModified = saveDate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((creationDate == null) ? 0 : creationDate.hashCode());
		result = prime * result
				+ ((extension == null) ? 0 : extension.hashCode());
		result = prime * result
				+ ((lastModified == null) ? 0 : lastModified.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((size == null) ? 0 : size.hashCode());
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
		File other = (File) obj;
		if (creationDate == null) {
			if (other.creationDate != null)
				return false;
		} else if (!creationDate.equals(other.creationDate))
			return false;
		if (extension == null) {
			if (other.extension != null)
				return false;
		} else if (!extension.equals(other.extension))
			return false;
		if (lastModified == null) {
			if (other.lastModified != null)
				return false;
		} else if (!lastModified.equals(other.lastModified))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (size == null) {
			if (other.size != null)
				return false;
		} else if (!size.equals(other.size))
			return false;
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return name + "." + extension;
	}

	/**
	 * {@inheritDoc}
	 */
	public ArrayList<String> convertSize() {
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
		int spaces = 5 - newSize.length();
		for (int i = 0; i < spaces; i++) {
			newSize = " " + newSize;
		}
		sizeAndType.add(newSize);
		sizeAndType.add(unit);
		return sizeAndType;
	}

	/**
	 * {@inheritDoc}
	 */
	public String print(String space) {
		ArrayList<String> sizeAndType = convertSize();
		return creationDate.toString() + " " + lastModified.toString()
				+ sizeAndType.get(0) + "   " + sizeAndType.get(1) + space
				+ toString() + "\n";
	}

	/**
	 * {@inheritDoc}
	 */
	public String printDiskUsage(String path) {
		ArrayList<String> sizeAndType = convertSize();
		return sizeAndType.get(0) + "   " + sizeAndType.get(1) + path
				+ toString() + "\n";
	}

	/**
	 * {@inheritDoc}
	 */
	public TreeMap<String, Integer> buildListOfFileSystem(
			TreeMap<String, Integer> fsc, String filePath) {
		fsc.put(filePath + toString(), size);
		return fsc;
	}

}
