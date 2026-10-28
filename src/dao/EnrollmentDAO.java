package dao;

import config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.EnrolledSubject;
import util.Formats;

public class EnrollmentDAO {

    /** Returns every subject a student is enrolled in, with its section. */
    public List<EnrolledSubject> findByStudentId(int studentId) throws SQLException {
        String sql = "SELECT sub.subject_code, sub.subject_name, sub.units, sec.section_name, "
                   + "es.school_year, es.grade_level, es.status "
                   + "FROM enrollment_subject es "
                   + "JOIN subject sub ON es.subject_id = sub.subject_id "
                   + "LEFT JOIN section sec ON es.section_id = sec.section_id "
                   + "WHERE es.student_id = ? "
                   + "ORDER BY es.school_year DESC, sub.subject_name";

        List<EnrolledSubject> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new EnrolledSubject(
                            Formats.text(rs.getString("subject_code")),
                            Formats.text(rs.getString("subject_name")),
                            Formats.text(rs.getString("units")),
                            Formats.text(rs.getString("section_name")),
                            Formats.text(rs.getString("school_year")),
                            Formats.text(rs.getString("grade_level")),
                            rs.getString("status")));
                }
            }
        }
        return list;
    }
}
