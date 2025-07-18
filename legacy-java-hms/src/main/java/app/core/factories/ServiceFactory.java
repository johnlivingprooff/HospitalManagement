package app.core.factories;

import app.core.Context;
import app.core.Service;
import app.core.annotations.ServiceDescriptor;

public interface ServiceFactory {
    <S extends Service> S getInstance(Class<S> type, ServiceDescriptor meta, Context context) throws Exception;
}
