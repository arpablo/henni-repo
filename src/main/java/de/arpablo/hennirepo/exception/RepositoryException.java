/**
 * Copyright: Armin Pfarr (c) 2018
 */
package de.arpablo.hennirepo.exception;

/**
 * This is the baseclass of all exceptions thrown by the
 * Repository-Service
 * @author arpablo
 *
 */
public class RepositoryException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 */
	public RepositoryException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public RepositoryException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public RepositoryException(String message, Throwable cause) {
		super(message, cause);
	}

}