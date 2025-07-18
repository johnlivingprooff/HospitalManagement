package app.core;

import java.util.HashMap;
import java.util.LinkedHashMap;

public final class ServiceContainer {

    private interface TableInterface {
        <T extends Service> T getService(Class<T> service);

        Service addService(Service service);
    }

    private final TableInterface tableInterface;

    public ServiceContainer() {
        final long ownerThreadId = Thread.currentThread().getId();
        final HashMap<Class<? extends Service>, Service> serviceTable = new LinkedHashMap<>();
        tableInterface = new TableInterface() {
            @Override
            public <T extends Service> T getService(Class<T> service) {
                return service.cast(serviceTable.get(service));
            }

            @Override
            public Service addService(Service service) {
                Thread thread = Thread.currentThread();
                if (ownerThreadId != Thread.currentThread().getId()) {
                    throw new RuntimeException("Thread " + getThreadInfo(thread) + " does not have permission to modify container");
                }
                return serviceTable.put(service.getClass(), service);
            }
        };
    }

    private String getThreadInfo(Thread thread) {
        return thread.getName() + " (" + thread.getId() + ")";
    }

    /**
     * Adds given service and returns old instance of the same type
     *
     * @param service Service to add
     * @return Previous service or null
     */
    public Service addService(Service service) {
        return tableInterface.addService(service);
    }

    public <T extends Service> T getService(Class<T> service) {
        return tableInterface.getService(service);
    }
}
