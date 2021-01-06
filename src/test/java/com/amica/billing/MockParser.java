package com.amica.billing;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.amica.billing.parse.Parser;

public class MockParser implements Parser {

	public static String customerInput;
	public static String invoiceInput;
	public static Map<String,Customer> customerMapInput;
	
	public static Stream<Customer> customers;
	public static Stream<Invoice> invoices;
	
	
	public Stream<Customer> parseCustomers(Reader customerReader) {
		customerInput = new BufferedReader(customerReader).lines()
				.collect(Collectors.joining("\n"));
		return customers;
	}
	
	public Stream<Invoice> parseInvoices(Reader invoiceReader, 
			Map<String,Customer> customers) {
		invoiceInput = new BufferedReader(invoiceReader).lines()
				.collect(Collectors.joining("\n"));
		customerMapInput = customers;
		return invoices;
	}
}
