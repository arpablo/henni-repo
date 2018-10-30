/**
 * Copyright: Armin Pfarr (c) 2018
 */
package de.arpablo.hennirepo.api;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import de.arpablo.hennirepo.exception.InvalidResourceTypeException;
import de.arpablo.hennirepo.exception.ResourceAccessException;
import de.arpablo.hennirepo.model.RepoResource;
import de.arpablo.hennirepo.service.RepositoryService;

/**
 * @author arpablo
 *
 */
@RequestMapping(RepositoryAPI.CURRENT_API)
@RestController
public class RepositoryAPI {

	static final String CURRENT_API = "/api/repo/v1";
	private static final Logger log = LoggerFactory.getLogger(RepositoryAPI.class);

	@Autowired
	private RepositoryService service;
	
	@GetMapping(value="/**", produces="application/json")
	public @ResponseBody RepoResource info(HttpServletRequest request) {
		String path = getRequestURI(request);
		return service.info(path);		
	}

	@PutMapping(value="/**", params="folder", produces="application/json")
	public @ResponseBody RepoResource createFolder(HttpServletRequest request) {
		String path = getRequestURI(request);
		return service.createDirectories(path);		
	};
	
	@PutMapping(value="/**", params="file", produces="application/json")
	public @ResponseBody RepoResource createFile(HttpServletRequest request) {
		String path = getRequestURI(request);
		return service.createFile(path);		
	};

	/**
	 * Upload a resource with an HTTP PUT request. This is only suitable for
	 * small resources
	 * 
	 * @param request	the request
	 * @param requestEntity	the content
	 * @return a RepoResource
	 */
	@PutMapping(value="/**",produces="application/json")
	public  @ResponseBody RepoResource setContent(HttpServletRequest request, HttpEntity<byte[]> requestEntity) {
		String path = getRequestURI(request);
		 byte[] payload = requestEntity.getBody();
		 if (payload == null) {
			 return service.createFile(path);
		 }
		 InputStream in = new ByteArrayInputStream(payload);
		 return service.setContent(path, in);
	}

	/**
	 * Return the content of the given resource. This will throw a RepositoryException
	 * if the resource is a folder or not readable 
	 * @param request
	 * @return an InputStream
	 */
	@GetMapping(value="/**", params="content")
	public ResponseEntity<InputStreamResource> getContent(HttpServletRequest request) {
		
		String path = getRequestURI(request);
		
		RepoResource res = service.info(path);
		if (!res.isExists() || !res.isCanRead() ) {
			throw new ResourceAccessException("Cannot read content of resource " + path);
		}
		if (res.isDirectory()) {
			throw new InvalidResourceTypeException("Path '" + path + "' qualifies a directory");
		}
		InputStream in = service.getContentInputStream(path);
		
		HttpHeaders respHeaders = new HttpHeaders();
		respHeaders.setContentType(MediaType.parseMediaType(getMimeType(request, path)));
		respHeaders.setContentLength(res.getSize());
		respHeaders.setContentDispositionFormData("attachment", res.getName());
		InputStreamResource isr = new InputStreamResource(in);
		return new ResponseEntity<InputStreamResource>(isr, respHeaders, HttpStatus.OK);	
	}
	
	@PutMapping(value="/**", params="zip")
	public @ResponseBody RepoResource zipResource(HttpServletRequest request) {
		String path = getRequestURI(request);
		
		RepoResource res = service.info(path);
		if (!res.isExists() || !res.isCanRead() ) {
			throw new ResourceAccessException("Cannot access resource " + path);
		}
		String targetPath = path + ".zip";
		if (res.isDirectory()) {
			targetPath = res.getParentPath();
			if (targetPath == null) {
				targetPath = "/Archive.zip";
			} else {
				targetPath = targetPath + "/" + res.getName() + ".zip";
			}
		}
		log.debug("Zipping resource {} to targetPath {}", path, targetPath);
		return service.zip(path,targetPath);
	}
	
	@DeleteMapping(value="/**", produces="application/json")
	public @ResponseBody boolean delete(HttpServletRequest request) {
		String path = getRequestURI(request);
		try {
			service.delete(path);
			return true;
		} catch (Exception ex) {
			return false;
		}		
	}
	
	@GetMapping(value="/**", params="list", produces="application/json")
	public @ResponseBody List<RepoResource> list(HttpServletRequest request) {
		String path = getRequestURI(request);
		return service.list(path);
	}
	
	
	public static String getRequestURI(HttpServletRequest request) {
		final String uri = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
	    final String bestMatchPattern = (String ) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
	    AntPathMatcher apm = new AntPathMatcher();
	    String finalPath = apm.extractPathWithinPattern(bestMatchPattern, uri);
	    return finalPath;
	}
	
	protected String getMimeType(HttpServletRequest request, String fileName) {
		String retval = "application/octet-stream";
		if (fileName == null) {
			return retval;
		} else {
			String s = request.getSession().getServletContext().getMimeType(fileName);
			if ( s != null && s.length() > 0 ) {
				return s;
			}
			return retval;
		}
	}
	
}
