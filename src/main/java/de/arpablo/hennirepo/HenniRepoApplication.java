/**
 * Copyright: Armin Pfarr (c) 2018
 */

package de.arpablo.hennirepo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * @author arpablo
 *
 */
@SpringBootApplication
public class HenniRepoApplication extends SpringBootServletInitializer {

	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(HenniRepoApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(HenniRepoApplication.class, args);
	}

}
