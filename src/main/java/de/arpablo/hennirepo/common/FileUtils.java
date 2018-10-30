/**
 * 
 */
package de.arpablo.hennirepo.common;

import java.io.IOException;
import java.net.URI;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains utility methods for dealing with files
 * @author arpablo
 *
 */
public class FileUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);
	
	/**
	 * Return a Zip-Filesystem for the given filename. If create is true,
	 * create the Filesystem
	 * @param fileName	the name of the file
	 * @param create	if true, creates a new ZipFileSystem
	 * @return a FileSystem
	 * @throws IOException
	 */
	public static FileSystem getZipFileSystem(String fileName, boolean create) throws IOException {
		  // convert the filename to a URI
		  final Path path = Paths.get(fileName);
		  final URI uri = URI.create("jar:" + path.toUri().toString());
		  logger.debug("Creating ZipFile {}", uri.toString());
		  final Map<String, String> env = new HashMap<>();
		  if (create) {
		    env.put("create", "true");
		  }
		  return FileSystems.newFileSystem(uri, env);
	}
	
    /**
     * Copy source file to target location.
     * @param source	the source directory
     * @param target	the target directory 
     * @param okToOverwrite The {@code okToOverwrite} parameter determines if an existing target can be overwritten
     * @param preserveAttributes The {@code preserveAttributes} parameter determines if file attributes should be copied/preserved.
     */
    public static void copyFile(Path source, Path target, boolean okToOverwrite, boolean preserveAttributes) throws IOException {
        CopyOption[] options = (preserveAttributes) ?
            new CopyOption[] { StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING } :
            new CopyOption[] { StandardCopyOption.REPLACE_EXISTING };
        if (Files.notExists(target) || okToOverwrite) {
            Files.copy(source, target, options);
        }
    }    

    /**
     * Copy source directory to target location.
     * @param source	the source directory
     * @param target	the target directory 
     * @param okToOverwrite The {@code okToOverwrite} parameter determines if an existing target can be overwritten
     * @param preserveAttributes The {@code preserveAttributes} parameter determines if file attributes should be copied/preserved.
     */
    public static void copyDirectory(Path source, Path target, boolean okToOverwrite, boolean preserveAttributes) throws IOException {
    	if (!Files.isDirectory(source)) {
    		throw new IOException(String.format("Source Path %s is not a directory", source)) ;
    	}
    	if (Files.exists(target) && !Files.isDirectory(target)) {
    		throw new IOException(String.format("Target Path %s is not a directory", target)) ;
    	}
        EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        TreeCopier tc = new TreeCopier(source, target, okToOverwrite, preserveAttributes);
        Files.walkFileTree(source, opts, Integer.MAX_VALUE, tc);    	
    }
    
    static class TreeCopier implements FileVisitor<Path> {
    	
    	private static Logger logger = LoggerFactory.getLogger(TreeCopier.class);
    	
        private final Path source;
        private final Path target;
        private final boolean okToOverwrite;
        private final boolean preserve;

        
        
        TreeCopier(Path source, Path target, boolean okToOverwrite, boolean preserve) {
            this.source = source;
            this.target = target;
            this.okToOverwrite = okToOverwrite;
            this.preserve = preserve;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            // before visiting entries in a directory we copy the directory
            // (okay if directory already exists).
            CopyOption[] options = (preserve) ?
                new CopyOption[] { StandardCopyOption.COPY_ATTRIBUTES } : new CopyOption[0];

            Path newdir = target.resolve(source.relativize(dir));
            try {
                Files.copy(dir, newdir, options);
            } catch (FileAlreadyExistsException x) {
                // ignore
            } catch (IOException x) {
                logger.info(String.format("Unable to create: %s: %s%n", newdir, x));
                return FileVisitResult.SKIP_SUBTREE;
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        	try {
        		copyFile(file, target.resolve(source.relativize(file)),
                     okToOverwrite, preserve);
        		return FileVisitResult.CONTINUE;
        	} catch (IOException ex) {
                logger.warn(String.format("Unable to copy: %s: %s%n", file, ex.getMessage()));
        		return FileVisitResult.TERMINATE;
        	}
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            // fix up modification time of directory when done
            if (exc == null && preserve) {
                Path newdir = target.resolve(source.relativize(dir));
                try {
                    FileTime time = Files.getLastModifiedTime(dir);
                    Files.setLastModifiedTime(newdir, time);
                } catch (IOException x) {
                    logger.info(String.format("Unable to copy all attributes to: %s: %s%n", newdir, x));
                }
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            if (exc instanceof FileSystemLoopException) {
                logger.error("cycle detected: " + file);
            } else {
                logger.warn(String.format("Unable to copy: %s: %s%n", file, exc));
            }
            return FileVisitResult.CONTINUE;
        }
    }
    
    /**
     * Unzip the given ZipFile to the provides path
     * @param zipFileName	the path to the zipfile to extract
     * @param destDirName		the destination directory
     * @throws IOException
     */
	public static void unzip(String zipFileName, String destDirName) throws IOException {
		final Path destDir = Paths.get(destDirName);
		// if the destination doesn't exist, create it
		if (Files.notExists(destDir)) {
			logger.debug("{} does not exist. Creating...", destDir);
			Files.createDirectories(destDir);
		}

		try (FileSystem zipFileSystem = getZipFileSystem(zipFileName, false)) {
			final Path root = zipFileSystem.getPath("/");

			// walk the zip file tree and copy files to the destination
			Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file,	BasicFileAttributes attrs) throws IOException {
					final Path destFile = Paths.get(destDir.toString(),	file.toString());
					logger.debug("Extracting file {} to {}", file, destFile);
					Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult preVisitDirectory(Path dir,
						BasicFileAttributes attrs) throws IOException {
					final Path dirToCreate = Paths.get(destDir.toString(), dir.toString());
					if (Files.notExists(dirToCreate)) {
						logger.debug("Creating directory {}", dirToCreate);
						Files.createDirectory(dirToCreate);
					}
					return FileVisitResult.CONTINUE;
				}
			});
		}
	}    
    
	
	static void visitZip(String zipFileName, FileVisitor<Path> visitor) throws IOException {

		try (FileSystem zipFileSystem = getZipFileSystem(zipFileName, false)) {
			final Path root = zipFileSystem.getPath("/");

			Files.walkFileTree(root, visitor);
		}
	}
	
    /**
     * Creates/updates a zip file.
     * @param zipFilename the name of the zip to create
     * @param filenames list of filename to add to the zip
     * @throws IOException
     */
	public static void zip(String zipFilename, String... filenames) throws IOException {

		try (FileSystem zipFileSystem = getZipFileSystem(zipFilename, true)) {
//			final Path root = zipFileSystem.getPath("/");

			// iterate over the files we need to add
			for (String filename : filenames) {
				final Path src = Paths.get(filename);
				final Path currentRoot = zipFileSystem.getPath(filename);

				// add a file to the zip file system
				if (!Files.isDirectory(src)) {
					final Path dest = zipFileSystem.getPath(src.getFileName().toString());
					final Path parent = dest.getParent();
					if (parent != null && Files.notExists(parent)) {
						logger.debug("Creating directory {}", parent);
						Files.createDirectories(parent);
					}
					Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
				} else {
					// for directories, walk the file tree
					Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
							final Path dest = currentRoot.relativize(zipFileSystem.getPath(file.toString()));
							Files.createDirectories(dest);
							Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
							return FileVisitResult.CONTINUE;
						}
					});
				}
			}
		}
	}
	

}
