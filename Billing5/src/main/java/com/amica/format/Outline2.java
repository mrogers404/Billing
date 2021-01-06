package com.amica.format;

import java.util.stream.Stream;

/**
 * Represents data of arbitrary types as a two-level outline,
 * with parents of "type T."
 * 
 * @author Will Provost
 */
public interface Outline2<T> {
	
	/**
	 * Returns a stream of the parent nodes. 
	 */
	public Stream<T> getParentLevel();
	
	/**
	 * Returns a stream of the child nodes under a given parent.
	 */
	public Stream<Pair> getChildLevel(T parent);
}
