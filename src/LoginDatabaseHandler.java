import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles all database-related actions. Uses singleton design pattern.
 *
 * 
 */

public class LoginDatabaseHandler {

	/** A {@link org.apache.log4j.Logger log4j} logger for debugging. */
	private static Logger log = LogManager.getLogger();

	/** Makes sure only one database handler is instantiated. */
	private static LoginDatabaseHandler singleton = new LoginDatabaseHandler();

	/** Used to determine if necessary tables are provided. */
	private static final String TABLES_SQL = "SHOW TABLES LIKE 'login_users';";

	/** Used to create necessary tables for this example. */
	private static final String CREATE_SQL = "CREATE TABLE login_users ("
			+ "userid INTEGER AUTO_INCREMENT PRIMARY KEY, " + "username VARCHAR(32) NOT NULL UNIQUE, "
			+ "password CHAR(64) NOT NULL, " + "usersalt CHAR(32) NOT NULL);";

	/** Used to insert a new user into the database. */
	private static final String REGISTER_SQL = "INSERT INTO login_users (username, password, usersalt) "
			+ "VALUES (?, ?, ?);";

	/** Used to determine if a username already exists. */
	private static final String USER_SQL = "SELECT username FROM login_users WHERE username = ?";

	/** Used to retrieve the salt associated with a specific user. */
	private static final String SALT_SQL = "SELECT usersalt FROM login_users WHERE username = ?";

	/** Used to authenticate a user. */
	private static final String AUTH_SQL = "SELECT username FROM login_users " + "WHERE username = ? AND password = ?";

	/** Used to remove a user from the database. */
	private static final String DELETE_SQL = "DELETE FROM login_users WHERE username = ?";

	/** Used to update the user's password */
	private static final String NEW_PASS_SQL = "UPDATE login_users SET password = ? WHERE username = ? AND password = ?";

	/** Used to create a unique table to save user search history **/
	private static final String CREATE_SEARCHED_SQL = "CREATE TABLE %s_search_hist (searchterm VARCHAR(2000), timestamp VARCHAR(50));";

	/** Used to create a unique table to save user visit history **/
	private static final String CREATE_VISIT_SQL = " CREATE TABLE %s_visit_hist (link VARCHAR(2000), timestamp VARCHAR(50));";

	//---------------------------------------------------------------------------------------------------------------------------

//	private static final String CREATE_FAVORITE_SQL = "CREATE TABLE %s_favorite_hist (link VARCHAR(3000));";
//	
//	private static final String ADD_FAVORITE_SQL = "INSERT INTO %s_favorite_hist (link) VALUES (? , ?);";
//	
//	private static final String GET_FAVORITE_SQL = "SELECT * FROM %s_favorite_hist;";
//	
//	private static final String TRUN_FAVORITE_SQL = "TRUNCATE %s_favorite_hist;";
	//---------------------------------------------------------------------------------------------------------------------------
	/** Used to add a new visit link to user visit history */
	private static final String ADD_VISIT_SQL = "INSERT INTO %s_visit_hist (link, timestamp) VALUES (? , ?);";

	/** Used to retrieve a users visit history from the database. */
	private static final String GET_VISIT_SQL = "SELECT * FROM %s_visit_hist;";

	/** Used to clear a users visit history */
	private static final String TRUN_VISIT_SQL = "TRUNCATE %s_visit_hist;";

	/** Used to retrieve a users search history from the database. */
	private static final String GET_SEARCHED_SQL = "SELECT * FROM %s_search_hist;";

	/** Used to delete the table containing a users search history */
	private static final String DELETE_HIST_SQL = "DROP TABLE %s_search_hist;";

	/** Used to truncate a users search history. */
	private static final String TRUN_SEARCHED_SQL = "TRUNCATE %s_search_hist;";

	/** Used to add a search term into a users search history. */
	private static final String ADD_SEARCHED_SQL = "INSERT INTO %s_search_hist (searchterm, timestamp) VALUES (? , ?);";

	/** Used to configure connection to database. */
	private DatabaseConnector db;

	/** Used to generate password hash salt for user. */
	private Random random;

	/**
	 * Initializes a database handler for the Login example. Private constructor
	 * forces all other classes to use singleton.
	 */
	private LoginDatabaseHandler() {
		Status status = Status.OK;
		random = new Random(System.currentTimeMillis());

		try {

			db = new DatabaseConnector("database.properties");
			status = db.testConnection() ? setupTables() : Status.CONNECTION_FAILED;
		} catch (FileNotFoundException e) {
			status = Status.MISSING_CONFIG;
		} catch (IOException e) {
			status = Status.MISSING_VALUES;
		}

		if (status != Status.OK) {
			log.fatal(status.message());
		}
	}

	/**
	 * Gets the single instance of the database handler.
	 *
	 * @return instance of the database handler
	 */
	public static LoginDatabaseHandler getInstance() {
		return singleton;
	}

	/**
	 * Checks to see if a String is null or empty.
	 * 
	 * @param text
	 *            - String to check
	 * @return true if non-null and non-empty
	 */
	public static boolean isBlank(String text) {
		return (text == null) || text.trim().isEmpty();
	}

	/**
	 * Returns the hex encoding of a byte array.
	 *
	 * @param bytes
	 *            - byte array to encode
	 * @param length
	 *            - desired length of encoding
	 * @return hex encoded byte array
	 */
	public static String encodeHex(byte[] bytes, int length) {
		BigInteger bigint = new BigInteger(1, bytes);
		String hex = String.format("%0" + length + "X", bigint);

		assert hex.length() == length;
		return hex;
	}

	/**
	 * Calculates the hash of a password and salt using SHA-256.
	 *
	 * @param password
	 *            - password to hash
	 * @param salt
	 *            - salt associated with user
	 * @return hashed password
	 */
	public static String getHash(String password, String salt) {
		String salted = salt + password;
		String hashed = salted;

		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(salted.getBytes());
			hashed = encodeHex(md.digest(), 64);
		} catch (Exception ex) {
			log.debug("Unable to properly hash password.", ex);
		}

		return hashed;
	}

	/**
	 * Checks if necessary table exists in database, and if not tries to create
	 * it.
	 *
	 * @return {@link Status.OK} if table exists or create is successful
	 */
	private Status setupTables() {
		Status status = Status.ERROR;

		try (Connection connection = db.getConnection(); Statement statement = connection.createStatement();) {
			if (!statement.executeQuery(TABLES_SQL).next()) {
				// Table missing, must create
				log.debug("Creating tables...");
				statement.executeUpdate(CREATE_SQL);

				// Check if create was successful
				if (!statement.executeQuery(TABLES_SQL).next()) {
					status = Status.CREATE_FAILED;
				} else {
					status = Status.OK;
				}
			} else {
				log.debug("Tables found.");
				status = Status.OK;
			}
		} catch (Exception ex) {
			status = Status.CREATE_FAILED;
			log.debug(status, ex);
		}

		return status;
	}

	/**
	 * Tests if a user already exists in the database. Requires an active
	 * database connection.
	 *
	 * @param connection
	 *            - active database connection
	 * @param user
	 *            - username to check
	 * @return Status.OK if user does not exist in database
	 * @throws SQLException
	 */
	private Status duplicateUser(Connection connection, String user) {

		assert connection != null;
		assert user != null;

		Status status = Status.ERROR;

		try (PreparedStatement statement = connection.prepareStatement(USER_SQL);) {
			statement.setString(1, user);

			ResultSet results = statement.executeQuery();
			status = results.next() ? Status.DUPLICATE_USER : Status.OK;
		} catch (SQLException e) {
			log.debug(e.getMessage(), e);
			status = Status.SQL_EXCEPTION;
		}

		return status;
	}

	/**
	 * Tests if a user already exists in the database.
	 *
	 * @see #duplicateUser(Connection, String)
	 * @param user
	 *            - username to check
	 * @return Status.OK if user does not exist in database
	 */
	public Status duplicateUser(String user) {
		Status status = Status.ERROR;

		try (Connection connection = db.getConnection();) {
			status = duplicateUser(connection, user);
		} catch (SQLException e) {
			status = Status.CONNECTION_FAILED;
			log.debug(e.getMessage(), e);
		}

		return status;
	}

	/**
	 * Registers a new user, placing the username, password hash, and salt into
	 * the database if the username does not already exist.
	 *
	 * @param newuser
	 *            - username of new user
	 * @param newpass
	 *            - password of new user
	 * @return {@link Status.OK} if registration successful
	 */
	private Status registerUser(Connection connection, String newuser, String newpass) {

		Status status = Status.ERROR;

		byte[] saltBytes = new byte[16];
		random.nextBytes(saltBytes);

		String usersalt = encodeHex(saltBytes, 32);
		String passhash = getHash(newpass, usersalt);

		try (PreparedStatement create_user = connection.prepareStatement(REGISTER_SQL);
				PreparedStatement create_hist = connection
						.prepareStatement(String.format(CREATE_SEARCHED_SQL, usersalt));
				PreparedStatement create_visit = connection
						.prepareStatement(String.format(CREATE_VISIT_SQL, usersalt));) {
			create_user.setString(1, newuser);
			create_user.setString(2, passhash);
			create_user.setString(3, usersalt);
			create_user.executeUpdate();

			// creates searchterm history table
			create_hist.executeUpdate();
			create_visit.executeUpdate();

			status = Status.OK;
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(ex.getMessage(), ex);
		}

		return status;
	}

	/**
	 * Registers a new user, placing the username, password hash, and salt into
	 * the database if the username does not already exist.
	 *
	 * @param newuser
	 *            - username of new user
	 * @param newpass
	 *            - password of new user
	 * @return {@link Status.OK} if registration successful
	 */
	public Status registerUser(String newuser, String newpass) {
		Status status = Status.ERROR;
		log.debug("Registering " + newuser + ".");

		// make sure we have non-null and non-emtpy values for login
		if (isBlank(newuser) || isBlank(newpass)) {
			status = Status.INVALID_LOGIN;
			log.debug(status);
			return status;
		}

		// try to connect to database and test for duplicate user
		try (Connection connection = db.getConnection();) {
			status = duplicateUser(connection, newuser);

			// if okay so far, try to insert new user
			if (status == Status.OK) {
				status = registerUser(connection, newuser, newpass);
			}
		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}

		return status;
	}

	/**
	 * Gets the salt for a specific user.
	 *
	 * @param connection
	 *            - active database connection
	 * @param user
	 *            - which user to retrieve salt for
	 * @return salt for the specified user or null if user does not exist
	 * @throws SQLException
	 *             if any issues with database connection
	 */
	private String getSalt(Connection connection, String user) throws SQLException {
		assert connection != null;
		assert user != null;

		String salt = null;

		try (PreparedStatement statement = connection.prepareStatement(SALT_SQL);) {
			statement.setString(1, user);

			ResultSet results = statement.executeQuery();

			if (results.next()) {
				salt = results.getString("usersalt");
			}
		}

		return salt;
	}

	/**
	 * Checks if the provided username and password match what is stored in the
	 * database. Requires an active database connection.
	 *
	 * @param username
	 *            - username to authenticate
	 * @param password
	 *            - password to authenticate
	 * @return {@link Status.OK} if authentication successful
	 * @throws SQLException
	 */
	private Status authenticateUser(Connection connection, String username, String password) throws SQLException {

		Status status = Status.ERROR;

		try (PreparedStatement statement = connection.prepareStatement(AUTH_SQL);) {
			String usersalt = getSalt(connection, username);
			String passhash = getHash(password, usersalt);

			statement.setString(1, username);
			statement.setString(2, passhash);

			ResultSet results = statement.executeQuery();
			status = results.next() ? status = Status.OK : Status.INVALID_LOGIN;
		} catch (SQLException e) {
			log.debug(e.getMessage(), e);
			status = Status.SQL_EXCEPTION;
		}

		return status;
	}

	/**
	 * Checks if the provided username and password match what is stored in the
	 * database. Must retrieve the salt and hash the password to do the
	 * comparison.
	 *
	 * @param username
	 *            - username to authenticate
	 * @param password
	 *            - password to authenticate
	 * @return {@link Status.OK} if authentication successful
	 */
	public Status authenticateUser(String username, String password) {
		Status status = Status.ERROR;

		log.debug("Authenticating user " + username + ".");

		try (Connection connection = db.getConnection();) {
			status = authenticateUser(connection, username, password);
		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}

		return status;
	}

	/**
	 * Removes a user from the database if the username and password are
	 * provided correctly.
	 *
	 * @param username
	 *            - username to remove
	 * @param password
	 *            - password of user
	 * @return {@link Status.OK} if removal successful
	 */
	private Status removeUser(Connection connection, String username, String password) {
		Status status = Status.ERROR;

		try (PreparedStatement del_user = connection.prepareStatement(DELETE_SQL);
				PreparedStatement del_user_hist = connection.prepareStatement(DELETE_HIST_SQL);) {
			del_user.setString(1, username);

			int count1 = del_user.executeUpdate();
			int count2 = del_user_hist.executeUpdate();
			status = (count1 == 1 && count2 == 1) ? Status.OK : Status.INVALID_USER;
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status, ex);
		}

		return status;
	}

	/**
	 * Removes a user from the database if the username and password are
	 * provided correctly.
	 *
	 * @param username
	 *            - username to remove
	 * @param password
	 *            - password of user
	 * @return {@link Status.OK} if removal successful
	 */
	public Status removeUser(String username, String password) {
		Status status = Status.ERROR;

		log.debug("Removing user " + username + ".");

		try (Connection connection = db.getConnection();) {
			status = authenticateUser(connection, username, password);

			if (status == Status.OK) {
				status = removeUser(connection, username, password);
			}
		} catch (Exception ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}

		return status;
	}

	/**
	 * updates a users password
	 *
	 * @param username
	 *            - username to remove
	 * @param oldpass
	 *            - password of user
	 * @return {@link Status.OK} if removal successful
	 */
	private Status updatePass(Connection connection, String username, String oldpass, String newpass) {
		Status status = Status.ERROR;

		try (PreparedStatement change_pass = connection.prepareStatement(NEW_PASS_SQL);) {
			String usersalt = getSalt(connection, username);
			String oldpasshash = getHash(oldpass, usersalt);
			String newpasshash = getHash(newpass, usersalt);

			change_pass.setString(1, newpasshash);
			change_pass.setString(2, username);
			change_pass.setString(3, oldpasshash);

			int count = change_pass.executeUpdate();
			status = (count == 1) ? Status.OK : Status.INVALID_USER;
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status, ex);
		}

		return status;
	}

	/**
	 * Updates a users passwords if their input password is correct
	 *
	 * @param username
	 *            - username to remove
	 * @param oldpass
	 *            - password of user
	 * @param newpass
	 *            - new password to change to
	 * @return {@link Status.OK} if removal successful
	 */
	public Status updatePass(String username, String oldpass, String newpass) {
		Status status = Status.ERROR;

		log.debug("Replacing password for " + username + ".");

		try (Connection connection = db.getConnection();) {
			status = authenticateUser(connection, username, oldpass);

			if (status == Status.OK) {
				status = updatePass(connection, username, oldpass, newpass);
			}
		} catch (Exception ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}

		return status;
	}

	/**
	 * Adds a search term to database for a specific user
	 *
	 * @param connection
	 *            - active database connection
	 * @param username
	 *            - which user to add search term
	 * @param searchterm
	 *            - search term to add to database
	 * @param timestamp
	 *            - timestamp for searchterm
	 * @return if add was successful or not
	 * @throws SQLException
	 *             if any issues with database connection
	 */
	private Status addSearched(Connection connection, String username, String searchterm, String timestamp)
			throws SQLException {
		Status status = Status.ERROR;

		String usersalt = getSalt(connection, username);

		try (PreparedStatement add_user_hist = connection
				.prepareStatement(String.format(ADD_SEARCHED_SQL, usersalt));) {

			add_user_hist.setString(1, searchterm);
			add_user_hist.setString(2, timestamp);

			int count = add_user_hist.executeUpdate();
			status = (count == 1) ? Status.OK : Status.INVALID_USER;
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status, ex);
		}

		return status;
	}

	/**
	 * Adds a search term to database for a specific user if they exist and are
	 * logged in
	 *
	 * @param username
	 *            - which user to add search term
	 * @param searchterm
	 *            - search term to add to database
	 * @param timestamp
	 *            - timestamp for searchterm
	 * @throws SQLException
	 *             if any issues with database connection
	 */
	public Status addSearched(String username, String searchterm, String timestamp) {
		Status status = Status.ERROR;

		log.debug("Adding searchterm to history for " + username + ".");

		try (Connection connection = db.getConnection();) {
			status = addSearched(connection, username, searchterm, timestamp);
		} catch (Exception ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}

		return status;
	}

	/**
	 * Truncates the search history for a particular user
	 *
	 * @param connection
	 *            - active database connection
	 * @param username
	 *            - which user to remove search term
	 * @return if delete was successful or not
	 * @throws SQLException
	 *             if any issues with database connection
	 */
	private Status removeSearched(Connection connection, String username) throws SQLException {
		Status status = Status.ERROR;

		String usersalt = getSalt(connection, username);

		try (PreparedStatement del_user_hist = connection
				.prepareStatement(String.format(TRUN_SEARCHED_SQL, usersalt));) {

			int count = del_user_hist.executeUpdate();
			status = (count == 1) ? Status.OK : Status.INVALID_USER;
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status, ex);
		}

		return status;
	}

	/**
	 * Truncates the search history for a particular user if they exist in the
	 * database
	 *
	 * @param username
	 *            - which user to remove search term
	 * @return if delete was successful or not
	 * @throws SQLException
	 *             if any issues with database connection
	 */
	public Status removeSearched(String username) {
		Status status = Status.ERROR;

		log.debug("Removing user history for " + username + ".");

		try (Connection connection = db.getConnection();) {
			status = removeSearched(connection, username);
		} catch (Exception ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}

		return status;
	}

	/**
	 * Gets the search history for a particular user
	 *
	 * @param connection
	 *            - active database connection
	 * @param username
	 *            - which user to get search term history from
	 * @return Map containing the searchterm as a key and timestamp as the value
	 * @throws SQLException
	 *             if any issues with database connection
	 */
	private Map<String, String> getSearched(Connection connection, String username) throws SQLException {
		Status status = Status.ERROR;

		String usersalt = getSalt(connection, username);
		Map<String, String> searchhist = null;

		try (PreparedStatement get_user_hist = connection
				.prepareStatement(String.format(GET_SEARCHED_SQL, usersalt));) {

			searchhist = new HashMap<>();
			ResultSet results = get_user_hist.executeQuery();

			while (results.next()) {
				String term = results.getString("searchterm");
				String timestamp = results.getString("timestamp");
				searchhist.put(term, timestamp);
			}

		} catch (SQLException ex) {
			log.debug(status, ex);
		}
		if (searchhist.isEmpty()) {
			return null;
		}

		return searchhist;
	}

	/**
	 * Gets the search history for a particular user if they exist in the
	 * database
	 *
	 * @param username
	 *            - which user to get search term history from
	 * @return Map containing the searchterm as a key and timestamp as the value
	 * @throws SQLException
	 *             if any issues with database connection
	 */
	public Map<String, String> getSearched(String username) {
		Status status = Status.ERROR;

		log.debug("Getting user history for " + username + ".");

		try (Connection connection = db.getConnection();) {
			return getSearched(connection, username);
		} catch (Exception ex) {
			log.debug(status, ex);
		}

		return null;
	}

	/**
	 * adds a visited link to the database for a particular user
	 *
	 * @param connection
	 *            - active database connection
	 * @param username
	 *            - which user to add visited link to
	 * @param timestamp
	 *            - timestamp of the link visit
	 * @return if the add was ok or not
	 * @throws SQLException
	 *             if any issues with database connection
	 */
	private Status addVisited(Connection connection, String username, String link, String timestamp)
			throws SQLException {
		Status status = Status.ERROR;

		String usersalt = getSalt(connection, username);

		try (PreparedStatement add_user_hist = connection.prepareStatement(String.format(ADD_VISIT_SQL, usersalt));) {

			add_user_hist.setString(1, link);
			add_user_hist.setString(2, timestamp);

			int count = add_user_hist.executeUpdate();
			status = (count == 1) ? Status.OK : Status.INVALID_USER;
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status, ex);
		}

		return status;
	}

	/**
	 * adds a visited link to the database for a particular user
	 *
	 * @param username
	 *            - which user to add visited link to
	 * @param timestamp
	 *            - timestamp of the link visit
	 * @return if the add was ok or not
	 * @throws SQLException
	 *             if any issues with database connection
	 */
	public Status addVisited(String username, String link, String timestamp) {
		Status status = Status.ERROR;

		log.debug("Adding query to history for " + username + ".");

		try (Connection connection = db.getConnection();) {
			status = addVisited(connection, username, link, timestamp);
		} catch (Exception ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}

		return status;
	}

	/**
	 * Truncates visited link history from the database for a particular user
	 *
	 * @param connection
	 *            - active database connection
	 * @param username
	 *            - which user to truncate visit history from
	 * @return if the delete was ok or not
	 * @throws SQLException
	 *             if any issues with database connection
	 */
	private Status removeVisited(Connection connection, String username) throws SQLException {
		Status status = Status.ERROR;

		String usersalt = getSalt(connection, username);

		try (PreparedStatement del_user_hist = connection.prepareStatement(String.format(TRUN_VISIT_SQL, usersalt));) {

			int count = del_user_hist.executeUpdate();
			status = (count == 1) ? Status.OK : Status.INVALID_USER;
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status, ex);
		}

		return status;
	}

	/**
	 * Truncates visited link history from the database for a particular user
	 *
	 * @param username
	 *            - which user to add truncate visit history from
	 * @return if the delete was ok or not
	 * @throws SQLException
	 *             if any issues with database connection
	 */
	public Status removeVisited(String username) {
		Status status = Status.ERROR;

		log.debug("Removing user history for " + username + ".");

		try (Connection connection = db.getConnection();) {
			status = removeVisited(connection, username);
		} catch (Exception ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}

		return status;
	}

	/**
	 * Gets visited link history from the database for a particular user
	 *
	 * @param connection
	 *            - active database connection
	 * @param username
	 *            - which user's visit history to retrieve
	 * @return Map of the visit history with link as the key and timestamp as
	 *         the value
	 * @throws SQLException
	 *             if any issues with database connection
	 */
	private Map<String, String> getVisit(Connection connection, String username) throws SQLException {
		Status status = Status.ERROR;

		String usersalt = getSalt(connection, username);
		Map<String, String> visithist = null;

		try (PreparedStatement get_user_hist = connection.prepareStatement(String.format(GET_VISIT_SQL, usersalt));) {

			visithist = new HashMap<>();
			ResultSet results = get_user_hist.executeQuery();

			while (results.next()) {
				String term = results.getString("link");
				String timestamp = results.getString("timestamp");
				visithist.put(term, timestamp);
			}

		} catch (SQLException ex) {
			log.debug(status, ex);
		}
		if (visithist.isEmpty()) {
			return null;
		}

		return visithist;
	}

	/**
	 * Gets visited link history from the database for a particular user
	 *
	 * @param username
	 *            - which user's visit history to retrieve
	 * @return Map of the visit history with link as the key and timestamp as
	 *         the value
	 * @throws SQLException
	 *             if any issues with database connection
	 */
	public Map<String, String> getVisit(String username) {
		Status status = Status.ERROR;

		log.debug("Getting user visit history for " + username + ".");

		try (Connection connection = db.getConnection();) {
			return getVisit(connection, username);
		} catch (Exception ex) {
			log.debug(status, ex);
		}

		return null;
	}




}