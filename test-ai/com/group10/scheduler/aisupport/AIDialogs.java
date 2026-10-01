package com.group10.scheduler.aisupport;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

/**
 * Lets a JUnit test drive a handler that opens a modal JOptionPane.
 *
 * The problem: BookingPanel.handleBook(), handlePay(), handleEdit() and
 * handleExtend() all call JOptionPane.showInputDialog() or
 * showConfirmDialog(). Those calls block until a human answers them, so a
 * plain test that clicks the button simply hangs. The manually written suite
 * responded by declaring those four handlers untestable and leaving them
 * uncovered - which is most of BookingPanel's missing coverage.
 *
 * The approach here: arm a background watcher BEFORE clicking the button.
 * doClick() is called from the test thread, so the modal loop blocks the test
 * thread and leaves the Event Dispatch Thread free. The watcher polls for a
 * visible JDialog, fills in any text fields, and clicks OK or Cancel on the
 * EDT - exactly what a user would do.
 *
 * Every armed watcher is a daemon thread with its own deadline, so a dialog
 * that never appears cannot wedge the build; the test's own @Test(timeout)
 * catches that case and fails properly.
 */
public final class AIDialogs {

    private static final long POLL_MS = 25;
    private static final long DEFAULT_TIMEOUT_MS = 5_000;

    private AIDialogs() {
    }

    /**
     * Answers the next N dialogs by typing the supplied values (one dialog per
     * value, in order) and pressing OK. A null entry means "press Cancel".
     *
     * For a multi-field dialog such as the credit-card form, pass the values
     * for that single dialog as one String[] element using {@link #okWithFields}.
     */
    public static void answerWithText(String... valuesInOrder) {
        arm(new ArrayList<>(List.of(valuesInOrder)), true, DEFAULT_TIMEOUT_MS);
    }

    /** Presses OK on the next dialog, filling its text fields in order. */
    public static void okWithFields(String... fieldValues) {
        armFields(fieldValues, DEFAULT_TIMEOUT_MS);
    }

    /** Presses Cancel on the next dialog that appears. */
    public static void cancelNext() {
        armCancel(DEFAULT_TIMEOUT_MS);
    }

    // ---------- implementation ----------

    private static void arm(List<String> values, boolean ok, long timeoutMs) {
        Thread watcher = new Thread(() -> {
            for (String value : values) {
                JDialog dialog = waitForDialog(timeoutMs);
                if (dialog == null) {
                    return;
                }
                if (value == null) {
                    click(dialog, "Cancel");
                } else {
                    typeInto(dialog, new String[]{value});
                    click(dialog, "OK");
                }
                waitForDialogToClose(dialog, timeoutMs);
            }
        }, "ai-dialog-watcher");
        watcher.setDaemon(true);
        watcher.start();
    }

    private static void armFields(String[] fieldValues, long timeoutMs) {
        Thread watcher = new Thread(() -> {
            JDialog dialog = waitForDialog(timeoutMs);
            if (dialog == null) {
                return;
            }
            typeInto(dialog, fieldValues);
            click(dialog, "OK");
        }, "ai-dialog-watcher");
        watcher.setDaemon(true);
        watcher.start();
    }

    private static void armCancel(long timeoutMs) {
        Thread watcher = new Thread(() -> {
            JDialog dialog = waitForDialog(timeoutMs);
            if (dialog != null) {
                click(dialog, "Cancel");
            }
        }, "ai-dialog-watcher");
        watcher.setDaemon(true);
        watcher.start();
    }

    private static JDialog waitForDialog(long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            for (Window window : Window.getWindows()) {
                if (window instanceof JDialog && window.isVisible()) {
                    return (JDialog) window;
                }
            }
            sleep(POLL_MS);
        }
        return null;
    }

    private static void waitForDialogToClose(JDialog dialog, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline && dialog.isVisible()) {
            sleep(POLL_MS);
        }
    }

    /** Fills the dialog's text components, in the order they appear. */
    private static void typeInto(JDialog dialog, String[] values) {
        runOnEdt(() -> {
            List<JTextComponent> fields = new ArrayList<>();
            collectTextComponents(dialog, fields);
            for (int i = 0; i < values.length && i < fields.size(); i++) {
                if (values[i] != null) {
                    fields.get(i).setText(values[i]);
                }
            }
        });
    }

    private static void click(JDialog dialog, String buttonText) {
        runOnEdt(() -> {
            JButton target = findButton(dialog, buttonText);
            if (target == null) {
                // Locale or look-and-feel may label it differently; fall back
                // to the first button, which JOptionPane makes the OK option.
                target = findAnyButton(dialog);
            }
            if (target != null) {
                target.doClick();
            } else {
                dialog.setVisible(false);
                dialog.dispose();
            }
        });
    }

    private static void collectTextComponents(Container container, List<JTextComponent> out) {
        for (Component c : container.getComponents()) {
            if (c instanceof JTextComponent) {
                out.add((JTextComponent) c);
            }
            if (c instanceof Container) {
                collectTextComponents((Container) c, out);
            }
        }
    }

    private static JButton findButton(Container container, String text) {
        for (Component c : container.getComponents()) {
            if (c instanceof JButton && text.equalsIgnoreCase(((JButton) c).getText())) {
                return (JButton) c;
            }
            if (c instanceof Container) {
                JButton nested = findButton((Container) c, text);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private static JButton findAnyButton(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof JButton) {
                return (JButton) c;
            }
            if (c instanceof Container) {
                JButton nested = findAnyButton((Container) c);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private static void runOnEdt(Runnable action) {
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                action.run();
            } else {
                SwingUtilities.invokeAndWait(action);
            }
        } catch (Exception ignored) {
            // A dialog that vanished between detection and interaction is
            // fine: the test's assertions decide the outcome.
        }
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** A JTextField-typed convenience for tests that need to assert on fields. */
    public static List<JTextField> textFieldsOf(Container container) {
        List<JTextComponent> all = new ArrayList<>();
        collectTextComponents(container, all);
        List<JTextField> result = new ArrayList<>();
        for (JTextComponent c : all) {
            if (c instanceof JTextField) {
                result.add((JTextField) c);
            }
        }
        return result;
    }
}
