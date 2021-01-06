package com.amica.billing;

import static com.amica.billing.ParserFactory.createParser;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.amica.billing.parse.CSVParser;
import com.amica.billing.parse.FlatParser;
import com.amica.billing.parse.Parser;

public class ParserFactoryTest {

	@Before
	public void setUp() {
		ParserFactory.parsers.put(Parser.Format.CSV, CSVParser::new);
	}
	
	@Test
	public void testCreateParser_CSVFilename() {
		assertThat(createParser("any.csv"), instanceOf(CSVParser.class));
	}
	
	@Test
	public void testCreateParser_CSVFormat() {
		assertThat(createParser(Parser.Format.CSV), instanceOf(CSVParser.class));
	}
	
	@Test
	public void testCreateParser_FlatFilename () {
		assertThat(createParser("any.flat"), instanceOf(FlatParser.class));
	}
	
	@Test
	public void testCreateParser_FlatFormat () {
		assertThat(createParser(Parser.Format.FLAT), instanceOf(FlatParser.class));
	}
	
	@Test
	public void testCreateParser_UnknownExtension() {
		assertThat(createParser("x.y.z"), instanceOf(CSVParser.class));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCreateParser_NullFormat() {
		Parser.Format format = null;
		createParser(format);
	}
	
	@Test
	public void testCreateParser_DefaultFormat () {
		assertThat(createParser(Parser.Format.DEFAULT), instanceOf(CSVParser.class));
	}
	
	@Test
	public void testCreateParser_Overridden() {
		ParserFactory.parsers.put(Parser.Format.CSV, FlatParser::new);
		assertThat(createParser("any.csv"), instanceOf(FlatParser.class));
	}
}
