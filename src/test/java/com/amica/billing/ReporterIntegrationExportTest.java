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

public class ReporterIntegrationExportTest {

	public static final String INPUT_FOLDER = "src/test/resources/data";
	public static final String EXPECTED_FOLDER = "src/test/resources/expected";
	public static final String OUTPUT_FOLDER = "export_test_reports";
	
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
		
		final String customersExportFile = INPUT_FOLDER + "/" + "customers_export" + suffix;
		final String invoicesExportFile = INPUT_FOLDER + "/" + "invoices_export" + suffix;

		final String invoicesForCustomerExportFilename = 
				prefix + "_" + customerFirst + "InvoicesExport_Plain.txt";
		final String invoicesByCustomerExportFilename = prefix + "_InvoicesByCustomerExport_Plain.txt";
		final String overdueInvoicesExportFilename = prefix + "_OverdueInvoicesExport_Plain.txt";
		final String customersByVolumeExportFilename = prefix + "_CustomersByVolumeExport_Plain.txt";
		
		try (
				FileReader customerExportReader = new FileReader(customersExportFile);
				FileReader invoiceExportReader = new FileReader(invoicesExportFile);
				
				FileWriter invoicesForCustomerExport = 
						new FileWriter(OUTPUT_FOLDER + "/" + invoicesForCustomerExportFilename);
				FileWriter invoicesByCustomerExport = 
						new FileWriter(OUTPUT_FOLDER + "/" + invoicesByCustomerExportFilename);
				FileWriter overdueInvoicesExport = 
						new FileWriter(OUTPUT_FOLDER + "/" + overdueInvoicesExportFilename);
				FileWriter customersByVolumeExport = 
						new FileWriter(OUTPUT_FOLDER + "/" + customersByVolumeExportFilename);
		) {
			Reporter reporterExport = new Reporter(customerExportReader, invoiceExportReader, formatter, format);
			
			reporterExport.reportInvoicesForCustomer(customerFirst + " " + customerLast, invoicesForCustomerExport);
			checkReport(invoicesForCustomerExportFilename);
			
			reporterExport.reportInvoicesByCustomer(invoicesByCustomerExport);
			checkReport(invoicesByCustomerExportFilename);
			
			reporterExport.reportOverdueInvoices(overdueInvoicesExport, LocalDate.of(2020, 12, 1));
			checkReport(overdueInvoicesExportFilename);
			
			reporterExport.reportCustomersByVolume(customersByVolumeExport);
			checkReport(customersByVolumeExportFilename);
			
		}
	}
	
	public void testParseAndReport(String suffix, Parser.Format format) 
			throws IOException {
		testParseAndReport(suffix, "CSV", "Janis", "Joplin", format);
	}
	
	@Test
	public void testFromCSV() throws IOException {
		testParseAndReport(".csv", Parser.Format.EXPORT);
	}
	
	/*
	@Test
	public void testFromFlat() throws IOException {
		testParseAndReport(".flat", "Flat", "Myrna", "Loy", Format.FLAT);
	}
	*/
}

