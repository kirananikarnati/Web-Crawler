package com.webcrawler;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class WebCrawler {

	volatile Set<String> success = new HashSet<String>();
	volatile Set<String> skipped = new HashSet<String>();
	volatile Set<String> error = new HashSet<String>();
	static Logger logger = Logger.getLogger(WebCrawler.class.getName());

	public static void main(String[] args) {
		
		WebCrawler webCrawler = new WebCrawler();
		BasicConfigurator.configure();
		Scanner sc = new Scanner(System.in);
		int testCases = sc.nextInt();

		JSONArray pageList = getInputFileData();

		for (int i = 0; i < testCases; i++) {

			String startPage = sc.next();
			webCrawler.init(startPage, pageList);
		}
		sc.close();

	}

	private void init(String startPage, JSONArray pageList) {

		try {

			System.out.println("Start page: ");
			success = new HashSet<String>();
			skipped = new HashSet<String>();
			error = new HashSet<String>();

			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					validateData(pageList, startPage);
					System.out.println("Success:\n" + success);
					System.out.println("\nSkipped:\n" + skipped);
					System.out.println("\nError:\n" + error);

				}
			});
			t.start();

		} catch (

		Exception e) {

			logger.error("Exception occured:: " + e);
		}

	}

	private static JSONArray getInputFileData() {
		try {
			JSONParser jsonParser = new JSONParser();
			FileReader reader = new FileReader("src\\com\\webcrawler\\data\\internet.json");
			JSONObject internetJsonObj = (JSONObject) jsonParser.parse(reader);
			return (JSONArray) internetJsonObj.get("pages");
		} catch (FileNotFoundException e) {

			logger.error("File is not present...");

		} catch (IOException e) {

			logger.error("Error while reading file...");
		} catch (ParseException e) {

			logger.error("Error occured while parsing file...");
		}
		return null;
	}

	private void validateData(JSONArray pageList, String page) {
		try {

			if (success.contains(page)) {
				skipped.add(page);
			} else {
				JSONArray linkedPages = visitPages(pageList, page);
				if (linkedPages != null && linkedPages.size() > 0) {

					success.add(page);
					linkedPages.forEach(items -> {
						try {
							Thread t = new Thread(new Runnable() {

								@Override
								public void run() {
									validateData(pageList, items.toString());
								}
							});
							t.start();
							t.join();
						} catch (Exception e) {
							logger.error("Exception occured:: " + e);

						}
					});

				} else if (linkedPages == null) {
					error.add(page);
				}
			}
		} catch (Exception e) {
			logger.error("Exception occured:: " + e);
		}
	}

	private static JSONArray visitPages(JSONArray pageList, String startPage) {
		try {
			Optional<?> result = pageList.stream()
					.filter(pages -> ((JSONObject) pages).get("address").equals(startPage)).findFirst();
			if (result.isPresent()) {
				return (JSONArray) ((JSONObject) result.get()).get("links");
			}
		} catch (Exception e) {
			logger.error("Exception occured:: " + e);
		}
		return null;
	}
}
