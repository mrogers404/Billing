package com.amica.format;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Captures a name-value pair for presentation.
 * 
 * @author Will Provost
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pair {
	private String name;
	private Object value;
}
