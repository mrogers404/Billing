package com.amica.format;

import java.util.stream.Stream;

/**
 * Represents data of arbitrary types as a three-level outline,
 * with top-level nodes of "type T1" and second-level nodes of "type T2."
 * (Okay, who isn't thinking of the Terminator movies right now?)
 * 
 * @author Will Provost
 */
public interface Outline3<T1,T2> {
	
	/**
	 * Returns a stream of the top-level nodes. 
	 */
	public Stream<T1> getLevel1();
	
	/**
	 * Returns a stream of the second-level nodes under a given 
	 * top-level value.
	 */
	public Stream<T2> getLevel2(T1 parent);

	/**
	 * Returns a stream of the bottom-level nodes under a given 
	 * second-level value.
	 */
	public Stream<Pair> getLevel3(T2 parent);
}
