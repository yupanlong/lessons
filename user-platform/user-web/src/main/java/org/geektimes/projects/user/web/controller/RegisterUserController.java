package org.geektimes.projects.user.web.controller;

import org.geektimes.projects.user.domain.User;
import org.geektimes.projects.user.service.UserServericeImpl;
import org.geektimes.projects.user.service.UserService;
import org.geektimes.projects.user.sql.DBConnectionManager;
import org.geektimes.web.mvc.controller.PageController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("")
public class RegisterUserController  implements PageController {
    private final Logger logger = Logger.getLogger(RegisterUserController.class.getName());

    private final UserService userServerice = new UserServericeImpl();
    @POST
    @Path("/register")
    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        logger.log(Level.ALL,"方法成功");
        User user = new User();
        user.setEmail(request.getParameter("email"));
        user.setName(request.getParameter("userName"));
        user.setPassword(request.getParameter("password"));
        user.setPhoneNumber(request.getParameter("phone"));
        return userServerice.register(user)?"success.jsp":"fail.jsp";
    }
}
