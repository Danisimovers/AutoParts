package com.autoparts.autoparts_system;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



class AutopartsSystemApplicationTests {

	private static final Logger log = LogManager.getLogger(AutopartsSystemApplication.class);

	public void logBasicSystemInfo() {
		log.info("Launching the application...");
		log.info("Operating System: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
		log.info("JRE: " + System.getProperty("java.version"));
		log.info("Java Launched From: " + System.getProperty("java.home"));
		log.info("Class Path: " + System.getProperty("java.class.path"));
		log.info("Library Path: " + System.getProperty("java.library.path"));
		log.info("User Home Directory: " + System.getProperty("user.home"));
		log.info("User Working Directory: " + System.getProperty("user.dir"));
		log.info("Test INFO logging.");
	}

	@BeforeEach
	public void setUp() {
		log.info("Setting up test environment...");
	}

	@Test
	void testLogBasicSystemInfo() {
		log.info("Testing logBasicSystemInfo...");
		logBasicSystemInfo();
	}
}
