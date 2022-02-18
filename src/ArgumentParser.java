import java.util.HashMap;

/**
 * Parses command-line arguments into flag/value pairs, and stores those pairs
 * in a map for easy access.
 * 
 * @author William_Chuang
 * @version 2.01, 19 March 2018
 */
public class ArgumentParser {

	/**
	 * Default constructor, initialize the argument map.
	 */
	public ArgumentParser() {
		argumentMap = new HashMap<String, String>();
	}

	/**
	 * Stores parsed flag, value pairs.
	 */
	private final HashMap<String, String> argumentMap;

	/**
	 * Create a new argument parser and immediately parses the passed arguments.
	 * 
	 * @param args
	 *            - arguments to parse into flag, value pairs
	 * @see {@code parseArguments(String[] arguments)}
	 */
	public ArgumentParser(String[] arguments) {
		this();
		if (arguments != null) {
			parseArguments(arguments);
		}
	}

	/**
	 * Check if the flag exists.
	 * 
	 * @param flag
	 *            - flag to check for existence
	 * @return {@code true} if flag exists
	 */
	public boolean hasFlag(String flag) {
		return argumentMap.containsKey(flag);
	}

	/**
	 * Return the value for a flag as an integer. If the flag or value is missing or
	 * if the value cannot be converted to an integer, returns the default value
	 * instead.
	 * 
	 * @param flag
	 * @return {@code rgumentMap.get(flag)}
	 */
	public String getValue(String flag) {
		return argumentMap.get(flag);
	}

	/**
	 * Parse the array of arguments into flag, value pairs. If an argument is a
	 * flag, test whether the next argument is a value. If it is a value, then
	 * stores the flag, value pair. Otherwise, it stores the flag with no value. If
	 * the flag appears multiple times with different values, only the last value
	 * will be kept. Values without an associated flag are ignored.
	 * 
	 * @param arguments
	 */
	public void parseArguments(String[] arguments) {

		String key = "", value = "";
		boolean haskey = false, hasvalue = false;

		if (arguments.length == 0) {
			System.out.println("Proper Usage is: java program filename");
		}

		for (String argu : arguments) {
			if (!argu.startsWith("-")) {
				hasvalue = true;
				value = argu;
			} else {
				haskey = true;
				key = argu;
			}

			if ((haskey == true) && (hasvalue == false)) {
				argumentMap.put(key, null);
			} else if ((haskey == hasvalue) && (hasvalue == true)) {
				argumentMap.put(key, value);
				haskey = false;
				hasvalue = haskey;
			}

		}
	}

	@Override
	public String toString() {
		return argumentMap.toString();
	}

	/**
	 * Return the value for a flag as an integer. If the flag or value is missing or
	 * if the value cannot be converted to an integer, return the default value
	 * instead.
	 * 
	 * @param flag
	 *            - flag whose associated value is to be returned
	 * @param defaultValue
	 *            - the default mapping of the flag
	 * @return value of flag as an integer or {@code defaultValue} if the value
	 *         cannot be returned as an integer
	 */
	public int getValue(String flag, int defaultValue) {
		if (flag != "" && flag != null) {
			if (argumentMap.get(flag) != null) {
				String number = argumentMap.get(flag);
				int result = 0;
				try {
					result = Integer.parseInt(number);
				} catch (NumberFormatException e) {
					return defaultValue;
				}
				return result;
			} else {
				return defaultValue;
			}
		} else {
			return defaultValue;
		}
	}

	/**
	 * Return the value for a flag. If the flag is missing or the value is
	 * {@code null}, return the default value instead.
	 * 
	 * @param flag
	 *            - flag whose associated value is to be returned
	 * @param defaultValue
	 *            - the default mapping of the flag
	 * @return value of flag or {@code defaultValue} if the flag is missing or the
	 *         value is {@code null}
	 */
	public String getValue(String flag, String defaultValue) {
		if (flag != "" && flag != null) {
			if (argumentMap.get(flag) != null) {
				return argumentMap.get(flag);
			} else {
				return defaultValue;
			}
		} else {
			return defaultValue;
		}
	}

	/**
	 * Test whether the argument is a valid flag, i.e. it is not null, and after
	 * trimming it starts with a dash "-" character followed by at least 1
	 * character.
	 * 
	 * @param arg
	 *            - argument to test
	 * @return {@code true} if the argument is a valid flag
	 */
	public static boolean isFlag(String arg) {
		arg = arg.trim();
		if (arg.startsWith("-") && (arg.length() > 1) && (!arg.endsWith(" ")) && (arg != null)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Test whether the argument is a valid value, i.e. it is not null, does not
	 * start with a dash "-" character, and is not empty after trimming.
	 * 
	 * @param arg
	 *            - argument to test
	 * @return {@code true} if the argument is a valid value
	 */
	public static boolean isValue(String arg) {

		arg = arg.trim();

		if ((!arg.startsWith("-")) && (arg.length() >= 1) && (!arg.endsWith("-")) && (arg != null)) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * Return the number of flags stored.
	 * 
	 * @return number of flags
	 */
	public int numFlags() {
		return argumentMap.size();
	}

	/**
	 * Check if the flag exists and has a non-null value.
	 * 
	 * @param flag
	 *            - flag whose associated value is to be checked
	 * @return {@code true} if the flag exists and has a non-null value
	 */
	public boolean hasValue(String flag) {
		return (argumentMap.keySet().contains(flag) && argumentMap.get(flag) != null);
	}

	/**
	 * Returns the value for the specified flag as a String object.
	 *
	 * @param flag
	 *            flag to get value for
	 *
	 * @return value as a String or null if flag or value was not found
	 */
	public String getString(String flag) {

		return argumentMap.get(flag);

	}

	/**
	 * Returns the value for the specified flag as an int value.
	 *
	 * @param flag
	 *            flag to get value for
	 * @param defaultValue
	 *            value to return if the flag or value is missing
	 * @return (1) value of flag as an int, or (2) the default value if the flag or
	 *         value is missing
	 */
	public int getInteger(String flag, int defaultValue) {

		try {

			return Integer.parseInt(argumentMap.get(flag));

		} catch (Exception e) {
			return defaultValue;
		}
	}

}