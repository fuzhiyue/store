public class WebSocketManager {
    private JavaScriptObject webSocket;
    private WebSocketCallback callback;
    private int reconnectAttempts = 0;
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    
    public interface WebSocketCallback {
        void onOpen();
        void onMessage(String message);
        void onError();
        void onClose(int code, String reason, boolean wasClean);
    }
    
    public WebSocketManager(WebSocketCallback callback) {
        this.callback = callback;
    }
    
    public void connect(String url) {
        createWebSocket(url);
    }
    
    private native void createWebSocket(String url) /*-{
        var self = this;
        try {
            var ws = new WebSocket(url);
            this.@com.example.WebSocketManager::webSocket = ws;
            
            ws.onopen = function(event) {
                self.@com.example.WebSocketManager::onOpen()();
            };
            
            ws.onmessage = function(event) {
                var data = event.data;
                self.@com.example.WebSocketManager::onMessage(Ljava/lang/String;)(data);
            };
            
            ws.onerror = function(event) {
                self.@com.example.WebSocketManager::onError()();
            };
            
            ws.onclose = function(event) {
                var code = event.code;
                var reason = event.reason;
                var wasClean = event.wasClean;
                self.@com.example.WebSocketManager::onClose(ILjava/lang/String;Z)(code, reason, wasClean);
            };
            
        } catch (error) {
            console.error('WebSocket 创建失败: ', error);
            self.@com.example.WebSocketManager::onError()();
        }
    }-*/;
    
    private void onOpen() {
        reconnectAttempts = 0;
        if (callback != null) {
            callback.onOpen();
        }
    }
    
    private void onMessage(String message) {
        if (callback != null) {
            callback.onMessage(message);
        }
    }
    
    private void onError() {
        if (callback != null) {
            callback.onError();
        }
    }
    
    private void onClose(int code, String reason, boolean wasClean) {
        // 记录关闭信息
        logCloseEvent(code, reason, wasClean);
        
        if (callback != null) {
            callback.onClose(code, reason, wasClean);
        }
        
        // 如果不是正常关闭，尝试重连
        if (!wasClean && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
            attemptReconnect();
        }
    }
    
    private void logCloseEvent(int code, String reason, boolean wasClean) {
        String logMessage = "WebSocket 关闭 - 代码: " + code + 
                          ", 原因: " + (reason != null ? reason : "无") +
                          ", 正常关闭: " + wasClean;
        Console.log(logMessage);
    }
    
    private void attemptReconnect() {
        reconnectAttempts++;
        Console.log("尝试第 " + reconnectAttempts + " 次重连...");
        
        Timer timer = new Timer() {
            @Override
            public void run() {
                connect(getCurrentUrl());
            }
        };
        timer.schedule(calculateReconnectDelay());
    }
    
    private native String getCurrentUrl() /*-{
        var ws = this.@com.example.WebSocketManager::webSocket;
        if (ws && ws.url) {
            return ws.url;
        }
        return "ws://localhost:8080/websocket";
    }-*/;
    
    private int calculateReconnectDelay() {
        // 指数退避策略
        return Math.min(1000 * (int)Math.pow(2, reconnectAttempts), 30000);
    }
    
    public native void send(String message) /*-{
        var ws = this.@com.example.WebSocketManager::webSocket;
        if (ws && ws.readyState === WebSocket.OPEN) {
            ws.send(message);
        }
    }-*/;
    
    public native void close(int code, String reason) /*-{
        var ws = this.@com.example.WebSocketManager::webSocket;
        if (ws) {
            ws.close(code || 1000, reason);
        }
    }-*/;
    
    public native int getReadyState() /*-{
        var ws = this.@com.example.WebSocketManager::webSocket;
        return ws ? ws.readyState : WebSocket.CLOSED;
    }-*/;
}
