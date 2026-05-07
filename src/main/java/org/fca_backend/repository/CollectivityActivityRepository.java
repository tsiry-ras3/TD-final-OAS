package org.fca_backend.repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.fca_backend.DTO.CreateCollectivityActivityDto;
import org.fca_backend.config.DataSourceConfig;
import org.fca_backend.entity.ActivityType;
import org.fca_backend.entity.CollectivityActivity;
import org.fca_backend.entity.MemberOccupation;
import org.fca_backend.entity.MonthlyRecurrenceRule;
import org.springframework.stereotype.Repository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class CollectivityActivityRepository {
    private DataSourceConfig dataSourceConfig;

    public List<CollectivityActivity> addNewActivities(String collectivityId,
            List<CreateCollectivityActivityDto> newActivitiesDTOs) {
        List<CollectivityActivity> activities = new ArrayList<>();

        String insertActivitySql = """
                INSERT INTO collectivity_activity (id, collectivity_id, label, activity_type, executive_date)
                VALUES (?, ?, ?, ?, ?)
                """;

        String insertOccupationSql = """
                INSERT INTO activity_occupation_concerned (activity_id, occupation)
                VALUES (?, ?)
                """;

        String insertRecurrenceSql = """
                INSERT INTO activity_recurrence_rule (activity_id, week_ordinal, day_of_week)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = dataSourceConfig.dataSource().getConnection()) {
            conn.setAutoCommit(false);
            PreparedStatement psActivity = conn.prepareStatement(insertActivitySql);
            PreparedStatement psOccupation = conn.prepareStatement(insertOccupationSql);
            PreparedStatement psRecurrence = conn.prepareStatement(insertRecurrenceSql);
            for (CreateCollectivityActivityDto dto : newActivitiesDTOs) {

                String activityId = UUID.randomUUID().toString();

                psActivity.setString(1, activityId);
                psActivity.setString(2, collectivityId);
                psActivity.setString(3, dto.getLabel());
                psActivity.setString(4, dto.getActivityType().name());
                if (dto.getExecutiveDate() != null) {
                    psActivity.setDate(5, Date.valueOf(dto.getExecutiveDate()));
                } else {
                    psActivity.setNull(5, Types.DATE);
                }
                psActivity.executeUpdate();

                if (dto.getMemberOccupationConcerned() != null) {
                    for (MemberOccupation occupation : dto.getMemberOccupationConcerned()) {
                        psOccupation.setString(1, activityId);
                        psOccupation.setString(2, occupation.name());
                        psOccupation.addBatch();
                    }
                    psOccupation.executeBatch();
                }

                if (dto.getRecurrenceRule() != null) {
                    psRecurrence.setString(1, activityId);
                    psRecurrence.setInt(2, dto.getRecurrenceRule().getWeekOrdinal());
                    psRecurrence.setString(3, dto.getRecurrenceRule().getDayOfWeek());
                    psRecurrence.executeUpdate();
                }

                CollectivityActivity activity = findById(conn, activityId);
                activities.add(activity);
            }

            conn.commit();

        } catch (SQLException e) {
            throw new RuntimeException("Database connection error", e);
        }

        return activities;
    }

    public CollectivityActivity findById(Connection conn, String activityId) throws SQLException {

        String selectActivitySql = """
                SELECT id, label, activity_type, executive_date
                FROM collectivity_activity
                WHERE id = ?
                """;

        String selectOccupationsSql = """
                SELECT occupation
                FROM activity_occupation_concerned
                WHERE activity_id = ?
                """;

        String selectRecurrenceSql = """
                SELECT week_ordinal, day_of_week
                FROM activity_recurrence_rule
                WHERE activity_id = ?
                """;

        CollectivityActivity activity = new CollectivityActivity();

        try (PreparedStatement ps = conn.prepareStatement(selectActivitySql)) {
            ps.setString(1, activityId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    activity.setId(rs.getString("id"));
                    activity.setLabel(rs.getString("label"));
                    activity.setActivityType(ActivityType.valueOf(rs.getString("activity_type")));

                    Date executiveDate = rs.getDate("executive_date");
                    if (executiveDate != null) {
                        activity.setExecutiveDate(executiveDate.toLocalDate());
                    }
                } else {
                    throw new RuntimeException("Activity not found with id: " + activityId);
                }
            }
        }

        try (PreparedStatement ps = conn.prepareStatement(selectOccupationsSql)) {
            ps.setString(1, activityId);
            try (ResultSet rs = ps.executeQuery()) {
                List<MemberOccupation> occupations = new ArrayList<>();
                while (rs.next()) {
                    occupations.add(MemberOccupation.valueOf(rs.getString("occupation")));
                }
                activity.setMemberOccupationConcerned(occupations);
            }
        }

        try (PreparedStatement ps = conn.prepareStatement(selectRecurrenceSql)) {
            ps.setString(1, activityId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    MonthlyRecurrenceRule rule = new MonthlyRecurrenceRule();
                    rule.setWeekOrdinal(rs.getInt("week_ordinal"));
                    rule.setDayOfWeek(rs.getString("day_of_week"));
                    activity.setRecurrenceRule(rule);
                }
            }
        }

        return activity;
    }

    public List<CollectivityActivity> getActivities(String collectivityId) {
        List<CollectivityActivity> activities = new ArrayList<>();

        String selectActivitySql = """
                SELECT id, label, activity_type, executive_date
                FROM collectivity_activity
                WHERE collectivity_id = ?
                """;

        try (Connection conn = dataSourceConfig.dataSource().getConnection();
                PreparedStatement ps = conn.prepareStatement(selectActivitySql)) {

            ps.setString(1, collectivityId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CollectivityActivity activity = new CollectivityActivity();
                    activity.setId(rs.getString("id"));
                    activity.setLabel(rs.getString("label"));
                    activity.setActivityType(ActivityType.valueOf(rs.getString("activity_type")));

                    Date executiveDate = rs.getDate("executive_date");
                    if (executiveDate != null) {
                        activity.setExecutiveDate(executiveDate.toLocalDate());
                    }

                    activity.setMemberOccupationConcerned(getOccupations(conn, activity.getId()));
                    activity.setRecurrenceRule(getRecurrenceRule(conn, activity.getId()));

                    activities.add(activity);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve activities for collectivity: " + collectivityId, e);
        }

        return activities;
    }

    private List<MemberOccupation> getOccupations(Connection conn, String activityId) throws SQLException {
        List<MemberOccupation> occupations = new ArrayList<>();
        String sql = "SELECT occupation FROM activity_occupation_concerned WHERE activity_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, activityId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    occupations.add(MemberOccupation.valueOf(rs.getString("occupation")));
                }
            }
        }
        return occupations;
    }

    private MonthlyRecurrenceRule getRecurrenceRule(Connection conn, String activityId) throws SQLException {
        String sql = "SELECT week_ordinal, day_of_week FROM activity_recurrence_rule WHERE activity_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, activityId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    MonthlyRecurrenceRule rule = new MonthlyRecurrenceRule();
                    rule.setWeekOrdinal(rs.getInt("week_ordinal"));
                    rule.setDayOfWeek(rs.getString("day_of_week"));
                    return rule;
                }
            }
        }
        return null;
    }

    public boolean existsById(String activityId) {
        String sql = "SELECT 1 FROM collectivity_activity WHERE id = ?";
        try (Connection conn = dataSourceConfig.dataSource().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, activityId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
