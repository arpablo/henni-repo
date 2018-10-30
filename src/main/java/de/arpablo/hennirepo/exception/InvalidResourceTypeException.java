/**
 * Copyright: Armin Pfarr (c) 2018
 */
package de.arpablo.hennirepo.exception;

/**
 * @author arpablo
 *
 */
public class InvalidResourceTypeException extends RepositoryException {

	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 */
	public InvalidResourceTypeException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public InvalidResourceTypeException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InvalidResourceTypeException(String message, Throwable cause) {
		super(message, cause);
	}

}