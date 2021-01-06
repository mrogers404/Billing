package com.amica.billing.parse;

import java.io.BufferedReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Stream;

import com.amica.billing.Customer;
import com.amica.billing.Invoice;
import com.amica.billing.Terms;

import lombok.SneakyThrows;
import lombok.extern.java.Log;

/**
 * A parser that can read a specific flat file format.
 * 
 * @author Will Provost
 */
 @Log
public class FlatParser implements Parser {

	private static final int CUSTOMER_FIRST_NAME_OFFSET = 0;
	private static final int CUSTOMER_FIRST_NAME_LENGTH = 12;
	private static final int CUSTOMER_LAST_NAME_OFFSET = 
			CUSTOMER_FIRST_NAME_OFFSET + CUSTOMER_FIRST_NAME_LENGTH;
	private static final int CUSTOMER_LAST_NAME_LENGTH = 12;
	private static final int CUSTOMER_TERMS_OFFSET = 
			CUSTOMER_LAST_NAME_OFFSET + CUSTOMER_LAST_NAME_LENGTH;
	private static final int CUSTOMER_TERMS_LENGTH = 10;
	private static final int CUSTOMER_LENGTH = 
			CUSTOMER_TERMS_OFFSET + CUSTOMER_TERMS_LENGTH;

	private static final int INVOICE_NUMBER_OFFSET = 0;
	private static final int INVOICE_NUMBER_LENGTH = 4;
	private static final int INVOICE_FIRST_NAME_OFFSET = 
			INVOICE_NUMBER_OFFSET + INVOICE_NUMBER_LENGTH;
	private static final int INVOICE_FIRST_NAME_LENGTH = 12;
	private static final int INVOICE_LAST_NAME_OFFSET = 
			INVOICE_FIRST_NAME_OFFSET + INVOICE_FIRST_NAME_LENGTH;
	private static final int INVOICE_LAST_NAME_LENGTH = 12;
	private static final int INVOICE_AMOUNT_OFFSET = 
			INVOICE_LAST_NAME_OFFSET + INVOICE_LAST_NAME_LENGTH;
	private static final int INVOICE_AMOUNT_LENGTH = 8;
	private static final int INVOICE_DATE_OFFSET = 
			INVOICE_AMOUNT_OFFSET + INVOICE_AMOUNT_LENGTH;
	private static final int INVOICE_DATE_LENGTH = 6;
	private static final int INVOICE_PAID_DATE_OFFSET = 
			INVOICE_DATE_OFFSET + INVOICE_DATE_LENGTH;
	private static final int INVOICE_PAID_DATE_LENGTH = 6;
	private static final int INVOICE_LENGTH = 
			INVOICE_PAID_DATE_OFFSET + INVOICE_PAID_DATE_LENGTH;

	/**
	 * Helper that can parse one line of text in order to
	 * produce a {@link Customer} object.
	 */
	private Customer parseCustomer(String line) {
		if (line.length() >= CUSTOMER_LENGTH) {
			try {
				String firstName = line.substring(CUSTOMER_FIRST_NAME_OFFSET, 
						CUSTOMER_LAST_NAME_OFFSET).trim();
				String lastName = line.substring(CUSTOMER_LAST_NAME_OFFSET, 
						CUSTOMER_TERMS_OFFSET).trim();
				Terms terms = Terms.valueOf(line.substring
						(CUSTOMER_TERMS_OFFSET, CUSTOMER_LENGTH).trim());
				return new Customer(firstName, lastName, terms);
			} catch (Exception ex) {
				log.warning(() -> 
						"Couldn't parse terms value, skipping customer: " + line);
			}
		} else {
			log.warning(() -> "Incorrect length, skipping customer: " + line);
		}
		
		return null;
	}

	/**
	 * Helper that can parse one line of text in order to
	 * produce an {@link Invoice} object.
	 */
	@SneakyThrows
	private Invoice parseInvoice(String line, Map<String, Customer> customers) {

		if (line.length() >= INVOICE_PAID_DATE_OFFSET) {
			try {
				int number = Integer.parseInt(line.substring
					(INVOICE_NUMBER_OFFSET,
						INVOICE_FIRST_NAME_OFFSET).trim());
				
				String firstName = line.substring
					(INVOICE_FIRST_NAME_OFFSET,
						INVOICE_LAST_NAME_OFFSET).trim();
				String lastName = line.substring
					(INVOICE_LAST_NAME_OFFSET, 
						INVOICE_AMOUNT_OFFSET).trim();
				
				double amount = Double.parseDouble(line.substring
					(INVOICE_AMOUNT_OFFSET, INVOICE_DATE_OFFSET).trim());
		
				DateTimeFormatter parser =
						DateTimeFormatter.ofPattern("MMddyy");
				LocalDate theDate = LocalDate.parse(line.substring
					(INVOICE_DATE_OFFSET, INVOICE_PAID_DATE_OFFSET), parser);
				String paidString = line.substring(
						INVOICE_PAID_DATE_OFFSET, INVOICE_LENGTH).trim();
				LocalDate paidDate = paidString.length() == 
					INVOICE_PAID_DATE_LENGTH
						? LocalDate.parse(paidString, parser) : null;
		
				Customer customer = customers.get(firstName + " " +  lastName);
				if (customer != null) {
					return new Invoice(number, customer, amount, 
							theDate, paidDate);
				} else {
					log.warning(() -> 
						"Unknown customer, skipping invoice: " + line);
				}
			} catch (Exception ex) {
				log.warning(() -> 
						"Couldn't parse values, skipping invoice: " + line);
			}
		} else {
			log.warning(() -> "Incorrect length, skipping invoice: " + line);
		}
		
		return null;
	}

	/**
	 * Consumes the given string streams and translates to {@link Customer}
	 * objects.
	 */
	public Stream<Customer> parseCustomers(Reader customerReader) {

		return new BufferedReader(customerReader).lines()
				.map(this::parseCustomer)
				.filter(customer -> customer != null);

	}

	/**
	 * Consumes the given string streams and translates to {@link Invoices}
	 * objects.
	 */
	public Stream<Invoice> parseInvoices(Reader invoiceReader, Map<String, Customer> customers) {

		return new BufferedReader(invoiceReader).lines()
				.map(line -> parseInvoice(line, customers))
				.filter(invoice -> invoice != null);
	}
}
