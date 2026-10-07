package scheduler.web;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/**
 * A pretend browser for the web tests: it sends real HTTP requests to a
 * running server and, like a browser, keeps the cookies it is given. Each
 * instance is a separate visitor.
 */
final class WebTestClient {

    /** One answer from the server. */
    static final class Response {
        final int status;
        final String text;                          // the raw body
        final HttpResponse<String> raw;             // for looking at headers

        Response(HttpResponse<String> raw) {
            this.raw = raw;
            this.status = raw.statusCode();
            this.text = raw.body();
        }

        /** The body parsed as a JSON object. */
        Map<String, Object> object() {
            return Json.parseObject(text);
        }

        /** The body parsed as a JSON array of objects. */
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list() {
            return (List<Map<String, Object>>) Json.parse(text);
        }

        /** Shortcut for the "error" message of a failed request. */
        String error() {
            return (String) object().get("error");
        }

        String header(String name) {
            return raw.headers().firstValue(name).orElse(null);
        }
    }

    private final String base;                      // e.g. http://localhost:54321
    private final HttpClient http = HttpClient.newHttpClient();

    // The session cookie is kept by hand. Java's own CookieManager sends cookies
    // that carry Max-Age back in an old quoted style ($Version="1", sid="...")
    // that no browser uses, so it would not test what a browser really sends.
    private String sessionCookie;

    WebTestClient(int port) {
        this.base = "http://localhost:" + port;
    }

    Response get(String path) throws Exception {
        return send(HttpRequest.newBuilder(URI.create(base + path)).GET());
    }

    /** POSTs a JSON body, the way the page's script does. */
    Response post(String path, String json) throws Exception {
        return send(HttpRequest.newBuilder(URI.create(base + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)));
    }

    /** POSTs with a chosen content type (to check that non-JSON posts are refused). */
    Response postAs(String contentType, String path, String body) throws Exception {
        return send(HttpRequest.newBuilder(URI.create(base + path))
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofString(body)));
    }

    Response method(String method, String path) throws Exception {
        return send(HttpRequest.newBuilder(URI.create(base + path))
                .method(method, HttpRequest.BodyPublishers.noBody()));
    }

    /** The value of the session cookie this client currently holds, or null. */
    String sessionCookie() {
        return sessionCookie;
    }

    private Response send(HttpRequest.Builder request) throws Exception {
        if (sessionCookie != null) {
            request.header("Cookie", "sid=" + sessionCookie);       // what a browser sends back
        }
        HttpResponse<String> raw = http.send(request.build(), HttpResponse.BodyHandlers.ofString());
        for (String setCookie : raw.headers().allValues("Set-Cookie")) {
            if (setCookie.startsWith("sid=")) {
                String value = setCookie.substring(4, setCookie.indexOf(';'));
                // an empty value with Max-Age=0 is the server telling the browser to delete the cookie
                sessionCookie = value.isEmpty() ? null : value;
            }
        }
        return new Response(raw);
    }

    /** Builds a small JSON object from pairs: json("a", "b", "n", 3) -> {"a":"b","n":3}. */
    static String json(Object... pairs) {
        Map<String, Object> map = new java.util.LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((String) pairs[i], pairs[i + 1]);
        }
        return Json.write(map);
    }
}
