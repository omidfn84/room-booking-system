package scheduler.web;

/**
 * Thrown anywhere in the web layer to end a request with a specific HTTP
 * status and a message that is safe to show to the person using the site.
 */
final class HttpError extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int status; // the HTTP status code, e.g. 404

    HttpError(int status, String message) {
        super(message);
        this.status = status;
    }

    int status() {
        return status;
    }
}
