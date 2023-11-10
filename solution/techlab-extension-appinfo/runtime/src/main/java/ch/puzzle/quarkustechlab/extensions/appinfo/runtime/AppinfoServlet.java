package ch.puzzle.quarkustechlab.extensions.appinfo.runtime;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
