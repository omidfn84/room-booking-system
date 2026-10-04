package scheduler.persistence.sql;

import scheduler.accounts.RegisteredUser;
import scheduler.accounts.RegisteredUserFactory;
import scheduler.persistence.UserRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter: UserRepository backed by SQLite.
 *
 * Like the CSV version, this REHYDRATES already-validated users rather than
 * going through AccountManagement, and delegates object construction to
 * RegisteredUserFactory so the accountType-to-subclass mapping lives in one place.
 */
public class SqliteUserRepository implements UserRepository {

    private final SqliteDatabase db;
    private final RegisteredUserFactory userFactory;

    public SqliteUserRepository(SqliteDatabase db, RegisteredUserFactory userFactory) {
        this.db = db;
        this.userFactory = userFactory;
    }

    @Override
    public List<RegisteredUser> loadUsers() {
        List<RegisteredUser> users = new ArrayList<>();
        String sql = "SELECT accountType, email, password, userName, organizationId FROM users ORDER BY rowid";
        try (Connection conn = db.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                users.add(userFactory.createUser(
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("accountType"),
                        rs.getString("userName"),
                        rs.getLong("organizationId")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load users", e);
        }
        return users;
    }

    @Override
    public void saveUsers(List<RegisteredUser> users) {
        db.inTransaction("users", conn -> {
            try (Statement del = conn.createStatement()) {
                del.executeUpdate("DELETE FROM users");
            }
            String sql = "INSERT INTO users (accountType, email, password, userName, organizationId) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (RegisteredUser u : users) {
                    ps.setString(1, u.getAccountType());
                    ps.setString(2, u.getEmail());
                    ps.setString(3, u.getPassword());
                    ps.setString(4, u.getUserName());
                    ps.setLong(5, u.getOrganizationId());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        });
    }
}
