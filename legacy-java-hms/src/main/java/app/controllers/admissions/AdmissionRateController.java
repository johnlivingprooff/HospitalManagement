package app.controllers.admissions;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.models.admissions.AdmissionRates;
import app.models.permission.AclPermission;
import app.services.admissions.AdmissionsService;
import lib.gintec_rdl.jbeava.validation.Options;
import lib.gintec_rdl.jbeava.validation.ValidationResults;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

@RouteController(path = "/Hms/Admissions/Rates")
public class AdmissionRateController extends Controller {

    @Inject
    private AdmissionsService admissionsService;

    @Action(path = "/", permission = AclPermission.WriteAdmissionRates)
    public String getAdmissionRates(Request request, Response response) {
        Model model;

        model = createModel(request);
        model.put("shortStay", admissionsService.getShortStayAdmissionRate());
        model.put("fullAdmission", admissionsService.getFullAdmissionRate());
        return renderView("admissions/rates/rates.html", model);
    }

    @Action(path = "/Update", method = HttpMethod.post, permission = AclPermission.WriteAdmissionRates)
    public String updateAdmissionRates(Request request, Response response) {
        Model model;
        AdmissionRates rates;
        ValidationResults results;

        results = validate(AdmissionRates.class, Options.defaults().sticky(true), request);

        if (!results.success()) {
            model = createModel(request);
            copyErrorListToModel(model, results);
            copyRawPostDataToModel(model, results);
            return renderView("admissions/rates/rates.html", model);
        }

        rates = results.getBean();
        admissionsService.updateFullAdmissionRate(rates.getFullAdmission());
        admissionsService.updateShortStayAdmissionRate(rates.getShortStay());
        setSessionSuccessMessage("Admission rates updated", request);
        return temporaryRedirect(getBaseUrl(), response);
    }
}
