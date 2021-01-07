package com.amica.billing.parse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import com.amica.billing.Customer;
import com.amica.billing.Invoice;
import com.amica.billing.Terms;

import lombok.extern.java.Log;

/**
 * A parser that can read a CSV format with certain expected columns.
 * 
 * @author Will Provost
 */
@Log
public class ExcelParser implements Parser {

	private static final int CUSTOMER_COLUMNS = 3;
	private static final int CUSTOMER_FIRST_NAME_COLUMN = 0;
	private static final int CUSTOMER_LAST_NAME_COLUMN = 1;
	private static final int CUSTOMER_TERMS_COLUMN = 2;

	private static final int INVOICE_MIN_COLUMNS = 5;
	private static final int INVOICE_NUMBER_COLUMN = 0;
	private static final int INVOICE_FIRST_NAME_COLUMN = 1;
	private static final int INVOICE_LAST_NAME_COLUMN = 2;
	private static final int INVOICE_AMOUNT_COLUMN = 3;
	private static final int INVOICE_DATE_COLUMN = 4;
	private static final int INVOICE_PAID_DATE_COLUMN = 5;

	/**
	 * Helper that can parse one line of comma-separated text in order to
	 * produce a {@link Customer} object.
	 */
	private Customer parseCustomer(CSVRecord record) {
		Terms terms = record.get(2).equals(Terms.CASH.toString()) 
				? Terms.CASH
				: Terms.valueOf(record.get(2));
		return new Customer(record.get(0), record.get(1), terms);
	}

	/**
	 * Helper that can parse one line of comma-separated text in order to
	 * produce an {@link Invoice} object.
	 */
	private Invoice parseInvoice(String line, Map<String, Customer> customers) {
		DateTimeFormatter parser = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String[] fields = line.split(",");
		if (fields.length >= INVOICE_MIN_COLUMNS) {
			try {
				int number = Integer.parseInt(fields[INVOICE_NUMBER_COLUMN]);
				String first = fields[INVOICE_FIRST_NAME_COLUMN];
				String last = fields[INVOICE_LAST_NAME_COLUMN];
				double amount = Double.parseDouble
						(fields[INVOICE_AMOUNT_COLUMN]);
				
				LocalDate date = LocalDate.parse(fields[INVOICE_DATE_COLUMN], parser);
				LocalDate paidDate = fields.length > INVOICE_PAID_DATE_COLUMN 
						? LocalDate.parse(fields[INVOICE_PAID_DATE_COLUMN], parser) 
						: null;

				Customer customer = customers.get(first + " " + last);
				if (customer != null) {
					return new Invoice(number, customer, amount, date, paidDate);
				} else {
					log.warning(() -> 
						"Unknown customer, skipping invoice: " + line);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				log.warning(() -> 
					"Couldn't parse values, skipping invoice: " + line);
			}
		} else {
			log.warning(() -> 
				"Incorrect number of fields, skipping invoice: " + line);
		}

		return null;
	}

	/**
	 * Consumes the given string streams and translates to {@link Customer}
	 * objects.
	 * @throws IOException 
	 */
	public Stream<Customer> parseCustomers(Reader customerReader) {
		try {
			ArrayList<Customer> customers = new ArrayList<>();
			org.apache.commons.csv.CSVParser csvParser = CSVFormat.DEFAULT.parse(customerReader);
			for (CSVRecord record : csvParser) {
				customers.add(parseCustomer(record));
			}
			return customers.stream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Consumes the given string streams and translates to {@link Invoices}
	 * objects.
	 * 
	 * @param customers
	 *            We use this to translate the customer name to a reference to
	 *            the already-loaded {@link Customer} object.
	 */
	public Stream<Invoice> parseInvoices(Reader invoiceReader, 
			Map<String, Customer> customers) {

		return new BufferedReader(invoiceReader).lines()
				.map(line -> parseInvoice(line, customers))
				.filter(invoice -> invoice != null);
	}
}
