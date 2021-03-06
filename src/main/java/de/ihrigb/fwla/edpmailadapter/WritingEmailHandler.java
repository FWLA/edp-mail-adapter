package de.ihrigb.fwla.edpmailadapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import de.ihrigb.fwla.edpmailadapter.Properties.WritingProperties;
import de.ihrigb.fwla.edpmailadapter.ValueExtraction.Value;
import de.ihrigb.fwla.mail.Email;
import de.ihrigb.fwla.mail.EmailHandler;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class WritingEmailHandler implements EmailHandler<String> {

	private String currentDateTime() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd_HH-mm-ss");
		return formatter.format(LocalDateTime.now());
	}

	private final WritingProperties properties;

	@Override
	public void handle(Email<String> email) {

		Set<Value> values = ValueExtraction.extract(email);

		try {
			File tempFile = File.createTempFile("edp_", ".txt");
			log.debug("Writing to tempfile {}.", tempFile.getAbsolutePath());
			try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
					PrintStream printStream = new PrintStream(fileOutputStream)) {
				for (Value value : values) {
					printStream.println(String.format("%s=%s", value.getName(), value.getValue()));
				}
			}

			String filename = String.format("%s.txt", currentDateTime());
			File parentDirectory = new File(properties.getDirectory());
			File targetFile = new File(parentDirectory, filename);

			log.info("Target file {}.", targetFile.getAbsolutePath());

			FileUtils.copyFile(tempFile, targetFile);
			tempFile.delete();
		} catch (IOException e) {
			log.error("Exception during file writing.", e);
		}
	}
}
