package com.amica.billing;

import static java.util.function.Function.identity;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.amica.billing.parse.Parser;
import com.amica.format.Formatter;
import com.amica.format.Outline2;
import com.amica.format.Outline3;
import com.amica.format.Pair;

import lombok.SneakyThrows;
import lombok.extern.java.Log;

/**
 * This is the central component of the system.
 * It reads a file of {@link Customer}s and a file of {@link Invoice}s,
 * using configurable {@link Parser}s so as to to handle different file 
 * formats; and then can produce reports based on a few different queries
 * and relying on a generic {@link TextReporter report generator}. 
 * 
 * @author Will Provost
 */
@Log
public class Reporter {

	private Map<String,Customer> customers;
	private List<Invoice> invoices;
	private Formatter formatter;
	
	/**
	 * Provide the locations of a file of customer data and a file of 
	 * invoice data. The invoice data is expected to include customer names,
	 * and in loading the data we re-connect the invoices so that they refer
	 * directly to the customer objects in memory.
	 */
	public Reporter(Reader customerReader, Reader invoiceReader, Formatter formatter) {
		this(customerReader, invoiceReader, formatter, Parser.Format.DEFAULT);
	}
	
	public Reporter(Reader customerReader, Reader invoiceReader, 
			Formatter formatter, Parser.Format format) {

		this.formatter = formatter;
		
		Parser parser = ParserFactory.createParser(format);
		try {
			
			customers = parser.parseCustomers(customerReader)
					.collect(Collectors.toMap(Customer::getName, identity()));
			
			invoices = parser.parseInvoices(invoiceReader, customers)
					.collect(Collectors.toList());
					
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Couldn't load from given filenames.", ex);
		}
		
	}
	
	/**
	 * Helper to produce a report based on a given {@link Outline2 outline}.
	 */
	@SneakyThrows
	private <T> void report(String title, Outline2<T> outline, Writer writer) {
		formatter.produceReport(title,  outline, new PrintWriter(writer));
	}

	/**
	 * Helper to produce a report based on a given {@link Outline3 outline}.
	 */
	@SneakyThrows
	private <T1,T2> void report(String title, Outline3<T1,T2> outline, Writer writer) {
		formatter.produceReport(title,  outline, new PrintWriter(writer));
	}
	
	/**
	 * Helper to build a stream of name-value pairs representing the details
	 * of the given invoice. 
	 */
	public static Stream<Pair> getValuesForInvoice(Invoice invoice) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
		LocalDate paidDate = invoice.getPaidDate();
		String paidString = paidDate != null 
				? paidDate.format(formatter) 
				: "Outstanding";
		Pair[] values = {
			new Pair("Amount", String.format("$%.2f", invoice.getAmount())),
			new Pair("Date", invoice.getTheDate().format(formatter)),
			new Pair("Paid", paidString)
		};
		
		return Stream.of(values);
	}

	/**
	 * Builds an {@link Outline2} representation of the invoices for the given
	 * customer, and generates the report. 
	 */
	public void reportInvoicesForCustomer(String customerName, Writer writer) {

		Customer customer = customers.get(customerName);

		class Outline implements Outline2<Invoice> {
		
			public Stream<Invoice> getParentLevel() {
				return invoices.stream()
						.filter(inv -> inv.getCustomer().equals(customer))
						.sorted((a,b) -> Integer.compare(a.getNumber(), b.getNumber()));
			}
		
			public Stream<Pair> getChildLevel(Invoice invoice) {
				return getValuesForInvoice(invoice);
			}
		}
		
		report("Invoices for " + customerName, new Outline(), writer);
	}

	/**
	 * Builds an {@link Outline3} representation of invoices grouped by
	 * customer, and generates the report. 
	 */
	/*START String filename */
	public void reportInvoicesByCustomer(Writer writer) {
		
		class Outline implements Outline3<Customer,Invoice> {
		
			public Stream<Customer> getLevel1() {
				return customers.values().stream()
						.sorted((a,b) -> a.getFirstName().compareTo(b.getFirstName()))
						.sorted((a,b) -> a.getLastName().compareTo(b.getLastName()));
			}
		
			public Stream<Invoice> getLevel2(Customer customer) {
				return invoices.stream()
						.filter(inv -> inv.getCustomer().equals(customer))
						.sorted((a,b) -> Integer.compare(a.getNumber(), b.getNumber()));
			}
		
			public Stream<Pair> getLevel3(Invoice invoice) {
				return getValuesForInvoice(invoice);
			}
		}
		
		report("All invoices, by customer", new Outline(), writer);
	}

	/**
	 * Builds an {@link Outline2} representation of overdue invoices, 
	 * and generates the report. 
	 */
	/*START String filename */
	public void reportOverdueInvoices(Writer writer, LocalDate asOf) {
		
		class Outline implements Outline2<Invoice> {
		
			public Stream<Invoice> getParentLevel() {
				return invoices.stream()
						.filter(invoice -> invoice.isOverdue(asOf))
						.sorted((a,b) -> a.getTheDate().compareTo(b.getTheDate()));
			}
		
			public Stream<Pair> getChildLevel(Invoice invoice) {
				return Stream.concat(Stream.of(new Pair("Customer", 
						invoice.getCustomer().getName())), 
					getValuesForInvoice(invoice));
			}
		}
		
		report("Overdue invoices", new Outline(), writer);
	}
	
	/**
	 * Builds an {@link Outline2} representation of customers,
	 * sorted in descending order of the sum of their invoices. 
	 */
	/*START String filename */
	public void reportCustomersByVolume(Writer writer) {
		
		/**
		 * This class functions as a comparator of customers,
		 * based on their total volume of business. It also caches
		 * the computed volume, so as not to re-calculate that value
		 * every time it is asked to compare two customers. 
		 */
		class ByVolume implements Comparator<Customer> {

			private Map<String,Double> cache = new HashMap<>();
			
			private double getVolume(Customer customer) {
				String name = customer.getName();
				if (!cache.containsKey(name)) {
					cache.put(name, 
						invoices.stream()
							.filter(inv -> inv.getCustomer().getName().equals(name))
							.mapToDouble(Invoice::getAmount)
							.sum());
				}
				return cache.get(name);
			}
			
			public int compare(Customer c1, Customer c2) {
				return -Double.compare(getVolume(c1), getVolume(c2));
			}
		}
		
		class Outline implements Outline2<Customer> {

			private ByVolume byVolume = new ByVolume();
			
			public Stream<Customer> getParentLevel() {
				return customers.values().stream().sorted(byVolume);
			}

			public Stream<Pair> getChildLevel(Customer customer) {
				Pair[] details = {
					new Pair("Terms", customer.getTerms()),
					new Pair("Volume", byVolume.getVolume(customer))
				};
				return Stream.of(details);
			}
		}
		
		report("Customers in descending order of total business", 
				new Outline(), writer);
	}
}
