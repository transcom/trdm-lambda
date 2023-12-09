package com.milmove.trdmlambda.milmove;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import com.milmove.trdmlambda.milmove.config.ApacheProps;
import com.milmove.trdmlambda.milmove.config.TrdmProps;
import com.milmove.trdmlambda.milmove.util.DecodeTruststore;

import ch.qos.logback.classic.Logger;

 // Web server dependency removed, this is an extra precaution
@SpringBootApplication(exclude = { ServletWebServerFactoryAutoConfiguration.class, WebMvcAutoConfiguration.class })
public class TrdmRestApplication {
	private Logger logger = (Logger) LoggerFactory.getLogger(TrdmRestApplication.class);
	@Autowired
	ApacheProps apacheProps;

	@Autowired
	TrdmProps trdmProps;

	@Autowired
	DecodeTruststore decodeTruststore;

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(TrdmRestApplication.class);
		app.setWebApplicationType(WebApplicationType.NONE);
		app.run(args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void writeApacheCxfProps() throws IOException {
		File file = new File(trdmProps.getPropsPath());
		if (!file.exists()) {
			buildFile();
		}
		if (file.exists()) {
			logger.info("File created successfully!");
		} else {
			throw new IOException("Failed to create file: " + file.getAbsolutePath());
		}
	}

	private void buildFile() {
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(trdmProps.getPropsPath()), StandardCharsets.UTF_8))) {
			writer.write(apacheProps.getCryptoProvider() + "=" + apacheProps.getProvider() + "\n");
			writer.write(apacheProps.getMerlinKeystoreType() + "=" + apacheProps.getType() + "\n");
			writer.write(apacheProps.getMerlinKeystorePassword() + "=" + apacheProps.getPassword() + "\n");
			writer.write(apacheProps.getMerlinKeystoreAlias() + "=" + apacheProps.getAlias() + "\n");
			writer.write(apacheProps.getMerlinKeystoreFile() + "=" + apacheProps.getKeystoreFile() + "\n");
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

}
