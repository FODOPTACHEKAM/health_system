package dao;

import database.DBConnection;
import models.Appointment;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {
    public static boolean create(Appointment appointment) {
        String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date_time, status, notes) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, appointment.getPatientId());
            ps.setInt(2, appointment.getDoctorId());
            ps.setTimestamp(3, java.sql.Timestamp.valueOf(appointment.getDateTime()));
            ps.setString(4, appointment.getStatus());
            ps.setString(5, appointment.getNotes());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    appointment.setId(rs.getInt(1));
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean hasConflict(int patientId, int doctorId, LocalDateTime dateTime) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE (patient_id = ? OR doctor_id = ?) AND appointment_date_time = ? AND status != 'cancelled'";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setInt(2, doctorId);
            ps.setTimestamp(3, java.sql.Timestamp.valueOf(dateTime));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<Appointment> listByPatient(int patientId) {
        List<Appointment> appts = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE patient_id = ? ORDER BY appointment_date_time";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Appointment appt = new Appointment();
                    appt.setId(rs.getInt("id"));
                    appt.setPatientId(rs.getInt("patient_id"));
                    appt.setDoctorId(rs.getInt("doctor_id"));
                    appt.setDateTime(rs.getTimestamp("appointment_date_time").toLocalDateTime());
                    appt.setStatus(rs.getString("status"));
                    appt.setNotes(rs.getString("notes"));
                    appts.add(appt);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appts;
    }

    public static List<Appointment> listByDoctor(int doctorId) {
        List<Appointment> appts = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE doctor_id = ? ORDER BY appointment_date_time";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Appointment appt = new Appointment();
                    appt.setId(rs.getInt("id"));
                    appt.setPatientId(rs.getInt("patient_id"));
                    appt.setDoctorId(rs.getInt("doctor_id"));
                    appt.setDateTime(rs.getTimestamp("appointment_date_time").toLocalDateTime());
                    appt.setStatus(rs.getString("status"));
                    appt.setNotes(rs.getString("notes"));
                    appts.add(appt);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appts;
    }

    public static boolean cancel(int id) {
        String sql = "UPDATE appointments SET status = 'cancelled' WHERE id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

