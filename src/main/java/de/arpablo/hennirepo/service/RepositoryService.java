/**
 * Copyright: Armin Pfarr (c) 2018
 */
package de.arpablo.hennirepo.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

import de.arpablo.hennirepo.exception.RepositoryException;
import de.arpablo.hennirepo.model.RepoResource;

/**
 * This interface defines the Repository-API
 * @author arpablo
 *
 */
public interface RepositoryService {

	/**
	 * Return the root directory
	 * @return the root directory as resource
	 */
	public RepoResource getRoot();
	
	/**
	 * Return a Stream source object for the given path. SystemID is set
	 * @param path	the path of the object
	 * @return a StreamSource
	 */
	public Source getSource(String path);
	
	/**
	 * Return a stream result for the given path. SystemID is set
	 * @param path	the path to use
	 * @return a Result
	 */
	public Result getResult(String path) throws IOException;
	
	/**
	 * Return information about the RepoResource at the given path
	 * @param path	the path to query
	 * @return a RepoResource
	 */
	public RepoResource info(String path);
	
	/**
	 * Checks, if the given path exists
	 * @param path the path to check
	 * @return <code>true</code>, if the file exists, <code>false</code> otherwise
	 */
	public boolean exists(String path);
	
	/**
	 * Checks, if the file with the specified path exists
	 * @param path	the path to query
	 * @return <code>true</code>, if the file exists, <code>false</code> otherwise
	 */
	public boolean existsFile(String path);
	
	/**
	 * Checks, if the directory with the specified path exists
	 * @param path	the path to query
	 * @return <code>true</code>, if the directory exists, <code>false</code> otherwise
	 */
	public boolean existsDirectory(String path);
	
	/**
	 * Return the RepoResources that are direct children of
	 * the RepoResource at the given path. This method will throw a
	 * RepositoryException if the RepoResource at the given path isn't a
	 * directory resource.
	 * This method is a shorthand for
	 * <code>
	 * 		list(path, true);
	 * </code>
	 * @param path	the path of the directory resource to query
	 * @return a List of RepoResource instances
	 * @throws RepositoryException
	 */
	public List<RepoResource> list(String path) throws RepositoryException;
	
	/**
	 * Return the RepoResources that are direct children of
	 * the RepoResource at the given path. This method will throw a
	 * RepositoryException if the RepoResource at the given path isn't a
	 * directory resource 
	 * This method is a shorthand for
	 * <code>
	 * 		list(path, true, null);
	 * </code>
	 * @param path	the path of the directory resource to query
	 * @param showHidden if true, the method also returns hidden resources
	 * @return a List of RepoResource instances
	 * @throws RepositoryException
	 */
	public List<RepoResource> list(String path, boolean showHidden) throws RepositoryException;

	/**
	 * Return the RepoResources that are direct children of
	 * the RepoResource at the given path. This method will throw a
	 * RepositoryException if the RepoResource at the given path isn't a
	 * directory resource 
	 * @param path	the path of the directory resource to query
	 * @param showHidden if true, the method also returns hidden resources
	 * @param glob a globber-string to use. If NULL, all files will be returned
	 * @return a List of RepoResource instances
	 * @throws RepositoryException
	 */
	public List<RepoResource> list(String path, boolean showHidden, String glob) throws RepositoryException;
	
	/**
	 * Return an InputStream for the given path
	 * @param path	the path to query
	 * @return an InputStream
	 * @throws RepositoryException
	 */
	public InputStream getContentInputStream(String path) throws RepositoryException;
	
	/**
	 * Write the content at the given path to the provided OutputStream
	 * This method will throw a RepositoryException if the given path does not
	 * identify a file resource
	 * @param path	the path to work on
	 * @param out	the outputstream to write to
	 * @throws RepositoryException
	 */
	public void getContent(String path, OutputStream out) throws RepositoryException;
	
	/**
	 * Write the content of the given inputstream to the resource at the given
	 * path.
	 * This method will throw a RepositoryException if the given path does not
	 * identify a file resource
	 * @param path	the path to work on
	 * @param in	the inputstream with the new content
	 * @return		the RepoResource of the saved content
	 * @throws RepositoryException
	 */
	public RepoResource setContent(String path, InputStream in) throws RepositoryException;
	
	/**
	 * Delete the resource at the given path. If the resource is a directory, it will be deleted recursively
	 * @param path	the path to the resource
	 * @throws RepositoryException
	 */
	public void delete(String path) throws RepositoryException;
	
	/**
	 * Copy the Resource from source to target
	 * @param sourcePath	the source path to copy
	 * @param targetPath	the target path to copy to
	 * @throws RepositoryException
	 */
	public void copy(String sourcePath, String targetPath) throws RepositoryException; 

	/**
	 * Move the Resource from source to target
	 * @param sourcePath	the source path to copy
	 * @param targetPath	the target path to copy to
	 * @throws RepositoryException
	 */
	public void move(String sourcePath, String targetPath) throws RepositoryException; 
	
	/**
	 * Zip the Resource from source to target
	 * @param sourcePath	the source path to zip
	 * @param targetPath	the target path of the zipfile
	 * @return	the RepoResource of the new Zipfile
	 * @throws RepositoryException
	 */
	public RepoResource zip(String sourcePath, String targetPath) throws RepositoryException; 
	
	/**
	 * UnZip the Resource from source to target
	 * @param sourcePath	the source path of the zipfile
	 * @param targetPath	the target path to extract to
	 * @throws RepositoryException
	 */
	public void unzip(String sourcePath, String targetPath) throws RepositoryException; 
	
	
	
	/**
	 * Create a directory with the given path (including all subdirectories)
	 * @param dirPath	the directory path
	 * @return	the RepoResource of the new Resource
	 * @throws RepositoryException
	 */
	public RepoResource createDirectories(String dirPath) throws RepositoryException; 
	
	/**
	 * Create an empty file with the given path
	 * @param path the path of the file
	 * @return a RepoResource
	 * @throws RepositoryException
	 */
	public RepoResource createFile(String path) throws RepositoryException;
	
}