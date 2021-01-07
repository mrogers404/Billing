package com.amica.billing;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.amica.billing.parse.ExportParser;

public class AddCustomer {
	private static final String CUSTOMER_FILE = "data/customers_export.csv";
	private static final String INVOICE_FILE = "data/invoices_export.csv";
	
	public static void main(String[] args) {
		AddCustomer addCustomer = new AddCustomer();
		addCustomer.payInvoice(107);
	}
	
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
	
	public void payInvoice(int number) {
		ExportParser exParse = new ExportParser();
		Map<String, Customer> custMap = new HashMap();
		try {
			Stream<Customer> custStream = exParse.parseCustomers(new FileReader(CUSTOMER_FILE));
			custStream.forEach(x -> custMap.put(x.getName(), x));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		List<Invoice> invoiceList = new ArrayList();
		try {
			invoiceList = exParse.parseInvoices(new FileReader(INVOICE_FILE), custMap).collect(Collectors.toList());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}	
		for(Invoice invoice: invoiceList) {
			if(invoice.getNumber() == number) {
				invoice.setPaidDate(LocalDate.now());
			}
			System.out.println(invoice);
		}
		writeInvoiceCSV(invoiceList.stream());
	}
	
	public void writeInvoiceCSV(Stream<Invoice> invoices) {
		String path = INVOICE_FILE;
		//String path = "src/main/java/invoices_export_TEST.csv";
		try {
			BufferedWriter writer = Files.newBufferedWriter(Paths.get(path));
			CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.POSTGRESQL_CSV.withQuote('"').withHeader("Number", "CustomerFirst", "CustomerLast", "Amount", "Date", "Paid"));
			invoices.forEach(x ->
			{
				try {
					csvPrinter.printRecord(x.getNumber(), x.getCustomer().getFirstName(), x.getCustomer().getLastName(), x.getAmount(), x.getTheDate(), Objects.isNull(x.getPaidDate()) ? "NULL" : x.getPaidDate());
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			csvPrinter.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
}
