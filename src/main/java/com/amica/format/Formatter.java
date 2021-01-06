package com.amica.format;

import java.io.Writer;

public interface Formatter {

	/**
	 * Generates a report to the given writer, based on the provided
	 * two-level data, in plain style (no bullets or numbers). 
	 */
	<T> void produceReport(String title, Outline2<T> outline, Writer report);

	/**
	 * Generates a report to the given writer, based on the provided
	 * two-level data, in plain style (no bullets or numbers). 
	 */
	<T1, T2> void produceReport(String title, Outline3<T1, T2> outline, Writer report);
}
