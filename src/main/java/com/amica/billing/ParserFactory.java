package com.amica.billing;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.amica.billing.parse.CSVParser;
import com.amica.billing.parse.FlatParser;
import com.amica.billing.parse.Parser;

import lombok.extern.java.Log;

/**
 * A factory for parsers that determines which type of parser to create
 * based on the extension of given filenames.
 * 
 * @author Will Provost
 */
@Log
public class ParserFactory {

	public static Map<Parser.Format,Supplier<Parser>> parsers = new HashMap<>();
	
	static {
		parsers.put(Parser.Format.CSV, CSVParser::new);
		parsers.put(Parser.Format.FLAT, FlatParser::new);
		parsers.put(Parser.Format.DEFAULT, CSVParser::new);
//		parsers.compute(Parser.Format.EXPORT, ExportParser::new);
	}

	/**
	 * Looks up the file extension to find a 
	 * <code>Supplier&lt;Parser&gt;</code>, invokes it, and returns the result. 
	 */
	public static Parser createParser(String filename) {
		int separatorIndex = filename.indexOf(".");
		String type;
		if (separatorIndex != -1) {
			String extension = "needs fixed"; //TODO throw real error
			type = filename.substring(separatorIndex + 1);
			if(filename.toLowerCase().contains("_export"))// contains _export
			{
				type = "EXPORT";
			}
			for (Parser.Format format : Parser.Format.values()) {
				if (format.toString().equalsIgnoreCase(type)) {
					return createParser(format);
				}
			}
			log.fine(() -> "Unknown format " + extension + "; using default parser.");
		} else {
			log.fine(() -> "No file extension; using default parser.");
		}
		
		return createParser(Parser.Format.DEFAULT);
	}
	
	/**
	 * Looks up the file extension to find a 
	 * <code>Supplier&lt;Parser&gt;</code>, invokes it, and returns the result. 
	 */
	public static Parser createParser(Parser.Format format) {

		Supplier<Parser> supplier = parsers.get(format);
		if (supplier != null) {
			return supplier.get();
		}
		
		throw new IllegalArgumentException("No parser configured for " + format);
	}
}
