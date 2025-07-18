package app.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for performing various activities
 */
public abstract class Service {
    private final Context context;
    private final Logger logger;
    ServiceContainer serviceContainer;

    /**
     * @param context Abstract context
     */
    public Service(Context context) {
        this.context = context;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    /**
     * Called when service has been created. {@link  #getService(Class)}} will return a valid instance
     */
    protected void onCreate() {
    }

    protected <T extends Service> T getService(Class<T> service) {
        return serviceContainer.getService(service);
    }

    protected final Logger getLogger() {
        return logger;
    }

    protected final Context getContext() {
        return context;
    }
}
