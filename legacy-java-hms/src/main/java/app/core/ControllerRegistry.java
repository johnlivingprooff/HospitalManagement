package app.core;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ControllerRegistry {
    private final Map<Class<? extends Controller>, Controller> controllerMap;
    private final ServiceContainer serviceContainer;

    public ControllerRegistry(ServiceContainer serviceContainer) {
        this.controllerMap = new LinkedHashMap<>();
        this.serviceContainer = serviceContainer;
    }

    /**
     * <p>Registers the given controller class</p>
     * <p>The method reflectively instantiates the given controller class, calls
     * its {@link Controller#registerRoutes(Map)} and caches its instance and route permissions</p>
     *
     * @param clazz The class to instantiate and register
     * @throws Exception Catch all exception wrapper
     */
    public synchronized void registerController(Class<? extends Controller> clazz) throws Exception {
        final LinkedHashMap<String, String> permissions = new LinkedHashMap<>();
        try {
            Controller controller = controllerMap.get(clazz);
            if (controller == null) {
                controller = clazz.getConstructor(ServiceContainer.class).newInstance(serviceContainer);
                //controller.registerRoutes(permissions);
                controllerMap.put(clazz, controller);
            }
        } catch (Exception e) {
            throw new Exception("Error when registering route controller: " + clazz, e);
        }
    }

    public int getCount() {
        return controllerMap.size();
    }
}
