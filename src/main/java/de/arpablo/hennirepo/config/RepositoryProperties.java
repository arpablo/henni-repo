/**
 * Copyright: Armin Pfarr (c) 2018
 */

package de.arpablo.hennirepo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import lombok.Data;

/**
 * @author arpablo
 *
 */
@Configuration
@ConfigurationProperties(prefix="henni.repo", ignoreInvalidFields=false)
@Data
public class RepositoryProperties {

	String basedir;
	String uri;
	
	/**
	 * Set the basedir
	 * @param basedir
	 */
	public void setBasedir(String basedir) {
		if (basedir == null || basedir.equals("")) {
			basedir = System.getProperty("user.home") + "/henni-repo";
		}
		basedir = StringUtils.cleanPath(basedir);
		if (basedir.startsWith("~/")) {
			basedir = System.getProperty("user.home") + basedir.substring(1);
		}
		this.basedir = basedir;
	}
	
	/**
	 * Set the uri
	 * @param uri
	 */
	public void setUri(String uri) {
		while (uri.endsWith("/")) {
			uri = uri.substring(0, uri.length() - 1);
		}
		uri = uri.replace("file://~", "file://"+ System.getProperty("user.home"));
		int index = uri.indexOf("://");
		if (index > 0 && !uri.substring(index + "://".length()).contains("/")) {
			// If there's no context path add one
			uri = uri + "/";
		}
		this.uri = uri;
	}
	
}
