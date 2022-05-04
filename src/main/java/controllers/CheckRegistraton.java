package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.UserDAO;

@WebServlet("/CheckRegistration")
public class CheckRegistraton extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
	public void init() throws ServletException {
		try {
			ServletContext context = getServletContext();
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String password = context.getInitParameter("dbPassword");
			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, password);

		} catch (ClassNotFoundException e) {
			throw new UnavailableException("Can't load database driver");
		} catch (SQLException e) {
			throw new UnavailableException("Couldn't get db connection");
		}
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String name = request.getParameter("name");
		String surname = request.getParameter("email");
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		
		if (email == null || email.isEmpty() || password == null || password.isEmpty() || 
				name == null || name.isEmpty() || surname == null || surname.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
			return;
		}
		
		UserDAO userService = new UserDAO(connection);
		User user = null;
		try {
			user = userService.checkCredentials(email, password);
		} catch (SQLException e){
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in database credential checking");
			return;
		}
		
		String path = "";
		if (user == null) {
			try {
				userService.createCredentials(email, name, surname, password);
				user = userService.checkCredentials(email, password);
			} catch (SQLException e){
				response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in database credential creation");
				return;
			}
			path = path + "/GotoHomePage";
			request.getSession().setAttribute("user", user);
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User already registered"); 
		}
		
		response.sendRedirect(path);
	}
	
	public void destroy() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException sqle) {
		}
	}
	
	
	
}
