package antidose.antidose;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import timber.log.Timber;

public class SocketConnection {

    // Variables
    private WebSocket webSocket;
    private OkHttpClient client;
    private String serverUrl;

    public SocketConnection(String url, String token) {
        // String url = getResources().getText(R.string.server_url).toString()+"ws";
        // Would be great to get the URL somehow but XML is dogshit so ¯\_(ツ)_/¯
        this.clientInit(url);
        this.connectSocket();
        webSocket.send(token);
    }

    public class SocketListener extends WebSocketListener {

        private void run() {
            client = new OkHttpClient.Builder()
                    .readTimeout(3,  TimeUnit.SECONDS)
                    .build();

            Request request = new Request.Builder()
                    .url(serverUrl)
                    .build();
            client.newWebSocket(request, this);
        }

        @Override public void onMessage(WebSocket webSocket, String text) {
            Timber.d("MESSAGE: " + text);

        }

        @Override public void onMessage(WebSocket webSocket, ByteString bytes) {
            Timber.d("MESSAGE: " + bytes.hex());
        }

        @Override public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(1000, null);
            Timber.d("CLOSE: " + code + " " + reason);
        }

        @Override public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            webSocket.cancel();

            t.printStackTrace();
        }


    }

    public void clientInit(String url) {
        client = new OkHttpClient.Builder()
                .readTimeout(3,  TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        serverUrl = url;
    }

    public void connectSocket() {
        Request request = new Request.Builder()
                .url(serverUrl)
                .build();
        webSocket = client.newWebSocket(request, new SocketListener());
    }

    public void sendMessage(String message) {
        webSocket.send(message);
    }
}
