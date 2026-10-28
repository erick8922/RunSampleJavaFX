package dao;

import config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Credential;
import util.Formats;

public class CredentialDAO {

    public List<Credential> findByStudentId(int studentId) throws SQLException {
        String sql = "SELECT credential_type, issue_date, status "
                   + "FROM credentials WHERE student_id = ? ORDER BY issue_date DESC";

        List<Credential> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Credential(
                            Formats.text(rs.getString("credential_type")),
                            Formats.date(rs.getDate("issue_date")),
                            rs.getString("status")));
                }
            }
        }
        return list;
    }
}
