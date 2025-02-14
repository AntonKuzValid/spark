package spark.embeddedserver.jetty.websocket;

import static java.util.Objects.requireNonNull;
import static spark.embeddedserver.jetty.websocket.WebSocketHandlerWrapper.validateHandlerClass;

public class WebSocketHandlerClassWrapper implements WebSocketHandlerWrapper {

    private final Class<?> handlerClass;

    public WebSocketHandlerClassWrapper(Class<?> handlerClass) {
        requireNonNull(handlerClass, "WebSocket handler class cannot be null");
        validateHandlerClass(handlerClass);
        this.handlerClass = handlerClass;
    }

    @Override
    public Object getHandler() {
        try {
            return handlerClass.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException("Could not instantiate websocket handler", ex);
        }
    }

}
