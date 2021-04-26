package com.leggett.mediamaster;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MediamasterApplication implements CommandLineRunner {
	private static Logger LOG = LoggerFactory.getLogger(MediamasterApplication.class);
	private static String PHOTO_ROOT;
	public static void main(String[] args) {
		LOG.info("STARTING THE APPLICATION");
		SpringApplication.run(MediamasterApplication.class, args);
		LOG.info("APPLICATION FINISHED");
	}

	@Override
	public void run(String... args) throws IOException {
		LOG.info("EXECUTING : command line runner");

		for (int i = 0; i < args.length; ++i) {
			LOG.info("args[{}]: {}", i, args[i]);
			PHOTO_ROOT = args[0];
		}

		Set<String> photos = getFiles(PHOTO_ROOT);
		for (String photo : photos) {
			// Generate Thumbnails for all images
				Path path = Paths.get(photo);
				String fullImageFileName = path.getFileName().toString();
				File thumbnailPath = new File("./thumbnails/" + fullImageFileName);
				if (!thumbnailPath.exists()) {
					try {
						BufferedImage image = ImageIO.read(new File(photo));
						image = resizeImage(image, 1080);
						ImageIO.write(image, "jpg", thumbnailPath);
					} catch (IOException e) {
						LOG.error(e.getMessage());
					}
					LOG.info(photo.toString());
				}
		}
	}

	/**
	 * Resize an image to the specified width maintaining aspect ratio
	 * 
	 * @param originalImage
	 * @param targetWidth
	 * @return BufferedImage
	 * @throws Exception
	 */
	BufferedImage resizeImage(BufferedImage originalImage, int targetWidth) {
		BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
		try {
			image = Scalr.resize(originalImage, targetWidth);

		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		return image;
	}

	/**
	 * Retrieve all files recursivley from the specified directory
	 * 
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	public static Set<String> getFiles(String dir) throws IOException {
		Set<String> fileList = new HashSet<>();
		Files.walkFileTree(Paths.get(dir), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (!Files.isDirectory(file)) {
					fileList.add(file.toAbsolutePath().toString());
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
				System.err.printf("Visiting failed for %s\n", file);

				return FileVisitResult.SKIP_SUBTREE;
			}

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				System.out.printf("About to visit directory %s\n", dir);

				return FileVisitResult.CONTINUE;
			}
		});
		return fileList;
	}

}
