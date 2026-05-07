package org.fca_backend.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.coyote.BadRequestException;
import org.fca_backend.DTO.CreateActivityMemberAttendanceDto;
import org.fca_backend.DTO.MemberDescriptionDto;
import org.fca_backend.config.DataSourceConfig;
import org.fca_backend.entity.ActivityMemberAttendance;
import org.fca_backend.entity.AttendanceStatusEnum;
import org.fca_backend.entity.MemberOccupation;
import org.springframework.stereotype.Repository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class AttendanceRepository {
    private DataSourceConfig dataSourceConfig;

    public List<ActivityMemberAttendance> addAttendances(
            String activityId,
            List<CreateActivityMemberAttendanceDto> dtos) throws BadRequestException {

        String checkSql = """
                SELECT attendance_status FROM activity_member_attendance
                WHERE activity_id = ? AND member_id = ?
                """;

        String insertSql = """
                INSERT INTO activity_member_attendance (id, activity_id, member_id, attendance_status)
                VALUES (?, ?, ?, ?)
                """;

        List<ActivityMemberAttendance> results = new ArrayList<>();

        try (Connection conn = dataSourceConfig.dataSource().getConnection()) {
            conn.setAutoCommit(false);

            try {
                for (CreateActivityMemberAttendanceDto dto : dtos) {

                    // Vérifier si attendance existe déjà
                    try (PreparedStatement check = conn.prepareStatement(checkSql)) {
                        check.setString(1, activityId);
                        check.setString(2, dto.getMemberIdentifier());
                        try (ResultSet rs = check.executeQuery()) {
                            if (rs.next()) {
                                String existingStatus = rs.getString("attendance_status");
                                // MISSING ou ATTENDED ne peuvent pas être modifiés
                                if (!existingStatus.equals("UNDEFINED")) {
                                    throw new BadRequestException(
                                            "Either malformed provided data through request body or already confirmed attendance for any of provided member");
                                }
                            }
                        }
                    }

                    String attendanceId = UUID.randomUUID().toString();
                    try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
                        insert.setString(1, attendanceId);
                        insert.setString(2, activityId);
                        insert.setString(3, dto.getMemberIdentifier());
                        insert.setString(4, dto.getAttendanceStatus().name());
                        insert.executeUpdate();
                    }

                    results.add(findAttendanceById(conn, attendanceId));
                }

                conn.commit();

            } catch (BadRequestException e) {
                conn.rollback();
                throw e;
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Failed to insert attendance", e);
            }

        } catch (BadRequestException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException("Database connection error", e);
        }

        return results;
    }

    public List<ActivityMemberAttendance> getAttendances(String collectivityId, String activityId) {
        // 1. Membres concernés par l'activité (occupation matching) → UNDEFINED/MISSING
        // 2. Membres ayant ATTENDED → inclus aussi
        String sql = """
                SELECT ama.id, ama.attendance_status,
                       m.id as member_id, m.first_name, m.last_name, m.email, m.occupation
                FROM activity_member_attendance ama
                JOIN members m ON m.id = ama.member_id
                WHERE ama.activity_id = ?
                """;

        List<ActivityMemberAttendance> results = new ArrayList<>();

        try (Connection conn = dataSourceConfig.dataSource().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, activityId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ActivityMemberAttendance attendance = new ActivityMemberAttendance();
                    attendance.setId(rs.getString("id"));
                    attendance.setAttendanceStatus(AttendanceStatusEnum.valueOf(rs.getString("attendance_status")));

                    MemberDescriptionDto desc = new MemberDescriptionDto();
                    desc.setId(rs.getString("member_id"));
                    desc.setFirstName(rs.getString("first_name"));
                    desc.setLastName(rs.getString("last_name"));
                    desc.setEmail(rs.getString("email"));
                    desc.setOccupation(MemberOccupation.valueOf(rs.getString("occupation")));
                    attendance.setMemberDescription(desc);

                    results.add(attendance);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve attendances", e);
        }

        return results;
    }

    private ActivityMemberAttendance findAttendanceById(Connection conn, String attendanceId) throws SQLException {
        String sql = """
                SELECT ama.id, ama.attendance_status,
                       m.id as member_id, m.first_name, m.last_name, m.email, m.occupation
                FROM activity_member_attendance ama
                JOIN members m ON m.id = ama.member_id
                WHERE ama.id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, attendanceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ActivityMemberAttendance attendance = new ActivityMemberAttendance();
                    attendance.setId(rs.getString("id"));
                    attendance.setAttendanceStatus(AttendanceStatusEnum.valueOf(rs.getString("attendance_status")));
                    MemberDescriptionDto desc = new MemberDescriptionDto();
                    desc.setId(rs.getString("member_id"));
                    desc.setFirstName(rs.getString("first_name"));
                    desc.setLastName(rs.getString("last_name"));
                    desc.setEmail(rs.getString("email"));
                    desc.setOccupation(MemberOccupation.valueOf(rs.getString("occupation")));
                    attendance.setMemberDescription(desc);

                    return attendance;
                }
            }
        }
        throw new RuntimeException("Attendance not found: " + attendanceId);
    }

}
