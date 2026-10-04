package scheduler.persistence.csv;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import scheduler.accounts.RegisteredUser;
import scheduler.accounts.RegisteredUserFactory;
import scheduler.persistence.UserRepository;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * «Adapter» — CsvUserRepository
 *
 * Adapter pattern roles:
 *   Target  : UserRepository (the interface the rest of the system expects)
 *   Adaptee : CsvReader / CsvWriter (third-party javacsv.jar library)
 *   Adapter : this class — translates loadUsers()/saveUsers() calls into
 *             the CSV library's readRecord()/get()/write() calls.
 *
 * WHY THIS CLASS DOES *NOT* GO THROUGH AccountManagement:
 * AccountManagement enforces the business rules for CREATING a brand-new
 * account (unique email, strong password, verified university account).
 * This repository does something different: it REHYDRATES users that were
 * already validated when they were originally registered. Re-running the
 * validation here would be incorrect — e.g. verifyUniqueEmail() would
 * reject every stored user as a "duplicate" of its own saved record, and
 * a future change to the password policy would make old accounts fail to
 * load. Persistence must faithfully restore state, not re-judge it.
 *
 * Object CONSTRUCTION, however, is still centralized: instead of a local
 * switch statement duplicating the type-to-class mapping, we delegate to
 * RegisteredUserFactory («Creator» in the Factory Method pattern), which
 * is the single place in the system that knows how to turn an accountType
 * string into the right RegisteredUser subclass.
 */
public class CsvUserRepository implements UserRepository {

    private final String filePath;
    private final RegisteredUserFactory userFactory; //

    private static final String[] HEADERS =
            {"accountType", "email", "password", "userName", "organizationId"};

    public CsvUserRepository(String filePath, RegisteredUserFactory userFactory) {
        this.filePath = filePath;
        this.userFactory = userFactory;
    }

    @Override
    public List<RegisteredUser> loadUsers() {
        List<RegisteredUser> users = new ArrayList<>();
        CsvReader reader = null;
        try {
            reader = new CsvReader(filePath);
            reader.readHeaders();
            while (reader.readRecord()) {
                String accountType = reader.get("accountType");
                String email       = reader.get("email");
                String password    = reader.get("password");
                String userName    = reader.get("userName");
                long orgId         = parseLongOrZero(reader.get("organizationId"));

                // Creation is delegated to the Factory Method — no switch here.
                RegisteredUser user = userFactory.createUser(email, password, accountType, userName, orgId);
                users.add(user);
            }
        } catch (FileNotFoundException e) {
            // First run: no users.csv yet — starting with an empty user list is expected.
            System.out.println("No existing users.csv found, starting empty.");
        } catch (IOException e) {
            // A real read error mid-file should not be silently swallowed.
            throw new RuntimeException("Failed to read users.csv", e);
        } finally {
            if (reader != null) reader.close();
        }
        return users;
    }

    @Override
    public void saveUsers(List<RegisteredUser> users) {
        CsvWriter writer = null;
        try {
            writer = new CsvWriter(new FileWriter(filePath, false), ',');
            for (String h : HEADERS) {
                writer.write(h);
            }
            writer.endRecord();
            for (RegisteredUser u : users) {
                writer.write(u.getAccountType());
                writer.write(u.getEmail());
                writer.write(u.getPassword());
                writer.write(u.getUserName());
                writer.write(String.valueOf(u.getOrganizationId()));
                writer.endRecord();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save users.csv", e);
        } finally {
            if (writer != null) writer.close();
        }
    }

    /** Guards against blank/malformed cells so one bad row can't crash the whole load. */
    private static long parseLongOrZero(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return 0L;
        }
    }
}