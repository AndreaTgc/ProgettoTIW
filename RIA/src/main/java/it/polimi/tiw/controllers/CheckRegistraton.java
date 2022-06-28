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
import it.polimi.tiw.controllers.utils.ConnectionHandler;
import it.polimi.tiw.controllers.utils.ParamValidator;
import it.polimi.tiw.dao.UserDAO;

@WebServlet("/CheckRegistration")
public class CheckRegistraton extends HttpServlet {
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
		String name = request.getParameter("name");
		String surname = request.getParameter("surname");
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		String confirmPassword = request.getParameter("confirm-password");
		ParamValidator validator = new ParamValidator(response);
		
		//check param validity
		if(!validator.validateRegistration(email, name, surname, password, confirmPassword)) return;
		
		//makes email lowercase
		email = email.toLowerCase();
		//controllo se esiste già un altro user con la stessa mail
		UserDAO userService = new UserDAO(connection);
		boolean duplicate = false;
		try {
			duplicate = userService.checkDuplicateEmail(email);
		} catch (SQLException e){
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().println("Failure in database credential checking");	
			return;
		}
		
		
		User user = null;
		if (duplicate == false) {
			try {
				userService.createCredentials(email, name, surname, password);
				user = userService.checkCredentials(email, password);
			} catch (SQLException e){
				response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
				response.getWriter().println("Failure in database credential creation");
				return;
			}
			 
			request.getSession().setAttribute("user", user);
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().println("User already registered");
			return;
		}
		
	// need to check this one (lines 1 3 4 5)
		request.getSession().setAttribute("user", user);
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(user.getFullName());
	}
	
	@Override
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
		
}
