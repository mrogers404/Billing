package com.amica.billing;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class AddCustomer {
	private static final String CSV_FILE = "src/main/java/customers.csv";
	
	public void writeCSV(String outputFile, Customer newCustomer) {
		try (
//				BufferedWriter writer = Files.newBufferedWriter(Paths.get(CSV_FILE));
				BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFile));
				
				CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.POSTGRESQL_CSV.withQuote('"').withHeader("First", "Last", "Terms"));
			)
			{
//			csvPrinter.printRecord("Bob", "Jones", "CREDIT_60");
				csvPrinter.printRecord(newCustomer.getFirstName(), newCustomer.getLastName(), newCustomer.getTerms().toString());
				csvPrinter.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
}
