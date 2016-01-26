package edu.neu.ccs.cs8674.sp15.seattle.assignment4.problem2;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * This is a class for a Folder, which can contain both folders and files
 * 
 * @author Austin
 *
 */
public class Folder extends FileSystemContent {
	private String name;
	private Integer size;
	private Date creationDate;
	private Date lastModified;
	private ArrayList<FileSystemContent> contents = new ArrayList<FileSystemContent>();
	private ArrayList<String> contentNames = new ArrayList<String>();

	/**
	 * Creates a new empty Folder
	 * 
	 * @param name
	 *            , the non-null String name of the Folder (<= 128 characters)
	 *            Also cannot be the same as one of its subfolders or a folder
	 *            it is a part of
	 * @param creationDate
	 *            , a Date representing the date of creation
	 * @Pre: params != null, name <= 128 characters
	 * @Post: lastModified == creationDate, size == 0
	 */
	public Folder(String name, Date creationDate, Date lastModified) {
		setName(name);
		this.size = 0;
		this.creationDate = creationDate;
		this.lastModified = lastModified;
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
	 * @Pre: name != null, name <= 128 characters
	 * @Post: Changes name, or throws Exception if too big
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
	 *            , a non-null positive Integer size of the folder (<=
	 *            1073741824 bytes)
	 */
	private void setSize(Integer size) {
		if (size <= 1073741824) {
			this.size = size;
		} else {
			throw new RuntimeException(
					"Size cannot be more than 1073741824 bytes!");
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
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isFolder() {
		return true;
	}

	public void addFile(File newFile, ArrayList<String> filePath, Date current) {
		if (filePath.size() == 0) {
			if ((!(contentNames.contains(newFile.getName().toLowerCase())))) {
				setSize(size + newFile.getSize());
				contents.add(newFile);
				contentNames.add(newFile.getName().toLowerCase());
				lastModified = current;
			} else {
				throw new RuntimeException(newFile.getName() + "."
						+ newFile.getExtension() + " already in folder "
						+ filePath + "!");
			}
		} else {
			int matchingFolderIndex = contentNames.indexOf(filePath.get(0)
					.toLowerCase());
			filePath.remove(0);
			if ((matchingFolderIndex == -1)
					|| contents.get(matchingFolderIndex).isFile()) {
				throw new RuntimeException("Invalid file path!");
			} else {
				Folder updated = (Folder) contents.get(matchingFolderIndex);
				updated.addFile(newFile, filePath, current);
				setSize(size + newFile.getSize());
				lastModified = current;
				contents.remove(matchingFolderIndex);
				contents.add(updated);
				contentNames.remove(matchingFolderIndex);
				contentNames.add(updated.getName().toLowerCase());
			}
		}
	}

	/**
	 * Will add a folder to this folder, or if there is a path, will find the
	 * folder and add the folder to it
	 * 
	 * @param newFolder
	 *            , non-null Folder to add
	 * @param filePath
	 *            , an array of Strings (non-null) indicating the file path
	 * @param current
	 *            , the non-null Date of creation for the new Folder
	 * @pre: All params != null
	 * @post: Throws exception if filepath is invalid or if there is a name
	 *        conflict, otherwise will add to correct spot.
	 */
	public void addFolder(Folder newFolder, ArrayList<String> filePath,
			Date current) {
		if (filePath.size() == 0) {
			if ((!(contentNames.contains(newFolder.getName().toLowerCase())))) {
				setSize(size + newFolder.getSize());
				contents.add(newFolder);
				contentNames.add(newFolder.getName().toLowerCase());
				lastModified = current;
			} else {
				throw new RuntimeException(newFolder.getName()
						+ " already in folder " + filePath + "!");
			}
		} else {
			int matchingFolderIndex = contentNames.indexOf(filePath.get(0)
					.toLowerCase());
			filePath.remove(0);
			if ((matchingFolderIndex == -1)
					|| contents.get(matchingFolderIndex).isFile()) {
				throw new RuntimeException("Invalid file path!");
			} else {
				Folder updated = (Folder) contents.get(matchingFolderIndex);
				updated.addFolder(newFolder, filePath, current);
				setSize(size + newFolder.getSize());
				lastModified = current;
				contents.remove(matchingFolderIndex);
				contents.add(updated);
				contentNames.remove(matchingFolderIndex);
				contentNames.add(updated.getName().toLowerCase());
			}
		}
	}

	/**
	 * Will locate the file to save to it, altering its size and lastModified
	 * date
	 * 
	 * @param filePath
	 *            , a non-null String array indicating the file path
	 * @param newSize
	 *            , the non-null/positive Integer byte size the file will have
	 *            upon saving
	 * @param saveDate
	 *            , the non-null Date the save is occuring on
	 * @pre: params != null, filePath is valid, newSize >= 0
	 * @post: Will update all relevant folders sizes on success, throws
	 *        exception if invalid filePath
	 */
	public void saveFile(ArrayList<String> filePath, Integer newSize,
			Date saveDate) {
		if (filePath.size() == 0) {
			throw new RuntimeException("Invalid path to file!");
		} else if (filePath.size() == 1) {
			int matchingFileIndex = contentNames.indexOf(filePath.get(0)
					.toLowerCase());
			filePath.remove(0);
			if ((matchingFileIndex == -1)
					|| contents.get(matchingFileIndex).isFolder()) {
				throw new RuntimeException("Invalid file path!");
			} else {
				File updated = (File) contents.get(matchingFileIndex);
				Integer oldSize = updated.getSize();
				updated.saveFile(newSize, saveDate);
				setSize(size - oldSize + updated.getSize());
				lastModified = saveDate;
				contents.remove(matchingFileIndex);
				contents.add(updated);
				contentNames.remove(matchingFileIndex);
				contentNames.add(updated.getName().toLowerCase());
			}
		} else {
			int matchingFolderIndex = contentNames.indexOf(filePath.get(0)
					.toLowerCase());
			filePath.remove(0);
			if ((matchingFolderIndex == -1)
					|| contents.get(matchingFolderIndex).isFile()) {
				throw new RuntimeException("Invalid file path!");
			} else {
				Folder updated = (Folder) contents.get(matchingFolderIndex);
				Integer oldSize = updated.getSize();
				updated.saveFile(filePath, newSize, saveDate);
				setSize(size - oldSize + updated.getSize());
				lastModified = saveDate;
				contents.remove(matchingFolderIndex);
				contents.add(updated);
				contentNames.remove(matchingFolderIndex);
				contentNames.add(updated.getName().toLowerCase());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((contents == null) ? 0 : contents.hashCode());
		result = prime * result
				+ ((creationDate == null) ? 0 : creationDate.hashCode());
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
		Folder other = (Folder) obj;
		if (contents == null) {
			if (other.contents != null)
				return false;
		} else if (!contents.equals(other.contents))
			return false;
		if (creationDate == null) {
			if (other.creationDate != null)
				return false;
		} else if (!creationDate.equals(other.creationDate))
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
		if (name.equals("/")) {
			return name;
		} else {
			return name + "/";
		}
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
		String result = (creationDate.toString() + " "
				+ lastModified.toString() + sizeAndType.get(0) + "   "
				+ sizeAndType.get(1) + space + toString() + "\n");
		for (int i = 0; i < contents.size(); i++) {
			result = result + contents.get(i).print("  " + space);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public String printDiskUsage(String path) {
		ArrayList<String> sizeAndType = convertSize();
		String result = sizeAndType.get(0) + "   " + sizeAndType.get(1) + path
				+ toString() + "\n";
		for (int i = 0; i < contents.size(); i++) {
			result = result + contents.get(i).printDiskUsage(path + toString());
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public TreeMap<String, Integer> buildListOfFileSystem(
			TreeMap<String, Integer> fsc, String filePath) {
		if (contents.size() != 0) {
			for (int i = 0; i < contents.size(); i++) {
				fsc = contents.get(i).buildListOfFileSystem(fsc,
						filePath + toString());
			}
		}
		fsc.put(filePath + toString(), size);
		return fsc;
	}

}
