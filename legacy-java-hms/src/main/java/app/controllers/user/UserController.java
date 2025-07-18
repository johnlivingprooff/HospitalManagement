package app.controllers.user;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.RouteController;
import app.models.account.Account;
import app.services.user.AccountService;
import spark.Request;
import spark.Response;

import java.io.*;
import java.nio.file.Files;

@RouteController(path = "/Hms/Users")
@SuppressWarnings({"unused"})
public final class UserController extends Controller {

    @Action(path = "/ProfileImage/:account-id", checkPermission = false)
    public Object getUserProfileImage(Request request, Response response) {
        final Account account;
        final File file;
        final Long userId;
        final AccountService accountService;

        userId = getNumericQueryParameter(request, "account-id", Long.class);
        if (userId == null) {
            return resourceNotFound(response);
        }

        accountService = getService(AccountService.class);
        account = accountService.getUserById(userId);

        if (account == null) {
            return resourceNotFound(response);
        }

        try {
            if ((file = accountService.getUserProfileImage(userId)) == null) {
                // serve default (placeholder) file
                try (InputStream inputStream = getClass().getClassLoader()
                        .getResourceAsStream("public/assets/static/placeholder-face-big.png")) {
                    return serveFile(response, inputStream, "profile.png", "image/png");
                }
            } else {
                try (InputStream inputStream = new FileInputStream(file)) {
                    return serveFile(response, inputStream, file.getName(), Files.probeContentType(file.toPath()));
                }
            }
        } catch (FileNotFoundException fne) {
            return resourceNotFound(response);
        } catch (IOException e) {
            return serverError(response);
        }
    }
}