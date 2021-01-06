package com.amica.billing;

import static com.amica.billing.parse.ParserTestUtility.GOOD_CUSTOMERS;
import static com.amica.billing.parse.ParserTestUtility.GOOD_CUSTOMERS_MAP;
import static com.amica.billing.parse.ParserTestUtility.GOOD_INVOICES;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.amica.billing.parse.Parser;
import com.amica.format.Formatter;
import com.amica.format.Outline2;
import com.amica.format.Pair;

public class ReporterTest {

	public static final String CUSTOMER_INPUT = "one\ntwo\nthree";
	public static final String INVOICE_INPUT = "four\nfive";
	
	private StringReader customerReader;
	private StringReader invoiceReader;
	private Formatter mockFormatter;
	
	@BeforeClass
	public static void setUpClass() {
		ParserFactory.parsers.put(Parser.Format.DEFAULT, MockParser::new);
	}
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		customerReader = new StringReader(CUSTOMER_INPUT);
		invoiceReader = new StringReader(INVOICE_INPUT);
		
		MockParser.customers = Stream.of(GOOD_CUSTOMERS);
		MockParser.invoices = Stream.of(GOOD_INVOICES);
		
		mockFormatter = mock(Formatter.class);
		doAnswer(inv -> {
			PrintWriter output = (PrintWriter) inv.getArguments()[2];
			output.print("REPORT");
			return null;
		}).when(mockFormatter).produceReport
				(anyString(), any(Outline2.class), any(PrintWriter.class));
	}
	
	@Test
	public void testInitialization() {
		new Reporter(customerReader, invoiceReader, null);
		assertThat(MockParser.customerInput, equalTo(CUSTOMER_INPUT));
		assertThat(MockParser.invoiceInput, equalTo(INVOICE_INPUT));
		assertThat(MockParser.customerMapInput, equalTo(GOOD_CUSTOMERS_MAP));
	}
	
	private void assertCorrectFormat
			(Function<Invoice,Stream<Pair>> childLevel, boolean customerIncluded) {
		Invoice parent = new Invoice(99, GOOD_CUSTOMERS[0], 999, 
				LocalDate.of(2021, 1, 1), null);
		Iterator<Pair> children = childLevel.apply(parent).iterator();
		
		Pair detail = null;
		
		if (customerIncluded) {
			detail = children.next();
			assertThat(detail.getName(), equalTo("Customer"));
			assertThat(detail.getValue(), equalTo(GOOD_CUSTOMERS[0].getName()));
		}
		
		detail = children.next();
		assertThat(detail.getName(), equalTo("Amount"));
		assertThat(detail.getValue(), equalTo("$999.00"));
		
		detail = children.next();
		assertThat(detail.getName(), equalTo("Date"));
		assertThat(detail.getValue(), equalTo("1/1/2021"));
		
		detail = children.next();
		assertThat(detail.getName(), equalTo("Paid"));
		assertThat(detail.getValue(), equalTo("Outstanding"));
	}
	
	@Test
	@SuppressWarnings({"rawtypes","unchecked"}) // Mockito ArgumentCaptor on generic type
	public void testReportInvoicesForCustomer() {
		Reporter reporter = new Reporter
				(customerReader, invoiceReader, mockFormatter);
		StringWriter output = new StringWriter();
		ArgumentCaptor<Outline2> captor = ArgumentCaptor.forClass(Outline2.class);
		
		reporter.reportInvoicesForCustomer("Customer Two", output);
		
		assertThat(output.toString(), equalTo("REPORT"));
		
		verify(mockFormatter).produceReport(eq("Invoices for Customer Two"), 
				captor.capture(), any(PrintWriter.class));
		Outline2 dataProvided = captor.getValue();
		Invoice[] parents = ((Stream<Invoice>) dataProvided.getParentLevel())
				.toArray(Invoice[]::new);
		Invoice[] customerTwoInvoices = Arrays.copyOfRange(GOOD_INVOICES, 1, 4);
		assertThat(parents, arrayContaining(customerTwoInvoices));
		
		assertCorrectFormat(dataProvided::getChildLevel, false);
	}
	
	@Test
	@SuppressWarnings({"rawtypes","unchecked"}) // Mockito ArgumentCaptor on generic type
	public void testReportOverdueInvoices() {
		Reporter reporter = new Reporter
				(customerReader, invoiceReader, mockFormatter);
		StringWriter output = new StringWriter();
		ArgumentCaptor<Outline2> captor = ArgumentCaptor.forClass(Outline2.class);
		
		reporter.reportOverdueInvoices(output, LocalDate.of(2021, 1, 8));
		
		assertThat(output.toString(), equalTo("REPORT"));
		
		verify(mockFormatter).produceReport(eq("Overdue invoices"), 
				captor.capture(), any(PrintWriter.class));
		Outline2 dataProvided = captor.getValue();
		Iterator<Invoice> invoices = 
				((Stream<Invoice>) dataProvided.getParentLevel()).iterator();
		assertThat(invoices.next(), equalTo(GOOD_INVOICES[3]));
		assertThat(invoices.next(), equalTo(GOOD_INVOICES[5]));
		assertThat(invoices.next(), equalTo(GOOD_INVOICES[0]));
		assertThat(invoices.hasNext(), equalTo(false));
		
		assertCorrectFormat(dataProvided::getChildLevel, true);
	}
}
