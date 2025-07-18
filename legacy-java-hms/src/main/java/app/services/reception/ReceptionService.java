package app.services.reception;

import app.Configuration;
import app.core.ServiceImpl;
import app.core.annotations.ServiceDescriptor;

/**
 * Represents the reception model
 */
@ServiceDescriptor
public final class ReceptionService extends ServiceImpl {
    public ReceptionService(Configuration configuration) {
        super(configuration);
    }
}