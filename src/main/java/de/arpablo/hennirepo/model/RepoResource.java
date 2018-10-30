/**
 * Copyright: Armin Pfarr (c) 2018
 */
package de.arpablo.hennirepo.model;

import java.io.Serializable;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRootName;


/**
 * This class encapsulates a resource maintained by the 
 * repository
 * @author arp
 *
 */
@JsonRootName(value="Resource")
public class RepoResource implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@JsonIgnore
	private Path path;
	private String repositoryPath;
	private boolean exists = false;
	private boolean canRead = false;
	private boolean canWrite = false;
	private boolean isFile = false;
	private boolean isDirectory = false;
	private boolean isHidden = false;
	
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ")	
	private Calendar creationTime;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ")	
	private Calendar lastAccesTime;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ")	
	private Calendar lastModifiedTime;
	private long size;
	
	/**
	 * Constructor
	 * @param path
	 * @param relativePath the path relative to the repository root
	 */
	public RepoResource(Path path, String relativePath) {
		this.path = path;
		this.repositoryPath = relativePath;
	}
	
	@JsonIgnore
	public String getShortInfo() {
		StringBuffer sb = new StringBuffer();
		sb.append(isDirectory ? "d" : "-");
		sb.append(isCanRead() ? "r" : "-");
		sb.append(isCanWrite() ? "w" : "-");
		sb.append(isHidden() ? "h" : "-");
		return sb.toString();
	}
	
	@JsonIgnore
	public String getParentPath() {
		int last = repositoryPath.lastIndexOf("/");
		if (last <= 0) { // not found or at position 0
			return null;
		} else {
			return repositoryPath.substring(0, last);
		}
	}
	
	/**
	 * @return the path
	 */
	public Path getPath() {
		return path;
	}

	/**
	 * @return the name (the last part of the path)
	 */
	public String getName() {
		return path.getFileName().toString();
	}

	
	/**
	 * @return the relativePath
	 */
	public String getRepositoryPath() {
		return repositoryPath;
	}

	/**
	 * @param relativePath the relativePath to set
	 */
	public void setRepositoryPath(String relativePath) {
		this.repositoryPath = relativePath;
	}

	/**
	 * Return the full qualified URI of the resource
	 * @return a String
	 */
	@JsonIgnore
	public String getAbsoluteURI() {
		return path.toUri().toString();
	}
	
	/**
	 * @param path the path to set
	 */
	public RepoResource setPath(Path path) {
		this.path = path;
		return this;
	}

	
	protected void reset() {
		this.exists = false;
		this.canRead = false;
		this.canWrite = false;
		this.isFile = false;
		this.isDirectory = false;
		this.isHidden = false;
	}
	
	/**
	 * @return the exists
	 */
	public boolean isExists() {
		return exists;
	}
	/**
	 * @param exists the exists to set
	 */
	public RepoResource setExists(boolean exists) {
		this.exists = exists;
		return this;
	}
	/**
	 * @return the canRead
	 */
	public boolean isCanRead() {
		return canRead;
	}
	/**
	 * @param canRead the canRead to set
	 */
	public RepoResource setCanRead(boolean canRead) {
		this.canRead = canRead;
		return this;
	}
	/**
	 * @return the canWrite
	 */
	public boolean isCanWrite() {
		return canWrite;
	}
	/**
	 * @param canWrite the canWrite to set
	 */
	public RepoResource setCanWrite(boolean canWrite) {
		this.canWrite = canWrite;
		return this;
	}
	/**
	 * @return the isFile
	 */
	public boolean isFile() {
		return isFile;
	}
	/**
	 * @param isFile the isFile to set
	 */
	public RepoResource setFile(boolean isFile) {
		this.isFile = isFile;
		return this;
	}
	/**
	 * @return the isDirectory
	 */
	public boolean isDirectory() {
		return isDirectory;
	}
	/**
	 * @param isDirectory the isDirectory to set
	 */
	public RepoResource setDirectory(boolean isDirectory) {
		this.isDirectory = isDirectory;
		return this;
	}
	/**
	 * @return the isHidden
	 */
	public boolean isHidden() {
		return isHidden;
	}
	/**
	 * @param isHidden the isHidden to set
	 */
	public RepoResource setHidden(boolean isHidden) {
		this.isHidden = isHidden;
		return this;
	}

	/**
	 * @return the creationTime
	 */
	public Calendar getCreationTime() {
		return creationTime;
	}

	/**
	 * Return a String representaion of the creation time using
	 * the provided pattern
	 * @param pattern a SimpleDateFormat pattern
	 * @return a String
	 */
	public String getCreationTime(String pattern) {
		String ret = new SimpleDateFormat(pattern).format(this.creationTime);
		return ret;
	}
	
	/**
	 * @param creationTime the creationTime to set
	 */
	public void setCreationTime(Calendar creationTime) {
		this.creationTime = creationTime;
	}

	/**
	 * @return the lastAccesTime
	 */
	public Calendar getLastAccesTime() {
		return lastAccesTime;
	}

	/**
	 * Return a String representaion of the last access time using
	 * the provided pattern
	 * @param pattern a SimpleDateFormat pattern
	 * @return a String
	 */
	public String getLastAccessTime(String pattern) {
		return new SimpleDateFormat(pattern).format(this.lastAccesTime);
	}
	
	/**
	 * @param lastAccesTime the lastAccesTime to set
	 */
	public void setLastAccesTime(Calendar lastAccesTime) {
		this.lastAccesTime = lastAccesTime;
	}

	/**
	 * @return the lastModifiedTime
	 */
	public Calendar getLastModifiedTime() {
		return lastModifiedTime;
	}

	/**
	 * Return a String representaion of the last modified time using
	 * the provided pattern
	 * @param pattern a SimpleDateFormat pattern
	 * @return a String
	 */
	public String getLastModifiedTime(String pattern) {
		return new SimpleDateFormat(pattern).format(this.lastModifiedTime);
	}
	

	/**
	 * @param lastModifiedTime the lastModifiedTime to set
	 */
	public void setLastModifiedTime(Calendar lastModifiedTime) {
		this.lastModifiedTime = lastModifiedTime;
	}

	/**
	 * @return the size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(long size) {
		this.size = size;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RepoResource other = (RepoResource) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

	
}