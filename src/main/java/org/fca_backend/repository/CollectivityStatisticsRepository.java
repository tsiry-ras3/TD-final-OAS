package org.fca_backend.repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.fca_backend.DTO.CollectivityLocalStatistics;
import org.fca_backend.DTO.MemberDescription;
import org.fca_backend.config.DataSourceConfig;
import org.springframework.stereotype.Repository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class CollectivityStatisticsRepository {
    private DataSourceConfig dataSourceConfig;

    public List<CollectivityLocalStatistics> getStatistics(String collectivityId, LocalDate from, LocalDate to) {
    String sql = """
        SELECT 
            m.id,
            m.first_name,
            m.last_name,
            m.email,
            m.occupation,
            COALESCE(SUM(mp.amount), 0) AS earned_amount,
            COALESCE(mf.amount - SUM(mp.amount), mf.amount) AS unpaid_amount
        FROM members m
        JOIN collectivity_members cm ON cm.member_id = m.id
        JOIN membership_fees mf ON mf.collectivity_id = cm.collectivity_id
            AND mf.status = 'ACTIVE'
        LEFT JOIN member_payments mp 
            ON mp.member_id = m.id
            AND mp.membership_fee_id = mf.id
            AND mp.creation_date BETWEEN ? AND ?
        WHERE cm.collectivity_id = ?
        GROUP BY m.id, m.first_name, m.last_name, m.email, m.occupation, mf.amount
    """;

    List<CollectivityLocalStatistics> result = new ArrayList<>();

    try (Connection connection = dataSourceConfig.dataSource().getConnection();
        PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setDate(1, Date.valueOf(from));
        stmt.setDate(2, Date.valueOf(to));
        stmt.setString(3, collectivityId);

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            MemberDescription memberDescription = new MemberDescription(
                rs.getString("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("occupation")
            );

            CollectivityLocalStatistics stats = new CollectivityLocalStatistics(
                memberDescription,
                rs.getBigDecimal("earned_amount"),
                rs.getBigDecimal("unpaid_amount")
            );

            result.add(stats);
        }

    } catch (SQLException e) {
        throw new RuntimeException("Error fetching statistics", e);
    }

    return result;
}
}
