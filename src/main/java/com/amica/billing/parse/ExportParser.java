package com.amica.billing.parse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.csv.*;

import com.amica.billing.Customer;
import com.amica.billing.Invoice;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.java.Log;

public class ExportParser implements Parser {
	
	public void main() {
		try {
			parseCustomers(new FileReader("src/test/resources/data/customers_export.csv"));
		} catch (Exception e) {
			System.out.print(e);
		}		
		
	}
	
	
	@Override
	public Stream<Customer> parseCustomers(Reader customerReader) {
		
		try {
			org.apache.commons.csv.CSVParser parser = CSVFormat.DEFAULT
		            .withQuote('\'')
		            .withQuoteMode(QuoteMode.ALL)
		            .withNullString("null")
		            .parse(customerReader);
			
			parser.getRecords()
				.stream()
				.forEach(x -> System.out.println(x));
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
