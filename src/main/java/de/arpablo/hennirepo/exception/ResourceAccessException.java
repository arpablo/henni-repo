/**
 * Copyright: Armin Pfarr (c) 2018
 */
package de.arpablo.hennirepo.exception;

/**
 * This exception is thrown, if an attempt is made to access a
 * non-existing resource
 * @author arpablo
 *
 */
public class ResourceAccessException extends RepositoryException {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor
	 * @param message the exception message
	 */
	public ResourceAccessException(String message) {
		super(message);
	}


}