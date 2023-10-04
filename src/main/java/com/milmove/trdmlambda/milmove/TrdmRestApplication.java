package com.milmove.trdmlambda.milmove;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import com.milmove.trdmlambda.milmove.config.ApacheProps;
import com.milmove.trdmlambda.milmove.config.TrdmProps;

@SpringBootApplication
public class TrdmRestApplication {
	@Autowired
	ApacheProps apacheProps;

	@Autowired
	TrdmProps trdmProps;

	public static void main(String[] args) {
		SpringApplication.run(TrdmRestApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void writeApacheCxfProps() {
		File file = null;
		try {
			file = new File(trdmProps.getPropsPath());
			if (!file.exists()) {
				buildFile();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	private void buildFile() {
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(trdmProps.getPropsPath()), "utf-8"))) {
			writer.write(apacheProps.getCryptoProvider() + "=" + apacheProps.getProvider() + "\n");
			writer.write(apacheProps.getMerlinKeystoreType() + "=" + apacheProps.getType() + "\n");
			writer.write(apacheProps.getMerlinKeystorePassword() + "=" + apacheProps.getPassword() + "\n");
			writer.write(apacheProps.getMerlinKeystoreAlias() + "=" + apacheProps.getAlias() + "\n");
			writer.write(apacheProps.getMerlinKeystoreFile() + "=" + apacheProps.getKeystoreFile() + "\n");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

}
