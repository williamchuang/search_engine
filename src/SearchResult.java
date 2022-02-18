/**
 * Goal: Result Sorting.
 * 
 * In order to obtain the search results are sorted, and the most relevant
 * search result is listed first, and the least relevant search result is listed
 * last.
 * 
 * This class that stores a single search result and implements the Comparable
 * interface.
 * 
 * @author William_Chuang
 * @version 2.01, 19 March 2018
 */
public class SearchResult implements Comparable<SearchResult> {

	/**
	 * String representation of the file path
	 */
	private final String location; // the path to the file

	/**
	 * Number of times this object was found, and it's for determining relevance of
	 * query word and the search results
	 */
	private int frequency; // the number of times found in file

	/**
	 * It represents the first position was founded
	 */
	private int position; // the position in file

	/**
	 * Constructor for managing the new query
	 * 
	 * @param file
	 *            - the file location, and it was stored as a String
	 * @param frequency
	 *            - the number of times a word occurs within a given file
	 * @param position
	 *            - the initial index, or first place the word is found at
	 */
	public SearchResult(int frequency, String location, int position) {
		this.position = position;
		this.frequency = frequency;
		this.location = location;

	}

	/**
	 * Implemented Comparable--override compareTo method. It firstly sorts the given
	 * query by their frequency, then via their initial index, and then their file
	 * name
	 */
	@Override
	public int compareTo(SearchResult o) {
		if (this.frequency != o.frequency) {
			return Integer.compare(o.frequency, this.frequency);
		}
		else {
			if (this.position == o.position) {
				return this.location.compareToIgnoreCase(o.location);
			}
			else {
				return Integer.compare(this.position, o.position);
			}
		}
	}

	/**
	 * Updates a SearchResult object's position from the position of another
	 * SearchResult object, so we obtain one combined SearchResult object
	 * 
	 * @param initalIndex
	 *            - a SearchResult object's position from the position of another
	 *            SearchResult object
	 */
	public void updateIndex(int initalIndex) {

		if (initalIndex < this.position) {
			this.position = initalIndex;
		}
	}

	/**
	 * Update the frequency as a new word was found
	 *
	 * @param frequency
	 *            - the new frequency found in the file
	 */
	public void updateFrequency(int frequency) {
		this.frequency += frequency;
	}

	/**
	 * Get the location (the file name) of the current search result
	 * 
	 * @return the location of the SearchResult
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Get the frequency
	 * 
	 * @return the frequency of the search result
	 */
	public int getFrequency() {
		return frequency;
	}

	/**
	 * Get the initial index
	 * 
	 * @return the initial index of the search result
	 */

	public int getPosition() {
		return position;
	}

	/**
	 * Adds quotes to any string, useful in pretty printing JSON
	 * 
	 * @param text
	 *            - the string to be quoted
	 * @return the string with quotations on it
	 */
	public String quote(String text) {
		return String.format("\"%s\"", text);
	}

}