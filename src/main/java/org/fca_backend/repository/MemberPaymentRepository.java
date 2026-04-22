package org.fca_backend.repository;

import lombok.AllArgsConstructor;
import org.fca_backend.DTO.CreateMemberPaymentDTO;
import org.fca_backend.config.DataSourceConfig;
import org.fca_backend.entity.*;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Service
public class MemberPaymentRepository {
    private DataSourceConfig dataSourceConfig;

    public List<MemberPayment> createMemberPayments(String memberId, List<CreateMemberPaymentDTO> payments) throws Exception {
        String getCollectivityAndAccountSql = """
            SELECT m.collectivity_id, mf.collectivity_id as fee_collectivity_id
            FROM members m
            JOIN membership_fees mf ON mf.id = ?::uuid
            WHERE m.id = ?::uuid
        """;

        String insertPaymentSql = """
            INSERT INTO member_payments (member_id, membership_fee_id, account_credited_id, amount, payment_mode)
            VALUES (?::uuid, ?::uuid, ?::uuid, ?, ?::payment_mode)
            RETURNING id, amount, payment_mode, creation_date
        """;

        String insertTransactionSql = """
            INSERT INTO collectivity_transactions (collectivity_id, member_debited_id, account_credited_id, amount, payment_mode)
            VALUES (?::uuid, ?::uuid, ?::uuid, ?, ?::payment_mode)
        """;

        String getAccountSql = """
            SELECT id, account_type, amount,
                   holder_name, mobile_banking_service, mobile_number,
                   bank_name, bank_code, bank_branch_code,
                   bank_account_number, bank_account_key
            FROM financial_accounts
            WHERE id = ?::uuid
        """;

        List<MemberPayment> createdPayments = new ArrayList<>();

        try (Connection conn = dataSourceConfig.dataSource().getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psGetInfo = conn.prepareStatement(getCollectivityAndAccountSql);
                 PreparedStatement psPayment = conn.prepareStatement(insertPaymentSql);
                 PreparedStatement psTransaction = conn.prepareStatement(insertTransactionSql);
                 PreparedStatement psAccount = conn.prepareStatement(getAccountSql)) {

                for (CreateMemberPaymentDTO paymentDTO : payments) {
                    psGetInfo.setString(1, paymentDTO.getMembershipFeeIdentifier());
                    psGetInfo.setString(2, memberId);
                    ResultSet rsInfo = psGetInfo.executeQuery();

                    if (!rsInfo.next()) {
                        throw new SQLException("Member or membership fee not found");
                    }

                    String collectivityId = rsInfo.getString("collectivity_id");
                    String feeCollectivityId = rsInfo.getString("fee_collectivity_id");

                    if (collectivityId == null) {
                        throw new SQLException("Member is not associated with any collectivity");
                    }

                    if (!collectivityId.equals(feeCollectivityId)) {
                        throw new SQLException("Membership fee does not belong to member's collectivity");
                    }

                    psPayment.setString(1, memberId);
                    psPayment.setString(2, paymentDTO.getMembershipFeeIdentifier());
                    psPayment.setString(3, paymentDTO.getAccountCreditedIdentifier());
                    psPayment.setDouble(4, paymentDTO.getAmount());
                    psPayment.setString(5, paymentDTO.getPaymentMode().name());

                    ResultSet rsPayment = psPayment.executeQuery();
                    if (rsPayment.next()) {
                        MemberPayment payment = new MemberPayment();
                        payment.setId(rsPayment.getString("id"));
                        payment.setAmount(rsPayment.getDouble("amount"));
                        payment.setPaymentMode(PaymentMode.valueOf(rsPayment.getString("payment_mode")));
                        payment.setCreationDate(rsPayment.getDate("creation_date").toLocalDate());

                        psAccount.setString(1, paymentDTO.getAccountCreditedIdentifier());
                        ResultSet rsAccount = psAccount.executeQuery();
                        if (rsAccount.next()) {
                            payment.setAccountCredited(mapFinancialAccount(rsAccount));
                        }

                        createdPayments.add(payment);
                    }

                    psTransaction.setString(1, collectivityId);
                    psTransaction.setString(2, memberId);
                    psTransaction.setString(3, paymentDTO.getAccountCreditedIdentifier());
                    psTransaction.setDouble(4, paymentDTO.getAmount());
                    psTransaction.setString(5, paymentDTO.getPaymentMode().name());
                    psTransaction.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
        return createdPayments;
    }

    private FinancialAccount mapFinancialAccount(ResultSet rs) throws SQLException {
        String type = rs.getString("account_type");

        return switch (type) {
            case "CASH" -> {
                CashAccount account = new CashAccount();
                account.setId(rs.getString("id"));
                account.setAmount(rs.getDouble("amount"));
                yield account;
            }
            case "MOBILE_BANKING" -> {
                MobileBankingAccount account = new MobileBankingAccount();
                account.setId(rs.getString("id"));
                account.setAmount(rs.getDouble("amount"));
                account.setHolderName(rs.getString("holder_name"));
                account.setMobileBankingService(
                        MobileBankingService.valueOf(rs.getString("mobile_banking_service"))
                );
                account.setMobileNumber(rs.getString("mobile_number"));
                yield account;
            }
            case "BANK" -> {
                BankAccount account = new BankAccount();
                account.setId(rs.getString("id"));
                account.setAmount(rs.getDouble("amount"));
                account.setHolderName(rs.getString("holder_name"));
                account.setBankName(Bank.valueOf(rs.getString("bank_name")));
                account.setBankCode(rs.getInt("bank_code"));
                account.setBankBranchCode(rs.getInt("bank_branch_code"));
                account.setBankAccountNumber(rs.getInt("bank_account_number"));
                account.setBankAccountKey(rs.getInt("bank_account_key"));
                yield account;
            }
            default -> throw new SQLException("Unknown account type: " + type);
        };
    }
}