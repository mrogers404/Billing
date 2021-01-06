package com.amica.format;

import java.io.Writer;

import lombok.SneakyThrows;

/**
 * Produces a plain-text report on provided data, with indented nodes
 * at two or three levels, and name-value pairs at the "leaves" of the outline.
 * It relies on provision of data through one of two standard interfaces,
 * for {@link Outline2 two-level} or a {@link Outline3 three-level} outline.  
 * 
 * @author Will Provost
 */
public class TextFormatter implements Formatter {

	public enum Style { PLAIN, BULLETED, NUMBERED }
	
	private static final String EOL = System.getProperty("line.separator");
	
	private Style style;

	private Writer report;
	private String bullet = "";
	private int number1;
	private int number2;
	private int number3;
	
	/**
	 * Use the default style (PLAIN).
	 */
	public TextFormatter() {
		this(Style.PLAIN);
	}
	
	/**
	 * Specify the desired report style.
	 */
	public TextFormatter(Style style) {
		this.style = style;
	}
	/**
	 * Helper method to write a line at the root level of the outline.
	 */
	@SneakyThrows
	private void addLevel1Line(Object datum) {
		report.append(EOL).append(bullet);
		if (style == Style.NUMBERED) {
			report.append(Integer.toString(++number1)).append(". ");
			number2 = number3 = 0;
		}
		report.append(datum.toString()).append(EOL);
	}
	
	/**
	 * Helper method to write a line at the second level of the outline.
	 */
	@SneakyThrows
	private void addLevel2Line(Object datum) {
		report.append("  ").append(bullet);
		if (style == Style.NUMBERED) {
			report.append(Integer.toString(++number2)).append(". ");
			number3 = 0;
		}
		report.append(datum.toString()).append(EOL);
	}
	
	/**
	 * Helper method to write a line at the bottom level of the outline.
	 */
	@SneakyThrows
	private void addBottomLevelLine(Pair nvPair) {
		report.append("    ").append(bullet);
		if (style == Style.NUMBERED) {
			report.append(Integer.toString(++number3)).append(". ");
		}
		report.append(nvPair.getName()).append(": ")
				.append(nvPair.getValue().toString()).append(EOL);
	}
	
	/**
	 * Generates a report to the given writer, based on the provided
	 * two-level data, in plain style (no bullets or numbers). 
	 */
	@Override
	@SneakyThrows
	public synchronized <T> void produceReport
			(String title, Outline2<T> outline, Writer report) {
		this.report = report;
		
		bullet = style == Style.BULLETED ? "* " : "";
		number1 = number2 = number3 = 0;
		
		report.append(title).append(EOL);
		outline.getParentLevel()
			.peek(this::addLevel1Line)
			.flatMap(outline::getChildLevel)
			.forEach(this::addBottomLevelLine);
		report.flush();
	}
	
	/**
	 * Generates a report to the given writer, based on the provided
	 * two-level data, in plain style (no bullets or numbers). 
	 */
	@Override
	@SneakyThrows
	public synchronized <T1,T2> void produceReport
			(String title, Outline3<T1,T2> outline, Writer report) {
		
		this.report = report;
		number1 = number2 = number3 = 0;
		
		report.append(title).append(EOL);
		bullet = style == Style.BULLETED ? "* " : "";
		outline.getLevel1()
			.peek(this::addLevel1Line)
			.flatMap(outline::getLevel2)
			.peek(this::addLevel2Line)
			.flatMap(outline::getLevel3)
			.forEach(this::addBottomLevelLine);
		report.flush();
	}
}
