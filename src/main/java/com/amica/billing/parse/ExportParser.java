package com.amica.billing.parse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.csv.*;

import com.amica.billing.Customer;
import com.amica.billing.Invoice;
import com.amica.billing.Terms;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.java.Log;

public class ExportParser implements Parser {
	
	public static void main(String[] args) {
		try {
			ExportParser exparser = new ExportParser();
			Stream<Customer> customers = exparser.parseCustomers(new FileReader("src/test/resources/data/customers_export.csv"));
			customers.forEach(x -> System.out.println(x));
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
			
			return parser.getRecords()
				.stream()
				.map(x -> new Customer(x.get(0), 
						x.get(1),
						x.get(2).equals(Terms.CASH.toString()) ? Terms.CASH : Terms.valueOf(x.get(2))));
				

			
		} catch (Exception e) {
			System.out.print(e);
		}
		
		
		
		return null;
	}

	@Override
	public Stream<Invoice> parseInvoices(Reader invoiceReader, Map<String, Customer> customers) {
//		return new BufferedReader(invoiceReader).lines()
//				.map(line -> parseInvoice(line, customers))
//				.filter(invoice -> invoice != null);
		return null;
	}

}
