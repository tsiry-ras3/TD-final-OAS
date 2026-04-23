package org.fca_backend.repository;

import lombok.AllArgsConstructor;
import org.fca_backend.config.DataSourceConfig;
import org.fca_backend.entity.Member;
import org.fca_backend.entity.PaymentMode;
import org.fca_backend.entity.Transaction;
import org.fca_backend.utils.FinancialAccountMapper;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Repository
public class CollectivityTransactionRepository {
    private DataSourceConfig dataSourceConfig;

    public List<Transaction> getCollectivityTransaction(String id) {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = dataSourceConfig.dataSource().getConnection()) {
            PreparedStatement preparedStatement = conn.prepareStatement("""
                     SELECT
                        ct.id,
                        ct.creation_date,
                        ct.amount,
                        ct.payment_mode,
                        ct.account_credited_id,
                        fa.account_type,
                        fa.amount AS account_amount,
                        fa.holder_name,
                        fa.mobile_banking_service,
                        fa.mobile_number,
                        fa.bank_name,
                        fa.bank_code,
                        fa.bank_branch_code,
                        fa.bank_account_number,
                        fa.bank_account_key,
                        m.first_name,
                        m.last_name,
                        m.birth_date,
                        m.gender,
                        m.address,
                        m.profession,
                        m.phone_number,
                        m.email,
                        m.occupation,
                        m.id AS member_id
                    FROM collectivity_transactions ct
                    JOIN members m ON ct.member_debited_id = m.id
                    JOIN financial_accounts fa ON ct.account_credited_id = fa.id
                    WHERE ct.collectivity_id = ?::uuid
                    """);

            preparedStatement.setString(1, id);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                Transaction transaction = new Transaction();
                transaction.setId(rs.getString("id"));
                transaction.setCreationDate(rs.getDate("creation_date").toLocalDate().atStartOfDay());
                transaction.setAmount(rs.getDouble("amount"));
                transaction.setPaymentMode(PaymentMode.valueOf(rs.getString("payment_mode")));

                transaction.setAccountCredited(FinancialAccountMapper.mapFinancialAccount(rs));

                Member member = new Member();
                member.setId(rs.getString("member_id"));
                member.setFirstName(rs.getString("first_name"));
                member.setLastName(rs.getString("last_name"));
                member.setEmail(rs.getString("email"));
                member.setPhoneNumber(rs.getString("phone_number"));
                member.setAddress(rs.getString("address"));
                member.setProfession(rs.getString("profession"));

                transaction.setMemberDebited(member);

                transactions.add(transaction);

            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return transactions;
    }

}
