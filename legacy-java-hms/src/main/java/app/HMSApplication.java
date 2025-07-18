package app;

import app.core.Application;
import app.services.scheduling.TaskSchedulerService;
import app.services.templating.TemplateService;
import app.util.Database;
import lib.gintec_rdl.jbeava.validation.Jbeava;
import org.quartz.SchedulerException;
import org.sql2o.GenericDatasource;
import org.sql2o.Sql2o;
import spark.Redirect;
import spark.Request;
import spark.Response;
import validators.HmsValidatorFactory;

import java.net.HttpURLConnection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HMSApplication extends Application {

    public Configuration getConfiguration() {
        return (Configuration) getContext();
    }

    @Override
    protected void onCreate() throws Exception {
        Configuration configuration;

        configuration = getConfiguration();

        Database.MigrateDatabase(configuration);
        getSparkService().ipAddress(configuration.IPAddress);
        getSparkService().port(configuration.ListenPort);
        if (configuration.EnableSSL) {
            getSparkService().secure(
                    configuration.SSLKeystoreFile.getAbsolutePath(),
                    configuration.SSLKeystorePassphrase, null, null
            );
        }
        configuration.setSql2oInstance(
                new Sql2o(
                        new GenericDatasource(
                                configuration.DataSource,
                                configuration.DatabaseUser,
                                configuration.DatabasePassword
                        )
                )
        );

        Map<String, String> mappings = new LinkedHashMap<>();
        mappings.put("bed_id", "bedId");
        mappings.put("ward_id", "wardId");
        mappings.put("nurse_id", "nurseId");
        mappings.put("filer_id", "filerId");
        mappings.put("end_date", "endDate");
        mappings.put("old_code", "oldCode");
        mappings.put("end_time", "endTime");
        mappings.put("doctor_id", "doctorId");
        mappings.put("account_id", "accountId");
        mappings.put("created_at", "createdAt");
        mappings.put("start_date", "startDate");
        mappings.put("start_time", "startTime");
        mappings.put("created_by", "createdBy");
        mappings.put("updated_at", "updatedAt");
        mappings.put("patient_id", "patientId");
        mappings.put("bill_count", "billCount");
        mappings.put("bill_type", "billType");
        mappings.put("updater_id", "updaterId");
        mappings.put("admitted_by", "admittedBy");
        mappings.put("medicine_id", "medicineId");
        mappings.put("scheduled_at", "scheduledAt");
        mappings.put("performed_by", "performedBy");
        mappings.put("procedure_id", "procedureId");
        mappings.put("generic_name", "genericName");
        mappings.put("cancel_reason", "cancelReason");
        mappings.put("permission_key", "permissionKey");
        mappings.put("terminated_by", "terminatedBy");
        mappings.put("admission_id", "admissionId");
        mappings.put("discharged_at", "dischargedAt");
        mappings.put("medicine_name", "medicineName");
        mappings.put("visitation_id", "visitationId");
        mappings.put("selling_price", "sellingPrice");
        mappings.put("purchase_price", "purchasePrice");
        mappings.put("admission_type", "admissionType");
        mappings.put("procedure_type", "procedureType");
        mappings.put("prescription_id", "prescriptionId");
        mappings.put("relationshipType", "relationship");
        mappings.put("termination_reason", "terminationReason");
        mappings.put("termination_attachment", "terminationAttachment");
        configuration.getSql2oInstance().setDefaultColumnMappings(mappings);

        Jbeava.addFactory(new HmsValidatorFactory());

        getSparkService().staticFileLocation("public");
        getSparkService().staticFiles.expireTime(TimeUnit.DAYS.toSeconds(60));
        getSparkService().redirect.any("/", "/Hms/Home", Redirect.Status.MOVED_PERMANENTLY);
    }

    @Override
    protected void onPostCreate() throws SchedulerException {
        getSparkService().notFound(this::notFoundHandler);
        getSparkService().internalServerError(this::errorHandler);
        getSparkService().exception(Exception.class, this::handleException);
        getService(TaskSchedulerService.class).startScheduler();
    }

    private String notFoundHandler(Request request, Response response) {
        response.status(HttpURLConnection.HTTP_NOT_FOUND);
        return renderView("error/404.html");
    }

    private String errorHandler(Request request, Response response) {
        response.status(HttpURLConnection.HTTP_INTERNAL_ERROR);
        return renderView("error/500.html");
    }

    private void handleException(Exception exception, Request request, Response response) {
        if (getConfiguration().DebugMode) {
            exception.printStackTrace();
        }
        getLogger().error("Exception in route", exception);
        response.body(renderView("error/500.html"));
    }

    private String renderView(String template) {
        return getService(TemplateService.class).render(template);
    }
}
