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
import org.fca_backend.entity.CollectivityInfo;
import org.fca_backend.entity.CollectivityOverall;
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

    // get overall statistics for all collectivities
    public List<CollectivityOverall> getOverallStatistics (LocalDate from, LocalDate to) {
        String sql = """
            SELECT
                c.name,
                c.number,
                COUNT(DISTINCT CASE
                    WHEN m.created_at::date BETWEEN ? AND ?
                    THEN m.id
                END) AS new_members_number,
                ROUND(
                    COUNT(DISTINCT CASE
                        WHEN paid.total_paid >= mf.amount
                        THEN m.id
                    END) * 100.0 / NULLIF(COUNT(DISTINCT m.id), 0)
                , 2) AS overall_percentage
            FROM collectivities c
            JOIN collectivity_members cm ON cm.collectivity_id = c.id
            JOIN members m ON m.id = cm.member_id
            JOIN membership_fees mf ON mf.collectivity_id = c.id AND mf.status = 'ACTIVE'
            LEFT JOIN (
                SELECT member_id, membership_fee_id, SUM(amount) AS total_paid
                FROM member_payments
                WHERE creation_date BETWEEN ? AND ?
                GROUP BY member_id, membership_fee_id
            ) paid ON paid.member_id = m.id AND paid.membership_fee_id = mf.id
            GROUP BY c.id, c.name, c.number
        """;
        
       List<CollectivityOverall> result = new ArrayList<>(); 

       try (Connection connection = dataSourceConfig.dataSource().getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(from));
            stmt.setDate(2, Date.valueOf(to));
            stmt.setDate(3, Date.valueOf(from));
            stmt.setDate(4, Date.valueOf(to));

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                CollectivityInfo info = new CollectivityInfo(
                    rs.getString("name"),
                    rs.getInt("number")
                );

                CollectivityOverall stats = new CollectivityOverall(
                    info,
                    rs.getInt("new_members_number"),
                    rs.getBigDecimal("overall_percentage")
                );

                result.add(stats);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching overall statistics", e);
        }

        return result;
    }

}
