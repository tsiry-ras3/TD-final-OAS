package org.fca_backend.utils;

import org.fca_backend.entity.*;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FinancialAccountMapper {

    public static FinancialAccount mapFinancialAccount(ResultSet rs) throws SQLException {
        String type = rs.getString("account_type");

        if ("CASH".equals(type)) {
            CashAccount account = new CashAccount();
            account.setId(rs.getString("id"));
            account.setAmount(rs.getDouble("amount"));
            return account;
        } else if ("MOBILE_BANKING".equals(type)) {
            MobileBankingAccount account = new MobileBankingAccount();
            account.setId(rs.getString("id"));
            account.setAmount(rs.getDouble("amount"));
            account.setHolderName(rs.getString("holder_name"));
            account.setMobileBankingService(
                    MobileBankingService.valueOf(rs.getString("mobile_banking_service"))
            );
            account.setMobileNumber(rs.getString("mobile_number"));
            return account;
        } else if ("BANK".equals(type)) {
            BankAccount account = new BankAccount();
            account.setId(rs.getString("id"));
            account.setAmount(rs.getDouble("amount"));
            account.setHolderName(rs.getString("holder_name"));
            account.setBankName(Bank.valueOf(rs.getString("bank_name")));
            account.setBankCode(rs.getInt("bank_code"));
            account.setBankBranchCode(rs.getInt("bank_branch_code"));
            account.setBankAccountNumber(rs.getInt("bank_account_number"));
            account.setBankAccountKey(rs.getInt("bank_account_key"));
            return account;
        } else {
            throw new SQLException("Unknown account type: " + type);
        }
    }
}