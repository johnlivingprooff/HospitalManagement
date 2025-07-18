package app.util;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import spark.ExceptionMapper;
import spark.embeddedserver.EmbeddedServers;
import spark.embeddedserver.jetty.EmbeddedJettyServer;
import spark.embeddedserver.jetty.JettyHandler;
import spark.embeddedserver.jetty.JettyServerFactory;
import spark.http.matching.MatcherFilter;
import spark.route.Routes;
import spark.staticfiles.StaticFilesConfiguration;

import java.util.concurrent.atomic.AtomicBoolean;

public enum SparkSessionUtils {
    ;
    private static final AtomicBoolean flag = new AtomicBoolean();

    /**
     * Set the Embedded Jetty server's cookie name.
     *
     * @param cookieName .
     * @see <a href="https://blog.codecentric.de/en/2017/07/fine-tuning-embedded-jetty-inside-spark-framework/">Here</a>
     */
    public static void setSCookieName(String cookieName) {
        if (flag.compareAndSet(false, true)) {
            EmbeddedServers.add(
                    EmbeddedServers.Identifiers.JETTY,
                    (routes, staticFilesConfiguration, exceptionMapper, hasMultipleHandler) -> {
                        JettyHandler handler = setupHandler(routes, exceptionMapper, staticFilesConfiguration, hasMultipleHandler);
                        handler.getSessionCookieConfig().setName(cookieName);
                        JettyServerFactory factory = new JettyServerFactory() {
                            @Override
                            public Server create(int maxThreads, int minThreads, int threadTimeoutMillis) {
                                if (maxThreads < 0 || minThreads < 0 || threadTimeoutMillis < 0) {
                                    return new Server();
                                } else {
                                    return new Server(new QueuedThreadPool(maxThreads, minThreads, threadTimeoutMillis));
                                }
                            }

                            @Override
                            public Server create(ThreadPool threadPool) {
                                return new Server();
                            }
                        };
                        return new EmbeddedJettyServer(factory, handler);
                    }
            );
        }
    }

    /**
     * setup handler in the same manner spark does in {@code EmbeddedJettyFactory.create()}.
     *
     * @see <a href="https://github.com/perwendel/spark/blob/master/src/main/java/spark/embeddedserver/jetty/EmbeddedJettyFactory.java#L39">EmbeddedJettyFactory.java</a>
     */
    private static JettyHandler setupHandler(Routes routeMatcher, ExceptionMapper mapper, StaticFilesConfiguration staticFilesConfiguration, boolean hasMultipleHandler) {
        MatcherFilter matcherFilter = new MatcherFilter(routeMatcher, staticFilesConfiguration, mapper, false, hasMultipleHandler);
        matcherFilter.init(null);
        return new JettyHandler(matcherFilter);
    }
}
