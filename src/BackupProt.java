
/**
 * Protokół implementujący komunikację między klientem a serwerem
 *
 */
public class BackupProt {
	static final String LOGIN = "login";
	static final String LOGGEDIN = "loggedin";
	static final String WRONG_LOGIN = "wrong_login";
	static final String WRONG_PASSWORD = "wrong_password";
	static final String LOGIN_EXISTS = "login_exists";
	static final String CREATE = "create";
	static final String CREATED = "created";
	static final String NULL_COMMAND = "null_command";
	static final String LOGOUT = "logout";
	static final String LOGGEDOUT = "loggout";
	static final String ADD_FILE = "add_file";
	static final String ADD_ACCEPTED = "add_accepted";
	static final String ADD_START = "add_start";
	static final String FILE_EXISTS = "file_exists";
	static final String NOT_FOUND = "not_found";
	static final String ADD_SUCCESS = "add_success";
	static final String DELETE_SUCCESS = "delete_success";
	static final String GET_FILE = "get_file";
	static final String SEND_START = "send_start";
	static final String DELETE_FILE = "delete_file";
	static final String MAKE_DIR = "make_dir";
	static final String STOP = "stop";
	static final String STOPPED = "stopped";
}
