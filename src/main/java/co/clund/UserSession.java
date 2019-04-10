package co.clund;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import co.clund.db.DatabaseConnector;
import co.clund.db.model.User;

public class UserSession {

	private final HttpSession session;
	private final User thisUser;
	private final DatabaseConnector dbCon;
	private final String clientIp;

	UserSession(DatabaseConnector dbCon, HttpServletRequest request) {
		session = request.getSession();

		this.dbCon = dbCon;

		String tmpClientIp = request.getRemoteAddr();
		if (tmpClientIp.equals("127.0.0.1")) {
			if (request.getHeader("X-Real-IP") != null) {
				tmpClientIp = request.getHeader("X-Real-IP");
			} else if (request.getHeader("X-Forwarded-For") != null) {
				tmpClientIp = request.getHeader("X-Forwarded-For");
			}
		}

		this.clientIp = tmpClientIp;

		if ((session.getAttribute("userId") == null)) {
			thisUser = null;
		} else {
			int id = ((Integer) session.getAttribute("userId")).intValue();
			thisUser = User.getUserById(dbCon, id);
		}
	}

	public User getThisUser() {
		return thisUser;
	}

	public void setUser(User u) {
		if (u == null) {
			session.removeAttribute("userId");
			return;
		}
		session.setAttribute("userId", new Integer(u.getId()));
	}

	public HttpSession getSession() {
		return session;
	}

	public void clear() {
		session.invalidate();
	}

	public DatabaseConnector getDbCon() {
		return dbCon;
	}

	public String getClientIp() {
		return clientIp;
	}
}
