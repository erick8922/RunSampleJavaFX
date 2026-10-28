package dao;

import config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Writes entries to Audit_Logs so the Admin can see who did what. */
public class AuditLogDAO {

    private AuditLogDAO() { }

    /**
     * Saves one audit log entry. A failure here never stops the main action,
     * it is only printed to the console.
     */
    public static void log(int userId, String action, String tableName, int recordId) {
        String sql = "INSERT INTO audit_logs (user_id, action, table_name, record_id, status) "
                   + "VALUES (?, ?, ?, ?, 'success')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, action);
            ps.setString(3, tableName);
            ps.setInt(4, recordId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
