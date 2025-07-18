package app.core;

import app.Configuration;
import app.core.annotations.Editable;
import app.core.templating.Model;
import app.core.validation.*;
import app.models.Subject;
import app.models.account.Account;
import app.models.doctor.schedule.Schedule;
import app.models.doctor.schedule.ScheduleSerializer;
import app.models.patient.Patient;
import app.models.permission.Permission;
import app.services.system.SystemService;
import app.services.templating.TemplateService;
import app.util.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lib.gintec_rdl.jbeava.validation.Jbeava;
import lib.gintec_rdl.jbeava.validation.Options;
import lib.gintec_rdl.jbeava.validation.ValidationResults;
import lib.gintec_rdl.jbeava.validation.resolvers.SparkFieldResolver;
import org.apache.tika.Tika;
import org.eclipse.jetty.util.MultiPartInputStreamParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Session;
import spark.utils.IOUtils;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * <p>The base controller class. All routes must extend this class</p>
 */
public abstract class Controller {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Schedule.class, new ScheduleSerializer())
            .create();

    private Logger logger;
    private String baseUrl;
    private DataValidator dataValidator;
    private ServiceContainer serviceContainer;
    private Map<String, Set<DataValidator.ValidationContext>> dataSchemaMap;

    private static final HashMap<String, String> EXTENSION_LOOKUP = new LinkedHashMap<>() {{
        put("image/png", "png");
        put("image/jpg", "jpg");
        put("image/jpeg", "jpeg");
        put("application/pdf", "pdf");
        put("application/rtf", "rtf");
        put("application/msword", "doc");
        put("application/vnd.ms-excel", "xls");
    }};

    protected static final List<String> IMAGE_TYPES = List.of("image/png", "image/jpg", "image/jpeg");

    private static final Tika tika = new Tika();

    final void initialize(InitializationParameters initParams, String baseUrl) throws Exception {
        this.dataSchemaMap = new LinkedHashMap<>();
        this.dataValidator = initParams.dataValidator;
        this.serviceContainer = initParams.serviceContainer;
        this.baseUrl = baseUrl;

        // instantiate logger
        this.logger = LoggerFactory.getLogger(getClass());

        // load schemas
        loadValidationSchemas();
    }

    /**
     * <p>Load validation schema file from resources</p>
     */
    private void loadValidationSchemas() throws Exception {
        final Method[] methods = this.getClass().getDeclaredMethods();
        for (final Method method : methods) {
            if (method.isAnnotationPresent(DataValidator.Schema.class)) {
                final DataValidator.Schema schema = method.getDeclaredAnnotation(DataValidator.Schema.class);
                // Windows path separator messes up internal jar resource loading
                final String schemaFile = Paths.get("schemas").resolve(schema.value() + ".json").toString().replace('\\', '/');
                final String schemaName = Paths.get(schema.value()).getFileName().toString();

                // Load schema file
                try (Reader reader = new BufferedReader(new InputStreamReader(getResourceStream(schemaFile)))) {
                    final DataValidator.ValidationSchema[] dataSchemas = gson.fromJson(reader, DataValidator.ValidationSchema[].class);
                    if (dataSchemas != null && dataSchemas.length > 0) {
                        this.dataSchemaMap.put(schemaName, dataValidator.createValidationContexts(dataSchemas));
                    }
                } catch (Exception e) {
                    logger.error("Error loading schema file: {}", schemaFile, e);
                    throw new Exception("Error loading schema file: " + schemaFile, e);
                }
            }
        }
    }

    private InputStream getResourceStream(String name) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    }

    protected final boolean isUserLoggedIn(Request request) {
        return SessionUtil.isUserAuthenticated(request);
    }

    protected final String getBaseUrl() {
        return baseUrl;
    }

    private void copyDataToModel(Map<String, Object> model, Map<String, Object> data) {
        if (data != null && !data.isEmpty()) {
            model.putAll(data);
        }
    }

    /**
     * <p>Convenient method that copies post data from the given request into the target model</p>
     * <p>The post data is collected into a map during validation in {@link #validatePostData(Request, String)}</p>
     * <p><b style="color:red">NOTE: </b>Any existing data in the model with identical keys as that of the post
     * data will be replaced by the latter</p>
     * <p>
     * <b style="color:red">WARNING: </b>
     * The data returned here must not be persisted. It is meant to be sent back to the view as it was posted
     * </p>
     *
     * @param model   Target model to copy data into
     * @param request Request
     */
    protected final void copyRawPostDataToModel(Map<String, Object> model, Request request) {
        copyDataToModel(model, request.attribute("postData"));
    }

    protected final void copyRawPostDataToModel(Map<String, Object> model, ValidationResults results) {
        copyDataToModel(model, results.getRaw());
    }

    private void copyErrorList(Map<String, Object> model, List<String> errorList) {
        if (errorList != null && !errorList.isEmpty()) {
            model.put("errorList", errorList);
        }
    }

    protected final void copyErrorListToModel(Map<String, Object> model, Request request) {
        copyErrorList(model, request.attribute("errorList"));
    }

    protected final void copyErrorListToModel(Map<String, Object> model, ValidationResults results) {
        copyErrorList(model, results.getViolations());
    }

    private Object getMultipartFieldData(String fieldName, Request request) {
        try {
            return IOUtils.toString(request.raw().getPart(fieldName).getInputStream());
        } catch (Exception e) {
            getLogger().error("Error when reading multipart form field " + fieldName, e);
            return null;
        }
    }

    /**
     * @param fileName .
     * @param label    .
     * @param dir      .
     * @param required .
     * @param request  .
     * @return .
     * @see #handleUploadedFile(String, String, File, boolean, Request, List)
     */
    protected final boolean handleUploadedFile(String fileName, String label, File dir, boolean required, Request request) {
        return handleUploadedFile(fileName, label, dir, required, request, null);
    }

    /**
     * <p>Handle a single uploaded file. This method verifies the uploaded file to make sure it's of a valid content type,
     * then copies it to to the given destination path.</p>
     * <p>Additionally, a list of content types can be passed to only accept corresponding files</p>
     *
     * @param fileName      This is actually the name of the part in the assumed multipart form that contains the file
     *                      data. This same name will also be use to obtain the uploaded {@link File} from the
     *                      request's attributes using the {@link Request#attribute(String)} method
     * @param label         The friendly text to use when showing errors
     * @param dir           The destination directory where to save the uploaded file after validation
     * @param required      Whether or not the uploaded file is required. If the file is required but is not present, the method returns false
     * @param request       The request to process
     * @param acceptedTypes (Can be null) Optional list of file types that the uploaded file must match
     * @return <p>The method returns <code>true</code>, otherwise false, if and only if one of the following conditions are met</p>
     * <ul>
     * <li>file is required, present, and of valid content type</li>
     * <li>file is not required and not present</li>
     * </ul>
     */
    protected final boolean handleUploadedFile(String fileName, String label, File dir, boolean required, Request request, List<String> acceptedTypes) {
        MultipartConfigElement mce = request.attribute("org.eclipse.jetty.multipartConfig");
        if (mce == null) {
            final Configuration configuration = getService(SystemService.class).getSystemConfiguration();
            mce = new MultipartConfigElement("tmp", configuration.MaxUploadFileSize,
                    configuration.MaxRequestSize, 0);
            request.attribute("org.eclipse.jetty.multipartConfig", mce);
        }

        // Get file part and write to tmp
        MultiPartInputStreamParser.MultiPart part = null;
        boolean deleteFile = true;
        boolean status = false;

        try {
            part = (MultiPartInputStreamParser.MultiPart) request.raw().getPart(fileName);
            if (part == null || part.getSize() == 0) {
                if (required) {
                    throw new IllegalArgumentException(label + " is required but was not uploaded");
                }
                request.attribute(fileName, null);
                return true;
            }

            // magic part: Save the file to disk first so we can seek the file back and forth
            part.write(Long.toString(System.currentTimeMillis(), 36));

            // get type
            final String type = getFileContentType(part.getFile());
            final String extension = EXTENSION_LOOKUP.get(type);

            // make sure the type and extension are valid
            if (LocaleUtil.isNullOrEmpty(extension)) { // Extension will be null if type is null or unsupported
                throw new IllegalArgumentException("Unsupported file type");
            }

            // Type and extension are supported. Check whether the controller requires specific types
            if (acceptedTypes != null) {
                if (!acceptedTypes.contains(type)) {
                    throw new IllegalArgumentException("Unsupported file type");
                }
            }

            // Move uploaded file to destination (append extension to file)
            File dst = new File(dir, Long.toString(System.currentTimeMillis(), 16) + "." + extension);

            Files.move(part.getFile().toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // save to attributes
            request.attribute(fileName, dst);

            deleteFile = false;
            status = true;
        } catch (IOException | ServletException e) {
            getLogger().error("There was an error processing the {} file.", label, e);
            setSessionErrorMessage("There was an error processing the " + label + " file.", request);
        } catch (IllegalStateException e) {
            setSessionErrorMessage("The size of the file you uploaded is too big to process", request);
        } catch (IllegalArgumentException e) {
            getLogger().error("There was an error processing uploaded file {}.", fileName, e);
            setSessionErrorMessage(e.getMessage(), request);
        } finally {
            if (deleteFile) {
                if (part != null) {
                    try {
                        part.delete();
                    } catch (IOException ignore) {
                    }
                }
            }
        }
        return status;
    }

    protected final File getUploadedFile(String fileName, Request request) {
        return requestAttribute(fileName, request);
    }

    private String getFileContentType(File file) {
        try {
            return tika.detect(file);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * <p>Validate post data using the given schema name. The schema must have already been loaded</p>
     *
     * @param request    The request to pull post data from
     * @param schemaName Schema name
     * @return True if all posted data is valid. False otherwise
     * @throws IllegalArgumentException If given schema could not be found
     * @see DataValidator.ValidationSchema
     * @see DataValidator.Schema
     */
    protected final boolean validatePostData(Request request, String schemaName) throws IllegalArgumentException {

        // Check if schema is registered and loaded
        if (!dataSchemaMap.containsKey(schemaName)) {
            throw new IllegalArgumentException("Validation schema '" + schemaName + "' not found. Register it with @Schema annotation");
        }

        // Get schema context
        final Set<DataValidator.ValidationContext> contextSet = dataSchemaMap.get(schemaName);
        Object input;

        // List of validation errors encountered
        final List<String> validationErrorList = new ArrayList<>();

        // Collection to keep posted data in, as it was posted, for later retrieval, to avoid manually copying to models
        final Map<String, Object> postData = new LinkedHashMap<>();
        final boolean isMultiPart;

        // set multipart configuration if request is multipart
        if ((isMultiPart = request.contentType().startsWith("multipart/form-data"))) {
            final Configuration configuration = getService(SystemService.class).getSystemConfiguration();
            MultipartConfigElement mce = new MultipartConfigElement("tmp",
                    configuration.MaxUploadFileSize, configuration.MaxRequestSize, 0);
            request.attribute("org.eclipse.jetty.multipartConfig", mce);
        }

        for (DataValidator.ValidationContext context : contextSet) {
            try {

                // if the field is an array
                if (context.isArray()) {
                    String fieldName = context.getFieldName();
                    if (!fieldName.endsWith("[]")) {
                        fieldName += "[]";
                    }
                    input = request.queryParamsValues(fieldName);
                } else {
                    input = isMultiPart
                            ? getMultipartFieldData(context.getFieldName(), request)
                            : request.queryParamOrDefault(context.getFieldName(), null);
                }

                // store into the post data
                postData.put(context.getFieldName(), input);

                // Do the actual validation by applying the filters
                input = dataValidator.applyFilters(
                        LocaleUtil.isNullOrEmpty(context.getLabel()) ? context.getFieldName() : context.getLabel(),
                        input,
                        context.getFilterMap()
                );

                // If the input passed the filter without an exception and is not null, stage it for consumption
                if (!ReflectionUtil.isNullOrEmpty(input)) {
                    // Stage the transformed or validated input
                    request.attribute(context.getFieldName(), input);
                }

            } catch (DataValidator.ValidationException e) {
                // Collect validation error
                validationErrorList.add(e.getMessage());
            }
        }

        // Send the errors back to route controller
        if (!validationErrorList.isEmpty()) {
            request.attribute("errorList", validationErrorList);
        }

        // send the post data back to route
        request.attribute("postData", postData);

        return validationErrorList.isEmpty();
    }

    protected final File getAttachmentDirectory() {
        return getService(SystemService.class).getSystemConfiguration().AttachmentDirectory;
    }

    protected final File getImageDirectory() {
        return getService(SystemService.class).getSystemConfiguration().ImageDirectory;
    }

    protected final <T> void copyEditableFieldsToModel(Object instance, Model model) {
        model.putAll(getInstanceFieldValues(instance, 2));
    }

    protected final ValidationResults validate(Class<?> clazz, Options options, Request request) {
        return Jbeava.validate(clazz, new SparkFieldResolver(request), options.sticky(true));
    }

    protected final <T> Map<String, Object> getInstanceFieldValues(Object instance, int depth) {
        int height;
        Class<?> tmp;
        Map<String, Object> values;

        values = new LinkedHashMap<>();

        tmp = instance.getClass();
        height = 0;

        while (height < depth && tmp != null && tmp != Object.class) {
            for (Field field : tmp.getDeclaredFields()) {
                if (field.isAnnotationPresent(Editable.class)) {
                    try {
                        field.trySetAccessible();
                        values.put(field.getName(), field.get(instance));
                    } catch (Exception e) {
                        getLogger().warn("Skipped {} due to access errors", field.getName(), e);
                    }
                }
            }
            height++;
            tmp = tmp.getSuperclass();
        }

        return values;
    }

    protected final <T extends Validatable> boolean validatePostData(Request request, Class<T> tClass, ValidationStage stage)
            throws RuntimeException {
        Object input;
        final boolean isMultiPart;
        final Map<String, Object> postData;
        final PostDataSchema postDataSchema;
        final List<String> validationErrorList;

        postData = new LinkedHashMap<>();
        validationErrorList = new LinkedList<>();

        try {
            postDataSchema = dataValidator.getPostDataSchema(tClass);
        } catch (DataValidator.FilterNotFoundException | DataValidator.ValidationException e) {
            getLogger().error("Error during post data validation for class {}", tClass, e);
            throw new RuntimeException(e);
        }

        if ((isMultiPart = request.contentType().startsWith("multipart/form-data"))) {
            final Configuration configuration = getService(SystemService.class).getSystemConfiguration();
            MultipartConfigElement mce = new MultipartConfigElement("tmp",
                    configuration.MaxUploadFileSize, configuration.MaxRequestSize, 0);
            request.attribute("org.eclipse.jetty.multipartConfig", mce);
        }

        for (PostDataField postField : postDataSchema.getDataFields()) {
            try {
                if (postField.stageMatches(stage)) {
                    if (postField.isArray()) {
                        String fieldName = postField.getFieldName();
                        if (!fieldName.endsWith("[]")) {
                            fieldName += "[]";
                        }
                        input = request.queryParamsValues(fieldName);
                    } else {
                        if (isMultiPart) {
                            input = getMultipartFieldData(postField.getFieldName(), request);
                        } else {
                            input = request.queryParamOrDefault(postField.getFieldName(), null);
                        }
                    }

                    postData.put(postField.getFieldName(), input);

                    input = postField.validate(input);

                    // If the input passed the filter without an exception and is not null, stage it for consumption
                    if (!ReflectionUtil.isNullOrEmpty(input)) {
                        request.attribute(postField.getFieldName(), input);
                    }
                }
            } catch (DataValidator.ValidationException e) {
                validationErrorList.add(e.getMessage());
            }
        }

        // Send the errors back to route controller
        if (!validationErrorList.isEmpty()) {
            request.attribute("errorList", validationErrorList);
        }

        // send the post data back to route
        request.attribute("postData", postData);

        return validationErrorList.isEmpty();
    }

    protected final <T extends Validatable> void copyValidatedData(Request request, T instance, ValidationStage stage) {
        final PostDataSchema postDataSchema;
        try {
            postDataSchema = dataValidator.getPostDataSchema(instance.getClass());
        } catch (DataValidator.FilterNotFoundException | DataValidator.ValidationException e) {
            getLogger().error("Error during post data retrieval for class {}", instance.getClass(), e);
            throw new RuntimeException(e);
        }

        for (PostDataField postDataField : postDataSchema.getDataFields()) {
            if (postDataField.stageMatches(stage)) {
                try {
                    postDataField.setInstance(instance, requestAttribute(postDataField.getFieldName(), request));
                } catch (Exception e) {
                    getLogger().warn("Error when copying post data to instance for schema {}",
                            postDataSchema.getName(), e);
                    throw new RuntimeException(e);
                }
            }
        }
    }

    protected final Response serveFile(Response response, File file) {
        try (InputStream is = new FileInputStream(file)) {
            return serveFile(response, is, file.getName(), getFileContentType(file));
        } catch (IOException e) {
            getLogger().error("Exception when serving file", e);
        }
        return response;
    }

    protected final Response serveFile(Response response, File file, String friendlyName) {
        try (InputStream is = new FileInputStream(file)) {
            return serveFile(
                    response,
                    is,
                    LocaleUtil.stripNonFileNameSymbols(Optional.ofNullable(friendlyName).orElse(file.getName())),
                    getFileContentType(file)
            );
        } catch (IOException e) {
            getLogger().error("Exception when serving file.", e);
        }
        return response;
    }

    protected final Response serveFile(Response response, InputStream inputStream, String fileName, String type) {
        try {
            response.raw().setHeader("Content-disposition", "attachment; filename=" + fileName + ";");
            response.raw().setContentType(type);
            final OutputStream os = response.raw().getOutputStream();
            IoUtils.copyStream(inputStream, os);
        } catch (Exception e) {
            response.status(500);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception ignore) {
                }
            }
        }
        return response;
    }

    protected final Logger getLogger() {
        return logger;
    }

    /**
     * Get a registered service from the underlying container
     *
     * @param service
     * @param <T>
     * @return
     */
    protected final <T extends Service> T getService(Class<T> service) {
        return serviceContainer.getService(service);
    }

    protected final void deleteAttachmentFile(String name) {
        deleteFile(name, getService(SystemService.class).getSystemConfiguration().AttachmentDirectory);
    }

    protected final void deleteProfileImageFile(String name) {
        deleteFile(name, getService(SystemService.class).getSystemConfiguration().ImageDirectory);
    }

    protected final void deleteFile(String name, File dir) {
        if (name != null) {
            File file = new File(dir, name);
            if (file.isFile()) {
                if (!file.delete()) {
                    logger.warn("Failed to delete file {}", file.getAbsoluteFile());
                }
            }
        }
    }

    /**
     * <p>Crete a model for authenticated users</p>
     * <p>The method adds all session related data to the model. The base view template requires some of this data.</p>
     *
     * @param request Request to access session data from
     * @return Model containing user session data
     */
    protected final Model createModel(Request request) throws IllegalStateException {
        final Model model = new Model();
        final Session session = request.session();

        if (session != null) {
            model.put("account", session.attribute("account"));
            model.put("userPermissions", session.attribute("userPermissions"));
            model.put("userRole", session.attribute("userRole"));
            loadSystemData(model);
            loadViewBag(model, request);

            copyMessagesToModel(session, model);
        } else {
            throw new IllegalStateException("This method must be called from a valid session context");
        }

        return model;
    }

    protected final Model createModel() {
        Model model = new Model();
        loadSystemData(model);
        return model;
    }

    protected final void loadViewBag(Model model, Request request) {
        Session session = request.session();
        if (session != null) {
            model.put("viewBag", session.attribute("viewBag"));
            session.removeAttribute("viewBag");
        }
    }

    protected final void copyMessagesToModel(Request request, Map<String, Object> model) {
        copyMessagesToModel(request.session(false), model);
    }

    protected final void copyMessagesToModel(Session session, Map<String, Object> model) {
        if (session != null) {
            // Remove session error and success message
            final String errorMessage = session.attribute("errorMessage");
            final String successMessage = session.attribute("successMessage");
            final String infoMessage = session.attribute("infoMessage");

            if (!LocaleUtil.isNullOrEmpty(errorMessage)) {
                session.removeAttribute("errorMessage");
                model.put("errorMessage", errorMessage);
            }

            if (!LocaleUtil.isNullOrEmpty(successMessage)) {
                session.removeAttribute("successMessage");
                model.put("successMessage", successMessage);
            }

            if (!LocaleUtil.isNullOrEmpty(infoMessage)) {
                session.removeAttribute("infoMessage");
                model.put("infoMessage", infoMessage);
            }
        }
    }

    private void loadSystemData(Model model) {
        model.put("websiteBanner", getService(SystemService.class).getSettings().getBanner());
    }

    protected final String renderView(String view) {
        return renderView(view, Collections.EMPTY_MAP);
    }

    protected final String renderView(String view, Map model) {
        return getService(TemplateService.class).render(view, model);
    }

    protected final String getQueryParameter(Request request, String name) {
        final String parameter = request.params(name);
        if (LocaleUtil.isNullOrEmpty(parameter)) {
            return null;
        }
        return parameter;
    }

    protected final <T extends Number> T getNumericQueryParameter(Request request, String name, Class<T> type) {
        try {
            final String parameter = request.params(name);
            if (LocaleUtil.isNullOrEmpty(parameter)) {
                return null;
            }
            return type.cast(type.getDeclaredMethod("valueOf", String.class).invoke(null, parameter));
        } catch (Exception e) {
            logger.warn("Error parsing numeric value", e);
        }
        return null;
    }

    protected final String resourceNotFound(Response response) {
        response.status(404);
        return renderView("error/404.html");
    }

    protected final String serverError(Response response) {
        response.status(500);
        return renderView("error/500.html");
    }

    protected final String temporaryRedirect(String url, Response response) {
        response.redirect(url, HttpURLConnection.HTTP_MOVED_TEMP);
        return "";
    }

    protected final String makePath(String baseUrl, String... paths) {
        return UrlUtils.make(baseUrl, paths);
    }

    protected final String withBaseUrl(String... paths) {
        return makePath(getBaseUrl(), paths);
    }

    /**
     * <p>Call this method before calling {@link #createModel(Request)}</p>
     *
     * @param message Message to display
     * @param request Request to access session from
     */
    protected final void setSessionErrorMessage(String message, Request request) {
        Session session = request.session();
        session.attribute("errorMessage", message);
    }

    /**
     * <p>Call this method before calling {@link #createModel(Request)}</p>
     *
     * @param message Message to display
     * @param request Request to access session from
     */
    protected final void setSessionSuccessMessage(String message, Request request) {
        Session session = request.session();
        session.attribute("successMessage", message);
    }

    private Map<String, Object> getViewBag(Request request) {
        Map<String, Object> viewBag;
        if ((viewBag = request.session().attribute("viewBag")) == null) {
            request.session().attribute("viewBag", viewBag = new LinkedHashMap<>());
        }
        return viewBag;
    }

    /**
     * Adds an item to a view bag that can be accessed on the next page within this request.
     * The view bag only remains until the next request and is only created during the first call to this method.
     *
     * @param item    Item key to add
     * @param value   Value of item
     * @param request .
     */
    protected final void addViewBagItem(String item, Object value, Request request) {
        getViewBag(request).put(item, value);
    }

    protected final void setSessionInfoMessage(String message, Request request) {
        request.session().attribute("infoMessage", message);
    }

    protected final String notAuthorized(Response response) {
        response.status(403);
        return renderView("error/403.html");
    }

    protected final <T> T getSessionObject(String name, Request request) {
        Session session = request.session(false);
        if (session != null) {
            return session.attribute(name);
        }
        return null;
    }

    protected final void setSessionObject(String name, Object value, Request request) {
        request.session().attribute(name, value);
    }

    protected final void deleteSessionObject(String name, Request request) {
        Session session = request.session(false);
        if (session != null) {
            session.removeAttribute(name);
        }
    }

    protected final <T> T requestAttribute(String name, Request request) {
        return request.attribute(name);
    }

    protected final String postRequestParameter(String name, Request request) {
        return request.queryParamOrDefault(name, null);
    }

    protected final Account getCurrentUser(Request request) {
        return getSessionObject("account", request);
    }

    protected final Subject.SubjectType getSubjectType(Request request) {
        return getSessionObject("subjectType", request);
    }

    protected final Subject getCurrentSubject(Request request) {
        return getSessionObject("account", request);
    }

    protected final Patient getCurrentPatient(Request request) {
        return getSessionObject("account", request);
    }

    boolean checkPermission(String permission, Request request, Response response) throws Exception {
        final List<Permission> permissions = getSessionObject("userPermissions", request);
        return PermissionUtil.hasPermission(permission, permissions);
    }

    /**
     * @return Returns A Gson instance
     */
    protected final Gson getGson() {
        return gson;
    }

    protected final String format(String message, Object... args) {
        return String.format(Locale.US, message, args);
    }
}