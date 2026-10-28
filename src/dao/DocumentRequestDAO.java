package dao;

import config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.DocumentRequest;
import util.Formats;

public class DocumentRequestDAO {

    /** Saves a new request with the status "pending". Returns the new record id (0 if unknown). */
    public int create(int studentId, String documentType, String purpose) throws SQLException {
        String sql = "INSERT INTO document_requests "
                   + "(student_id, document_type, purpose, status, requested_at, created_at, updated_at) "
                   + "VALUES (?, ?, ?, 'pending', NOW(), NOW(), NOW())";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, studentId);
            ps.setString(2, documentType);
            ps.setString(3, purpose);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return 0;
    }

    /** Returns all requests of a student, newest first. */
    public List<DocumentRequest> findByStudentId(int studentId) throws SQLException {
        String sql = "SELECT document_type, purpose, status, requested_at "
                   + "FROM document_requests WHERE student_id = ? ORDER BY requested_at DESC";

        List<DocumentRequest> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new DocumentRequest(
                            Formats.text(rs.getString("document_type")),
                            Formats.text(rs.getString("purpose")),
                            rs.getString("status"),
                            Formats.dateTime(rs.getTimestamp("requested_at"))));
                }
            }
        }
        return list;
    }
}
