package dao;

import database.DBConnection;
import models.HealthRecord;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HealthRecordDAO {
    public static boolean create(HealthRecord record) {
        String sql = "INSERT INTO health_records (patient_id, doctor_id, record_date, notes) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, record.getPatientId());
            ps.setInt(2, record.getDoctorId());
            ps.setDate(3, java.sql.Date.valueOf(record.getRecordDate()));
            ps.setString(4, record.getNotes());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<HealthRecord> listByPatient(int patientId) {
        List<HealthRecord> records = new ArrayList<>();
        String sql = "SELECT hr.*, d.name as doctor_name FROM health_records hr JOIN doctors d ON hr.doctor_id = d.id WHERE patient_id = ? ORDER BY record_date DESC";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HealthRecord record = new HealthRecord();
                    record.setId(rs.getInt("id"));
                    record.setPatientId(rs.getInt("patient_id"));
                    record.setDoctorId(rs.getInt("doctor_id"));
                    record.setRecordDate(rs.getDate("record_date").toLocalDate());
                    record.setNotes(rs.getString("notes"));
                    records.add(record);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    public static List<HealthRecord> listByDoctor(int doctorId) {
        List<HealthRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM health_records WHERE doctor_id = ? ORDER BY record_date DESC";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HealthRecord record = new HealthRecord();
                    record.setId(rs.getInt("id"));
                    record.setPatientId(rs.getInt("patient_id"));
                    record.setDoctorId(rs.getInt("doctor_id"));
                    record.setRecordDate(rs.getDate("record_date").toLocalDate());
                    record.setNotes(rs.getString("notes"));
                    records.add(record);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }
}

