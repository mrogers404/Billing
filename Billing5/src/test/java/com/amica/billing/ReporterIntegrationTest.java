package com.amica.billing;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.amica.billing.parse.Parser;
import com.amica.billing.parse.Parser.Format;
import com.amica.format.Formatter;
import com.amica.format.TextFormatter;

public class ReporterIntegrationTest {

	public static final String INPUT_FOLDER = "src/test/resources/data";
	public static final String EXPECTED_FOLDER = "src/test/resources/expected";
	public static final String OUTPUT_FOLDER = "test_reports";
	
	private Formatter formatter = new TextFormatter();
	
	@Before
	public void setUp() throws IOException {
		tearDown();
		File outputFolder = new File(OUTPUT_FOLDER);
		outputFolder.mkdir();
	}
	
	@After
	public void tearDown() throws IOException {
		File outputFolder = new File(OUTPUT_FOLDER);
		if (outputFolder.exists()) {
			for (File report : outputFolder.listFiles()) {
				report.delete();
			}
			outputFolder.delete();
		}
	}
	
	public static void checkReport(String reportName) throws IOException {
		try (
			Stream<String> expectedStream = 
					Files.lines(Paths.get(EXPECTED_FOLDER + "/" + reportName));
				Stream<String> actualStream = 
					Files.lines(Paths.get(OUTPUT_FOLDER + "/" + reportName));
		) {
			String expected = expectedStream.collect(Collectors.joining("\n"));
			String actual = actualStream.collect(Collectors.joining("\n"));
			assertThat(actual, equalTo(expected));
		}
	}

	public void testParseAndReport(String suffix, String prefix, 
			String customerFirst, String customerLast, Parser.Format format) 
			throws IOException {
		
		final String customersFile = INPUT_FOLDER + "/" + "customers" + suffix;
		final String invoicesFile = INPUT_FOLDER + "/" + "invoices" + suffix;

		final String invoicesForCustomerFilename = 
				prefix + "_" + customerFirst + "Invoices_Plain.txt";
		final String invoicesByCustomerFilename = prefix + "_InvoicesByCustomer_Plain.txt";
		final String overdueInvoicesFilename = prefix + "_OverdueInvoices_Plain.txt";
		final String customersByVolumeFilename = prefix + "_CustomersByVolume_Plain.txt";
		
		try (
				FileReader customerReader = new FileReader(customersFile);
				FileReader invoiceReader = new FileReader(invoicesFile);
				FileWriter invoicesForCustomer = 
						new FileWriter(OUTPUT_FOLDER + "/" + invoicesForCustomerFilename);
				FileWriter invoicesByCustomer = 
						new FileWriter(OUTPUT_FOLDER + "/" + invoicesByCustomerFilename);
				FileWriter overdueInvoices = 
						new FileWriter(OUTPUT_FOLDER + "/" + overdueInvoicesFilename);
				FileWriter customersByVolume = 
						new FileWriter(OUTPUT_FOLDER + "/" + customersByVolumeFilename);
		) {
			Reporter reporter = new Reporter
					(customerReader, invoiceReader, formatter, format);
			
			reporter.reportInvoicesForCustomer
			(customerFirst + " " + customerLast, invoicesForCustomer);
			checkReport(invoicesForCustomerFilename);
			
			reporter.reportInvoicesByCustomer(invoicesByCustomer);
			checkReport(invoicesByCustomerFilename);
			
			reporter.reportOverdueInvoices(overdueInvoices, LocalDate.of(2020, 12, 1));
			checkReport(overdueInvoicesFilename);
			
			reporter.reportCustomersByVolume(customersByVolume);
			checkReport(customersByVolumeFilename);
		}
	}
	
	public void testParseAndReport(String suffix, Parser.Format format) 
			throws IOException {
		testParseAndReport(suffix, "CSV", "Janis", "Joplin", format);
	}
	
	@Test
	public void testFromCSV() throws IOException {
		testParseAndReport(".csv", Parser.Format.CSV);
	}
	
	@Test
	public void testFromFlat() throws IOException {
		testParseAndReport(".flat", "Flat", "Myrna", "Loy", Format.FLAT);
	}
}
