package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.controllers.utils.ConnectionHandler;
import it.polimi.tiw.dao.AlbumDAO;
import it.polimi.tiw.dao.ImageDAO;

@WebServlet("/AddToAlbum")
@MultipartConfig
public class AddToAlbum extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String albumIdStr = StringEscapeUtils.escapeJava(req.getParameter("albumId"));
		String imageIdStr = StringEscapeUtils.escapeJava(req.getParameter("imageId"));
		int albumId = 0;
		int imageId = 0;

		if (albumIdStr == null || albumIdStr.isEmpty() ||
				imageIdStr == null || imageIdStr.isEmpty()) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp.getWriter().println("Missing parameters");
			return;
		}
		
		try {
			albumId = Integer.parseInt(albumIdStr);
			imageId = Integer.parseInt(imageIdStr);
		} catch (NumberFormatException e) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp.getWriter().println("Invalid parameters");
			return;
		}
		
		AlbumDAO albumService = new AlbumDAO(connection);
		ImageDAO imageService = new ImageDAO(connection);
		
		try {
			if (!albumService.validAlbum(albumId)) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				resp.getWriter().println("Album doesn't exist");
				return;
			}
			if (imageService.findImage(imageId) == null) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				resp.getWriter().println("Image doesn't exist");
				return;
			}
		} catch (SQLException e) {
			resp.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			resp.getWriter().println("Error while checking param existance from DB");
			return;
		}
		
		try {
			imageService.addImageToAlbum(imageId, albumId);
		} catch (SQLException e) {
			e.printStackTrace();
			resp.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			resp.getWriter().println("Error while inserting image into DB");
			return;
		}
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setCharacterEncoding("UTF-8");
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