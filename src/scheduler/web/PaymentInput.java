package scheduler.web;

import java.util.LinkedHashMap;
import java.util.Map;

import scheduler.booking.PaymentMethod;

/**
 * Turns the payment details of a request into the values SchedulerFacade
 * expects, and decides whose details are used:
 *
 *   demo account            -> the server's made-up details; the request is not read at all
 *   own account, demo site  -> what was typed is checked for the right shape, then DISCARDED:
 *                              the made-up details are handed on, so a real card number typed
 *                              into the public site is never kept, in memory or on disk
 *   own account, full mode  -> what was typed is checked and passed on
 */
final class PaymentInput {

    private PaymentInput() {}

    /** The request fields each payment method needs, in the order the facade wants them. */
    static String[] fieldNames(PaymentMethod method) {
        switch (method) {
            case CREDIT_CARD:
                return new String[] {"cardNumber", "expiryDate", "cvv"};
            case DEBIT_CARD:
                return new String[] {"cardNumber", "pin"};
            default:
                return new String[] {"organizationId", "billingAccount"};
        }
    }

    /**
     * @param demoSite     true when the server runs as the public demo
     * @param demoAccount  true when the signed-in session is one of the one-click demo accounts
     * @param body         the parsed JSON body of the request
     */
    static String[] forFacade(boolean demoSite, boolean demoAccount, PaymentMethod method,
                              Map<String, Object> body, long organizationId) {
        if (demoAccount) {
            return DemoData.paymentFields(method, organizationId);
        }
        String[] typed = readAndCheck(method, body);
        return demoSite ? DemoData.paymentFields(method, organizationId) : typed;
    }

    /** The made-up details as name -> value, so the page can show them on the demo payment screen. */
    static Map<String, String> demoDisplay(PaymentMethod method, long organizationId) {
        String[] names = fieldNames(method);
        String[] values = DemoData.paymentFields(method, organizationId);
        Map<String, String> out = new LinkedHashMap<>();
        for (int i = 0; i < names.length; i++) {
            out.put(names[i], values[i]);
        }
        return out;
    }

    // Reads the typed details and rejects anything that does not look like the real thing.
    private static String[] readAndCheck(PaymentMethod method, Map<String, Object> body) {
        switch (method) {
            case CREDIT_CARD: {
                String number = cardNumber(body);
                String expiry = text(body, "expiryDate");
                String cvv = text(body, "cvv");
                if (!expiry.matches("(0[1-9]|1[0-2])/\\d{2}")) {       // two-digit month 01-12, a slash, two-digit year
                    throw new HttpError(400, "Enter the expiry date as MM/YY.");
                }
                if (!cvv.matches("\\d{3,4}")) {
                    throw new HttpError(400, "Enter the 3 or 4 digit security code.");
                }
                return new String[] {number, expiry, cvv};
            }
            case DEBIT_CARD: {
                String number = cardNumber(body);
                String pin = text(body, "pin");
                if (!pin.matches("\\d{4,6}")) {
                    throw new HttpError(400, "Enter the PIN (4 to 6 digits).");
                }
                return new String[] {number, pin};
            }
            default: {
                String organizationId = text(body, "organizationId");
                String billingAccount = text(body, "billingAccount");
                if (!organizationId.matches("\\d{1,18}")) {
                    throw new HttpError(400, "Enter the organization ID (digits only).");
                }
                if (billingAccount.isEmpty() || billingAccount.length() > 40
                        || billingAccount.chars().anyMatch(Character::isISOControl)) {
                    throw new HttpError(400, "Enter the billing account (40 characters at most).");
                }
                return new String[] {organizationId, billingAccount};
            }
        }
    }

    // A card number may be typed with spaces or dashes; they are removed before checking.
    private static String cardNumber(Map<String, Object> body) {
        String number = text(body, "cardNumber").replaceAll("[ -]", "");
        if (!number.matches("\\d{12,19}")) {
            throw new HttpError(400, "Enter the card number (12 to 19 digits).");
        }
        return number;
    }

    // A text field of the body with spaces trimmed; a missing field counts as empty.
    private static String text(Map<String, Object> body, String field) {
        Object value = body.get(field);
        return value instanceof String ? ((String) value).trim() : "";
    }
}
