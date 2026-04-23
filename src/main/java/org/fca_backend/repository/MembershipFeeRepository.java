package org.fca_backend.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.Date;
import java.util.List;
import java.util.UUID;

import org.fca_backend.config.DataSourceConfig;
import org.fca_backend.entity.ActivityStatus;
import org.fca_backend.entity.Frequency;
import org.fca_backend.entity.MembershipFee;
import org.springframework.stereotype.Repository;

@Repository
public class MembershipFeeRepository {
    private DataSourceConfig dataSourceConfig;

    public List<MembershipFee> findByCollectivityId(UUID id) {
        String sql = """
                    SELECT id, collectivity_id, label, eligible_from, frequency, amount, status, created_at, updated_at
                    FROM membership_fees
                    WHERE collectivity_id = ?
                    ORDER BY created_at DESC
                """;

        List<MembershipFee> fees = new ArrayList<>();
        try (
                Connection conn = dataSourceConfig.dataSource().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    fees.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

        return fees;
    }

    //
    public List<MembershipFee> saveAll(UUID collectivityId, List<MembershipFee> fees) {
        String sql = """
            INSERT INTO membership_fees 
            (collectivity_id, eligible_from, frequency, amount, label, status)
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING id, created_at, updated_at
        """;

        List<MembershipFee> created = new ArrayList<>();
        try (Connection conn = dataSourceConfig.dataSource().getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (MembershipFee fee : fees) {
                    stmt.setObject(1, collectivityId);
                    stmt.setDate(2, Date.valueOf(fee.getEligibleFrom()));
                    stmt.setString(3, fee.getFrequency().name());
                    stmt.setBigDecimal(4, fee.getAmount());
                    stmt.setString(5, fee.getLabel());
                    stmt.setString(6, ActivityStatus.ACTIVE.name());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            MembershipFee saved = mapRow(rs);
                            saved.setCollectivityId(collectivityId);
                            created.add(saved);
                        }
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
        return created;
    }

    private MembershipFee mapRow(ResultSet rs) throws SQLException {
        MembershipFee fee = new MembershipFee();
        fee.setId(rs.getObject("id", UUID.class));
        fee.setCollectivityId(rs.getObject("collectivity_id", UUID.class));
        fee.setLabel(rs.getString("label"));
        fee.setEligibleFrom(rs.getDate("eligible_from").toLocalDate());
        fee.setFrequency(Frequency.valueOf(rs.getString("frequency")));
        fee.setAmount(rs.getBigDecimal("amount"));
        fee.setStatus(ActivityStatus.valueOf(rs.getString("status")));
        fee.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        fee.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
        return fee;
    }
}
