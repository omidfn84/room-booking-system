package scheduler.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Serves the page itself: index.html, app.js and styles.css from one folder.
 * Anything that is not a plain file inside that folder gets a 404.
 */
final class StaticHandler implements HttpHandler {

    // File extension -> the Content-Type the browser needs to treat the file correctly.
    private static final Map<String, String> TYPES = Map.of(
            "html", "text/html; charset=utf-8",
            "css", "text/css; charset=utf-8",
            "js", "text/javascript; charset=utf-8",
            "svg", "image/svg+xml",
            "png", "image/png",
            "ico", "image/x-icon",
            "txt", "text/plain; charset=utf-8");

    private final Path root; // absolute, normalised path of the public folder

    StaticHandler(Path publicDir) {
        this.root = publicDir.toAbsolutePath().normalize();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            if (!method.equals("GET") && !method.equals("HEAD")) {
                sendText(exchange, 405, "Method not allowed");
                return;
            }
            String path = exchange.getRequestURI().getPath();      // already URL-decoded
            if (path.equals("/")) {
                path = "/index.html";                               // the site's front page
            }

            // If the result is no longer inside the folder, someone asked for
            // something like /../../etc/passwd, and the answer is simply "not found".
            Path file = root.resolve(path.substring(1)).normalize();
            String name = file.getFileName() == null ? "" : file.getFileName().toString();
            int dot = name.lastIndexOf('.');
            String type = dot < 0 ? null : TYPES.get(name.substring(dot + 1).toLowerCase());
            if (!file.startsWith(root) || name.startsWith(".") || type == null || !Files.isRegularFile(file)) {
                sendText(exchange, 404, "Not found");
                return;
            }
            byte[] bytes = Files.readAllBytes(file);
            addSecurityHeaders(exchange);
            exchange.getResponseHeaders().set("Content-Type", type);
            exchange.getResponseHeaders().set("Cache-Control", "no-cache"); // always re-check, so a new deploy shows up at once
            if (method.equals("HEAD")) {
                exchange.sendResponseHeaders(200, -1);              // -1 means "no body"
            } else {
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
            }
        } catch (java.nio.file.InvalidPathException badPath) {
            sendText(exchange, 404, "Not found");                   // e.g. a path containing a NUL character
        } finally {
            exchange.close();
        }
    }

    private static void addSecurityHeaders(HttpExchange exchange) {
        // Only scripts, styles and requests from this same site are allowed, so even
        // if some text slipped into the page as HTML it could not load or run anything.
        exchange.getResponseHeaders().set("Content-Security-Policy",
                "default-src 'self'; frame-ancestors 'none'; base-uri 'none'; form-action 'self'");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff"); // do not guess file types
        exchange.getResponseHeaders().set("X-Frame-Options", "DENY");           // the site cannot be embedded in another page
        exchange.getResponseHeaders().set("Referrer-Policy", "no-referrer");    // do not tell other sites where a visitor came from
    }

    private static void sendText(HttpExchange exchange, int status, String message) throws IOException {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        addSecurityHeaders(exchange);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
    }
}
