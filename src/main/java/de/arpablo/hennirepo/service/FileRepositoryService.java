/**
 * Copyright: Armin Pfarr (c) 2018
 */
package de.arpablo.hennirepo.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.arpablo.hennirepo.common.FileUtils;
import de.arpablo.hennirepo.config.RepositoryProperties;
import de.arpablo.hennirepo.exception.InvalidResourceTypeException;
import de.arpablo.hennirepo.exception.RepositoryException;
import de.arpablo.hennirepo.model.RepoResource;



/**
 * @author arp
 *
 */
@Service
public class FileRepositoryService implements RepositoryService {

	private static Logger logger = LoggerFactory.getLogger(RepositoryService.class);
	
	private LinkOption linkOption = LinkOption.NOFOLLOW_LINKS;
	
	private Path root;
	
	@Autowired
	private RepositoryProperties repoConfig;
	
	@PostConstruct
	protected void initialize() {
		String rootPath = repoConfig.getBasedir();
		if (rootPath == null || rootPath.length() == 0) {
			logger.info("Root path is not set in configuration");
			rootPath = String.format("%s", System.getProperty("user.home") + "/repo");
		}
		root = Paths.get(rootPath);
		logger.info(String.format("RepositoryService is using root path '%s'. Directory is %s",root, root.toAbsolutePath().toString()));
		logger.info(String.format("in Filesystem %s", root.getFileSystem()));
	}
	
	/* (non-Javadoc)
	 * @see de.docufy.layouter.service.repo.RepositoryAPI#getRoot()
	 */
	@Override
	public RepoResource getRoot() {
		return new RepoResource(root,"");
	}
	
	/**
	 * Resolve the given path
	 * @param path
	 * @return
	 */
	protected Path resolve(String path) {
		Path p;
		if (path == null || path.length() == 0) {
			p = root;
		} else {
			p = root.resolve(path.startsWith("/")?path.substring(1):path);			
		}
		return p;
	}
	
	/* (non-Javadoc)
	 * @see de.docufy.layouter.service.repo.RepositoryAPI#getSource(java.lang.String)
	 */
	@Override
	public Source getSource(String path) {
		logger.debug(String.format("Retrieving Source for path %s",path));
		Path p = resolve(path);
		StreamSource src = new StreamSource(p.toFile());
		String uri = p.toAbsolutePath().toUri().toString();
		logger.debug("System ID set to {}", uri);
		src.setSystemId(uri);
		return src;
	}

	/* (non-Javadoc)
	 * @see de.docufy.layouter.service.repo.RepositoryAPI#getResult(java.lang.String)
	 */
	@Override
	public Result getResult(String path) throws IOException {
		logger.debug(String.format("Retrieving Result for path %s",path));
		Path p = resolve(path);
		Files.createDirectories(p.getParent());
		StreamResult r = new StreamResult(p.toFile());
		String uri = p.toAbsolutePath().toUri().toString();
		logger.debug("System ID set to {}", uri);
		r.setSystemId(uri);
		return r;
	}

	/* (non-Javadoc)
	 * @see de.docufy.layouter.service.repo.Repository#info(java.lang.String)
	 */
	@Override
	public RepoResource info(String path) {
		Path p = resolve(path);
		logger.debug(String.format("Returning info for path %s which resolved to %s",path, p.toFile().getAbsolutePath()));
		return pathToResource(p, path);
	}

	
	
	/* (non-Javadoc)
	 * @see de.arp.frenzl.commons.repo.RepositoryService#exists(java.lang.String)
	 */
	@Override
	public boolean exists(String path) {
		Path p = resolve(path);
		logger.debug(String.format("Executing exists for path %s which resolved to %s",path, p.toFile().getAbsolutePath()));
		return Files.exists(p, linkOption);
	}

	/* (non-Javadoc)
	 * @see de.docufy.layouter.service.repo.RepositoryAPI#existsFile(java.lang.String)
	 */
	@Override
	public boolean existsFile(String path) {
		Path p = resolve(path);
		logger.debug(String.format("Executing existsFile for path %s which resolved to %s",path, p.toFile().getAbsolutePath()));
		return Files.exists(p, linkOption) && Files.isRegularFile(p, linkOption);
	}

	/* (non-Javadoc)
	 * @see de.docufy.layouter.service.repo.RepositoryAPI#existsDirectory(java.lang.String)
	 */
	@Override
	public boolean existsDirectory(String path) {
		Path p = resolve(path);
		logger.debug(String.format("Executing existsDirectory for path %s which resolved to %s",path, p.toFile().getAbsolutePath()));
		return Files.exists(p, linkOption) && Files.isDirectory(p, linkOption);
	}

	
	/* (non-Javadoc)
	 * @see de.docufy.layouter.service.repo.Repository#list(java.lang.String)
	 */
	@Override
	public List<RepoResource> list(String path) throws RepositoryException {
		return list(path, false, null);
	}

	/* (non-Javadoc)
	 * @see de.docufy.layouter.service.repo.Repository#list(java.lang.String, boolean)
	 */
	@Override
	public List<RepoResource> list(String path, boolean showHidden) throws RepositoryException {
		return list(path, showHidden, null);
	}

	/* (non-Javadoc)
	 * @see de.docufy.layouter.service.repo.Repository#list(java.lang.String, boolean, java.lang.String)
	 */
	@Override
	public List<RepoResource> list(String path, boolean showHidden, String glob) throws RepositoryException {
		ArrayList<RepoResource> ret = new ArrayList<RepoResource>();
		Path p = resolve(path);
		logger.debug(String.format("Listing content for path %s which resolved to %s",path, p.toFile().getAbsolutePath()));
		
		if (Files.isDirectory(p, linkOption)) {
			try (DirectoryStream<Path> stream = (glob != null) ? Files.newDirectoryStream(p, glob) : Files.newDirectoryStream(p)) {
				for (Path file : stream) {
					if (showHidden || !Files.isHidden(file)) {
						String prefix = path.endsWith("/") ? path : path + "/";
						ret.add(pathToResource(file, prefix + file.getFileName().toString()));
					}
				}
			} catch (IOException | DirectoryIteratorException ex) {
				String msg = String.format("Exception listing content of directory %s", path);
				logger.error(msg, ex);
				throw new RepositoryException(msg, ex);
			}
		} else {
			throw new InvalidResourceTypeException(String.format("Path %s does not specifiy a directory", path));
		}
		return ret;
	}

	
	/* (non-Javadoc)
	 * @see de.docufy.repo.service.RepositoryService#getContentInputStream(java.lang.String)
	 */
	@Override
	public InputStream getContentInputStream(String path) throws RepositoryException {
		Path p = resolve(path);
		try {
			return new BufferedInputStream(Files.newInputStream(p));
		} catch (IOException ex) {
			throw new RepositoryException("Failed to create InputStram for resource", ex);
		}
	}

	/* (non-Javadoc)
	 * @see de.docufy.layouter.service.repo.Repository#getContent(java.lang.String, java.io.OutputStream)
	 */
	@Override
	public void getContent(String path, OutputStream out) throws RepositoryException {
		Path p = resolve(path);
		try {
			Files.copy(p, out);
		} catch (IOException ex) {
			logger.error(ex.getClass().getName()+": "+ex.getMessage());
			throw new RepositoryException(ex);
		}
	}

	/* (non-Javadoc)
	 * @see de.docufy.layouter.service.repo.Repository#setContent(java.lang.String, java.io.InputStream)
	 */
	@Override
	public RepoResource setContent(String path, InputStream in)	throws RepositoryException {
		Path p = resolve(path);
		try {
			Files.copy(in, p, StandardCopyOption.REPLACE_EXISTING);
			return pathToResource(p, path);
		} catch (IOException ex) {
			logger.error(ex.getClass().getName()+": "+ex.getMessage());
			throw new RepositoryException(ex);
		}
	}
	
	/* (non-Javadoc)
	 * @see de.docufy.layouter.service.repo.RepositoryAPI#delete(java.lang.String)
	 */
	@Override
	public void delete(String path) throws RepositoryException {
		Path p = resolve(path);
		try {
			if ( Files.isDirectory(p) ) {
				Files.walkFileTree(p, new FileVisitor<Path>() {
					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						logger.debug("Deleting directory: "+dir);
						Files.delete(dir);
						return FileVisitResult.CONTINUE;
					}
		
					@Override
					public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
						return FileVisitResult.CONTINUE;
					}
		
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						logger.debug("Deleting file: "+file);
						Files.delete(file);
						return FileVisitResult.CONTINUE;
					}
		
					@Override
					public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
						return FileVisitResult.CONTINUE;
					}
				});
			} else { 
				Files.delete(p);
			}				
		} catch (IOException ex) {
			logger.error(ex.getClass().getName()+": "+ex.getMessage());
			throw new RepositoryException(ex);
		}
	}

	/* (non-Javadoc)
	 * @see de.docufy.layouter.service.repo.RepositoryAPI#copy(java.lang.String, java.lang.String)
	 */
	@Override
	public void copy(String sourcePath, String targetPath) throws RepositoryException {
		Path pSource = resolve(sourcePath);
		Path pTarget = resolve(targetPath);
		
		logger.debug("Copy {} to {}", pSource.toAbsolutePath().toString(), pTarget.toAbsolutePath().toString());
		
		boolean isSourceDir = Files.isDirectory(pSource);
		boolean isTargetDir = Files.isDirectory(pTarget);
		
		if (isSourceDir) {
			if (!isTargetDir) {
				throw new RepositoryException(String.format("Target path %s does not specify a directory", targetPath));
			} else {
				try {
					FileUtils.copyDirectory(pSource, pTarget.resolve(pSource.getFileName()), true, false);
				} catch (IOException ex) {
					logger.error(ex.getClass().getName()+": "+ex.getMessage());
					throw new RepositoryException(ex);
				}
			}
		} else {
			try {
				FileUtils.copyFile(pSource, (isTargetDir)? pTarget.resolve(pSource.getFileName()) : pTarget, true, false);
			} catch (IOException ex) {
                logger.error(String.format("Unable to copy: %s%n", sourcePath), ex);
				throw new RepositoryException(ex);
			}
		}
	}

	/* (non-Javadoc)
	 * @see de.docufy.layouter.service.repo.RepositoryAPI#move(java.lang.String, java.lang.String)
	 */
	@Override
	public void move(String sourcePath, String targetPath) throws RepositoryException {
		Path pSource = resolve(sourcePath);
		Path pTarget = resolve(targetPath);
		try {
			boolean isDir = Files.isDirectory(pTarget);
			Path pDest = (isDir) ? pTarget.resolve(pSource.getFileName()) : pTarget;
			Files.move(pSource, pDest, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException ex) {
			logger.error(ex.getClass().getName()+": "+ex.getMessage());
			throw new RepositoryException(ex);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see de.docufy.layouter.service.repo.RepositoryAPI#zip(java.lang.String, java.lang.String)
	 */
	@Override
	public RepoResource zip(String sourcePath, String targetPath) throws RepositoryException {
		Path pSource = resolve(sourcePath);
		Path pTarget = resolve(targetPath);

		try {
			logger.info("Zipping {} to {}", pSource.toString(), pTarget.toString());
			FileUtils.zip(pTarget.toString(), pSource.toString());
			return pathToResource(pTarget, targetPath);
		} catch (IOException ex) {
			logger.error(ex.getClass().getName()+": "+ex.getMessage());
			throw new RepositoryException(ex);
		}
	}

	/* (non-Javadoc)
	 * @see de.docufy.layouter.service.repo.RepositoryAPI#unzip(java.lang.String, java.lang.String)
	 */
	@Override
	public void unzip(String sourcePath, String targetPath) throws RepositoryException {
		Path pSource = resolve(sourcePath);
		Path pTarget = resolve(targetPath);

		try {
			FileUtils.unzip(pSource.toString(), pTarget.toString());
		} catch (IOException ex) {
			logger.error(ex.getClass().getName()+": "+ex.getMessage());
			throw new RepositoryException(ex);
		}
	}

	/* (non-Javadoc)
	 * @see de.docufy.layouter.service.repo.RepositoryAPI#createDirectories(java.lang.String)
	 */
	@Override
	public RepoResource createDirectories(String dirPath) throws RepositoryException {
		Path path = resolve(dirPath);
		try {
			path = Files.createDirectories(path);
			return pathToResource(path, dirPath);
		} catch (IOException ex) {
			logger.error(ex.getClass().getName()+": "+ex.getMessage());
			throw new RepositoryException(ex);
		}
	}

	
	/* (non-Javadoc)
	 * @see de.arp.frenzl.commons.repo.RepositoryService#createFile(java.lang.String)
	 */
	@Override
	public RepoResource createFile(String filePath) throws RepositoryException {
		Path path = resolve(filePath);
		try {
			path = Files.createFile(path);
			return pathToResource(path, filePath);
		} catch (IOException ex) {
			logger.error(ex.getClass().getName()+": "+ex.getMessage());
			throw new RepositoryException(ex);
		}
	}

	protected static Calendar toCalendar(FileTime ft) {
		Calendar ct = Calendar.getInstance();
		ct.setTimeInMillis(ft.toMillis());
		return ct;
	}
	
	protected RepoResource pathToResource(Path p, String repositoryPath) {
//		int start = root.normalize().getNameCount();
//		int end = p.normalize().getNameCount();
		RepoResource ret = new RepoResource(p, repositoryPath);
		ret.setExists(Files.exists(p, linkOption));
		ret.setCanRead(Files.isReadable(p));
		ret.setCanWrite(Files.isWritable(p));
		ret.setDirectory(Files.isDirectory(p,  linkOption));
		ret.setFile(Files.isRegularFile(p, linkOption));
		if ( ret.isExists() ) {
			try {
				ret.setHidden(Files.isHidden(p));
				BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class, linkOption);
				ret.setCreationTime(toCalendar(attr.creationTime()));
				ret.setLastAccesTime(toCalendar(attr.lastAccessTime()));
				ret.setLastModifiedTime(toCalendar(attr.lastModifiedTime()));
				ret.setSize(attr.size());
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
				ret.setHidden(true);
			}
		}
		return ret;
	}
	
	
}
