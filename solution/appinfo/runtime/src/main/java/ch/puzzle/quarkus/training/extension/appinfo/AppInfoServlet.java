package ch.puzzle.quarkus.training.extension.appinfo;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigSource;

import javax.enterprise.inject.spi.CDI;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet
public class AppInfoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AppInfo ai = getAppInfoService().getAppInfo();
        resp.getWriter().write("Hello. \n\n" +
                ai.asHumanReadableString());
    }

    AppInfoService getAppInfoService() {
        return CDI.current().select(AppInfoService.class).get();
    }
}
