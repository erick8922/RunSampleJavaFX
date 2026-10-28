package dao;

import config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Grade;
import util.Formats;

public class GradeDAO {

    /** Returns all grades of a student, newest school year first, in grading period order. */
    public List<Grade> findByStudentId(int studentId) throws SQLException {
        String sql = "SELECT sub.subject_code, sub.subject_name, es.school_year, "
                   + "g.grading_period, g.grade, g.remarks "
                   + "FROM grades g "
                   + "JOIN enrollment_subject es ON g.enrollment_subject_id = es.enrollment_subject_id "
                   + "JOIN subject sub ON es.subject_id = sub.subject_id "
                   + "WHERE es.student_id = ? "
                   + "ORDER BY es.school_year DESC, sub.subject_name, "
                   + "FIELD(g.grading_period, 'Prelim', 'Midterm', 'Pre-Final', 'Final')";

        List<Grade> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Grade(
                            Formats.text(rs.getString("subject_code")),
                            Formats.text(rs.getString("subject_name")),
                            Formats.text(rs.getString("school_year")),
                            Formats.text(rs.getString("grading_period")),
                            Formats.text(rs.getString("grade")),
                            rs.getString("remarks")));
                }
            }
        }
        return list;
    }
}
