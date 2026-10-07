package scheduler.web;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import scheduler.booking.PaymentMethod;

/**
 * Tests for the rules that decide which payment details reach the facade.
 * The important promise: on the public demo site, a card number someone types
 * is checked and then thrown away, never passed on.
 */
public class PaymentInputTest {

    private static final long ORG = 123456789L;

    private static Map<String, Object> body(String... pairs) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put(pairs[i], pairs[i + 1]);
        }
        return map;
    }

    private static Map<String, Object> goodCard() {
        return body("cardNumber", "5555 4444 3333 1111", "expiryDate", "09/31", "cvv", "987");
    }

    private static void assertRejected(String expectedMessage, PaymentMethod method, Map<String, Object> body) {
        for (boolean demoSite : new boolean[] {true, false}) {
            try {
                PaymentInput.forFacade(demoSite, false, method, body, ORG);
                fail("accepted: " + body);
            } catch (HttpError e) {
                assertEquals(400, e.status());
                assertEquals(expectedMessage, e.getMessage());
            }
        }
    }

    // ---------------------------------------------------------------- whose details are used

    @Test
    public void aDemoAccountNeverHasItsRequestRead() {
        // even a nonsense body is fine, because it is not looked at
        String[] fields = PaymentInput.forFacade(true, true, PaymentMethod.CREDIT_CARD, body("cardNumber", "garbage"), ORG);
        assertArrayEquals(DemoData.paymentFields(PaymentMethod.CREDIT_CARD, ORG), fields);
        assertArrayEquals(DemoData.paymentFields(PaymentMethod.DEBIT_CARD, ORG),
                PaymentInput.forFacade(true, true, PaymentMethod.DEBIT_CARD, new HashMap<>(), ORG));
    }

    @Test
    public void onTheDemoSiteATypedCardNumberIsCheckedThenDiscarded() {
        String[] fields = PaymentInput.forFacade(true, false, PaymentMethod.CREDIT_CARD, goodCard(), ORG);
        assertArrayEquals(DemoData.paymentFields(PaymentMethod.CREDIT_CARD, ORG), fields);
        String handedOn = Arrays.toString(fields);
        assertFalse("the typed card number must not be passed on", handedOn.contains("5555"));
        assertFalse("the typed security code must not be passed on", handedOn.contains("987"));
    }

    @Test
    public void onTheDemoSiteATypedPinAndBillingAccountAreDiscardedToo() {
        String debit = Arrays.toString(PaymentInput.forFacade(true, false, PaymentMethod.DEBIT_CARD,
                body("cardNumber", "5555444433331111", "pin", "8642"), ORG));
        assertFalse(debit.contains("5555"));
        assertFalse(debit.contains("8642"));
        String billing = Arrays.toString(PaymentInput.forFacade(true, false, PaymentMethod.INSTITUTIONAL_BILLING,
                body("organizationId", "77", "billingAccount", "REAL-ACCOUNT-9"), ORG));
        assertFalse(billing.contains("REAL-ACCOUNT-9"));
    }

    @Test
    public void inFullModeTheTypedDetailsArePassedOnInTheFacadesOrder() {
        assertArrayEquals(new String[] {"5555444433331111", "09/31", "987"},      // spaces removed from the number
                PaymentInput.forFacade(false, false, PaymentMethod.CREDIT_CARD, goodCard(), ORG));
        assertArrayEquals(new String[] {"5555444433331111", "8642"},
                PaymentInput.forFacade(false, false, PaymentMethod.DEBIT_CARD, body("cardNumber", "5555-4444-3333-1111", "pin", "8642"), ORG));
        assertArrayEquals(new String[] {"77", "ACC-1"},
                PaymentInput.forFacade(false, false, PaymentMethod.INSTITUTIONAL_BILLING, body("organizationId", " 77 ", "billingAccount", " ACC-1 "), ORG));
    }

    // ---------------------------------------------------------------- what is rejected

    @Test
    public void cardNumberMustBeTwelveToNineteenDigits() {
        String message = "Enter the card number (12 to 19 digits).";
        assertRejected(message, PaymentMethod.CREDIT_CARD, body("expiryDate", "09/31", "cvv", "987"));                       // missing
        assertRejected(message, PaymentMethod.CREDIT_CARD, body("cardNumber", "", "expiryDate", "09/31", "cvv", "987"));
        assertRejected(message, PaymentMethod.CREDIT_CARD, body("cardNumber", "1234", "expiryDate", "09/31", "cvv", "987"));
        assertRejected(message, PaymentMethod.CREDIT_CARD, body("cardNumber", "4242 4242 4242 424x", "expiryDate", "09/31", "cvv", "987"));
        assertRejected(message, PaymentMethod.CREDIT_CARD, body("cardNumber", "12345678901234567890", "expiryDate", "09/31", "cvv", "987"));
        assertRejected(message, PaymentMethod.DEBIT_CARD, body("cardNumber", "abc", "pin", "1234"));
    }

    @Test
    public void expiryDateMustBeMonthSlashYear() {
        String message = "Enter the expiry date as MM/YY.";
        for (String bad : new String[] {"", "1230", "13/30", "00/30", "9/31", "09/2031", "ab/cd"}) {
            assertRejected(message, PaymentMethod.CREDIT_CARD, body("cardNumber", "5555444433331111", "expiryDate", bad, "cvv", "987"));
        }
    }

    @Test
    public void securityCodeMustBeThreeOrFourDigits() {
        String message = "Enter the 3 or 4 digit security code.";
        for (String bad : new String[] {"", "12", "12345", "abc"}) {
            assertRejected(message, PaymentMethod.CREDIT_CARD, body("cardNumber", "5555444433331111", "expiryDate", "09/31", "cvv", bad));
        }
    }

    @Test
    public void pinMustBeFourToSixDigits() {
        String message = "Enter the PIN (4 to 6 digits).";
        for (String bad : new String[] {"", "123", "1234567", "abcd"}) {
            assertRejected(message, PaymentMethod.DEBIT_CARD, body("cardNumber", "5555444433331111", "pin", bad));
        }
    }

    @Test
    public void institutionalBillingNeedsANumericIdAndAnAccount() {
        assertRejected("Enter the organization ID (digits only).", PaymentMethod.INSTITUTIONAL_BILLING, body("organizationId", "abc", "billingAccount", "ACC-1"));
        assertRejected("Enter the organization ID (digits only).", PaymentMethod.INSTITUTIONAL_BILLING, body("billingAccount", "ACC-1"));
        assertRejected("Enter the billing account (40 characters at most).", PaymentMethod.INSTITUTIONAL_BILLING, body("organizationId", "77", "billingAccount", "  "));
        assertRejected("Enter the billing account (40 characters at most).", PaymentMethod.INSTITUTIONAL_BILLING, body("organizationId", "77", "billingAccount", "A".repeat(41)));
    }

    @Test
    public void aFieldOfTheWrongTypeCountsAsMissing() {
        Map<String, Object> body = new HashMap<>();
        body.put("cardNumber", 4242424242424242L);    // a JSON number instead of text
        body.put("expiryDate", "09/31");
        body.put("cvv", "987");
        assertRejected("Enter the card number (12 to 19 digits).", PaymentMethod.CREDIT_CARD, body);
    }

    // ---------------------------------------------------------------- what the page is told

    @Test
    public void everyMethodListsTheFieldsItNeeds() {
        assertArrayEquals(new String[] {"cardNumber", "expiryDate", "cvv"}, PaymentInput.fieldNames(PaymentMethod.CREDIT_CARD));
        assertArrayEquals(new String[] {"cardNumber", "pin"}, PaymentInput.fieldNames(PaymentMethod.DEBIT_CARD));
        assertArrayEquals(new String[] {"organizationId", "billingAccount"}, PaymentInput.fieldNames(PaymentMethod.INSTITUTIONAL_BILLING));
    }

    @Test
    public void theDemoDetailsShownOnThePageAreTheOnesTheServerUses() {
        for (PaymentMethod method : PaymentMethod.values()) {
            Map<String, String> shown = PaymentInput.demoDisplay(method, ORG);
            assertArrayEquals(PaymentInput.fieldNames(method), shown.keySet().toArray(new String[0]));
            assertArrayEquals(DemoData.paymentFields(method, ORG), shown.values().toArray(new String[0]));
        }
        assertEquals(String.valueOf(ORG), PaymentInput.demoDisplay(PaymentMethod.INSTITUTIONAL_BILLING, ORG).get("organizationId"));
    }
}
