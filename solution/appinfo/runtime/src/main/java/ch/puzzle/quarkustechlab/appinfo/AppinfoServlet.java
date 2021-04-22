package ch.puzzle.quarkustechlab.appinfo;

import javax.enterprise.inject.spi.CDI;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet
public class AppinfoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.getWriter().write(getAppinfoService().getAppinfo().asHumanReadableString());
    }

    AppinfoService getAppinfoService() {
        return CDI.current().select(AppinfoService.class).get();
    }
}
