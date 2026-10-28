package dao;

import config.DBConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.Student;

public class StudentDAO {

    /** Finds the student profile that belongs to a logged-in user account. Returns null if none. */
    public Student findByUserId(int userId) throws SQLException {
        String sql = "SELECT s.student_id, s.student_no, s.first_name, s.middle_name, s.last_name, "
                   + "s.suffix, s.birth_date, s.gender, s.address, s.contact_no, s.status, "
                   + "u.email, u.username "
                   + "FROM students s JOIN users u ON s.user_id = u.user_id "
                   + "WHERE s.user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Student s = new Student();
                s.setStudentId(rs.getInt("student_id"));
                s.setStudentNo(rs.getString("student_no"));
                s.setFirstName(rs.getString("first_name"));
                s.setMiddleName(rs.getString("middle_name"));
                s.setLastName(rs.getString("last_name"));
                s.setSuffix(rs.getString("suffix"));
                Date birthDate = rs.getDate("birth_date");
                if (birthDate != null) {
                    s.setBirthDate(birthDate.toLocalDate());
                }
                s.setGender(rs.getString("gender"));
                s.setAddress(rs.getString("address"));
                s.setContactNo(rs.getString("contact_no"));
                s.setStatus(rs.getString("status"));
                s.setEmail(rs.getString("email"));
                s.setUsername(rs.getString("username"));
                return s;
            }
        }
    }

    /** Returns the student_id of a user account, or -1 if the account has no student record. */
    public int findStudentIdByUserId(int userId) throws SQLException {
        String sql = "SELECT student_id FROM students WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("student_id") : -1;
            }
        }
    }

    /** Updates the contact number and address of a student. Empty values are saved as NULL. */
    public void updateContactInfo(int studentId, String contactNo, String address) throws SQLException {
        String sql = "UPDATE students SET contact_no = ?, address = ?, updated_at = NOW() "
                   + "WHERE student_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, contactNo.isEmpty() ? null : contactNo);
            ps.setString(2, address.isEmpty() ? null : address);
            ps.setInt(3, studentId);
            ps.executeUpdate();
        }
    }
}
