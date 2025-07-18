package app.core;

import app.core.validation.DataValidator;

class InitializationParameters {
    DataValidator dataValidator;
    ServiceContainer serviceContainer;

    public InitializationParameters(DataValidator dataValidator, ServiceContainer serviceContainer) {
        this.dataValidator = dataValidator;
        this.serviceContainer = serviceContainer;
    }
}
