package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.controllers.utils.ConnectionHandler;
import it.polimi.tiw.controllers.utils.ParamValidator;

@WebServlet("/CheckLogin")
public class CheckLogin extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		
		ParamValidator validator = new ParamValidator(response);
		
		if(!validator.validateLogin(email, password)) return;
		
		//makes email lowercase
		email = email.toLowerCase();
		
		UserDAO userService = new UserDAO(connection);
		User user = null;
		try {
			user = userService.checkCredentials(email, password);
		} catch (SQLException e){
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in database credential checking");
			return;
		}
		
		String path = getServletContext().getContextPath();
		if (user == null) {
			path += "/index.html";
		} else {
			request.getSession().setAttribute("user", user);
			path += "/GoToHomePage";
		}
		response.sendRedirect(path);
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}
}
