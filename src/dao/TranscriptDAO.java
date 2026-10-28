package dao;

import config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.TranscriptRecord;
import util.Formats;

public class TranscriptDAO {

    public List<TranscriptRecord> findByStudentId(int studentId) throws SQLException {
        String sql = "SELECT school_year, issue_date, remarks, status "
                   + "FROM transcript WHERE student_id = ? ORDER BY issue_date DESC";

        List<TranscriptRecord> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new TranscriptRecord(
                            Formats.text(rs.getString("school_year")),
                            Formats.date(rs.getDate("issue_date")),
                            Formats.text(rs.getString("remarks")),
                            rs.getString("status")));
                }
            }
        }
        return list;
    }
}
