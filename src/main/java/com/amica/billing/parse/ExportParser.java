package com.amica.billing.parse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.csv.*;

import com.amica.billing.Customer;
import com.amica.billing.Invoice;
import com.amica.billing.Terms;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.java.Log;

public class ExportParser implements Parser {
	
	org.apache.commons.csv.CSVParser parser;
	
	public static void main(String[] args) {
		try {
			ExportParser exparser = new ExportParser();
			Stream<Customer> customers = exparser.parseCustomers(new FileReader("src/test/resources/data/customers_export.csv"));
			Map<String, Customer> map = new HashMap();
			customers.forEach(x -> map.put(x.getName(), x));
			
			Stream<Invoice> invoices = exparser.parseInvoices(new FileReader("src/test/resources/data/invoices_export.csv"), map);
			invoices.forEach(x -> System.out.println(x));
			
		} catch (Exception e) {
			System.out.print(e);
		}		
		
	}
	
	
	@Override
	public Stream<Customer> parseCustomers(Reader customerReader) {
		
		try {
			org.apache.commons.csv.CSVParser parser = CSVFormat.DEFAULT
		            .withQuote('"')
		            .withQuoteMode(QuoteMode.ALL)
		            .withNullString("null")
		            .withFirstRecordAsHeader()
		            .parse(customerReader);
			
			List<CSVRecord> records = parser.getRecords();
			
			return records.stream()
				.map(x -> new Customer(x.get(0), 
						x.get(1),
						x.get(2).equals(Terms.CASH.toString()) ? Terms.CASH : Terms.valueOf(x.get(2))));
			
		} catch (Exception e) {
			System.out.print(e);
		}		
		
		return null;
	}

	@Override
	public Stream<Invoice> parseInvoices(Reader invoiceReader, Map<String, Customer> customers){
		DateTimeFormatter dtParser = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		
			try {
				 parser = CSVFormat.DEFAULT
				        .withQuote('"')
				        .withQuoteMode(QuoteMode.ALL)
					    .withNullString("null")
					    .withFirstRecordAsHeader()
					    .parse(invoiceReader);
					} catch (Exception e) {
						e.printStackTrace();
					}
			try {
				return parser.getRecords()
						.stream()
						.map(x -> new Invoice(Integer.parseInt(x.get(0)), customers.get(x.get(1) + " " + x.get(2)), Double.valueOf(x.get(3)), LocalDate.parse(x.get(4), dtParser), x.get(5).equals("NULL")? null :LocalDate.parse(x.get(5), dtParser)));
			} catch(Exception e) {
				e.printStackTrace();
			}
			
		return null;
	}

}
	
