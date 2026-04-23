package org.fca_backend.repository;

import lombok.AllArgsConstructor;

import org.fca_backend.DTO.CreateCollectivityDTO;
import org.fca_backend.DTO.CreateCollectivityStructureDTO;
import org.fca_backend.DTO.UpdateCollectivityDTO;
import org.fca_backend.DTO.FinancialAccountDTO;
import org.fca_backend.config.DataSourceConfig;
import org.fca_backend.entity.*;
import org.fca_backend.validator.CollectivityNotFoundException;
import org.fca_backend.validator.CollectivityValidator;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Repository
public class CollectivityRepository {
    private CollectivityValidator collectivityValidator;
    private DataSourceConfig dataSourceConfig;

    public List<Collectivity> addNewListOfCollectivity(List<CreateCollectivityDTO> collectivities) {
        collectivityValidator.validateCreateCollectivity(collectivities);

        List<Collectivity> newCollectivities = new ArrayList<>();

        try (Connection conn = dataSourceConfig.dataSource().getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psCollectivity = conn.prepareStatement("""
                        INSERT INTO collectivities (location, federation_approval)
                        VALUES (?, ?)
                        RETURNING id, location, federation_approval
                    """);
                    PreparedStatement psStructure = conn.prepareStatement(
                            """
                                        INSERT INTO collectivity_structure (collectivity_id, president_id, vice_president_id, treasurer_id, secretary_id)
                                        VALUES (?::uuid, ?::uuid, ?::uuid, ?::uuid, ?::uuid)
                                    """);
                    PreparedStatement psCollectivityMember = conn.prepareStatement("""
                                INSERT INTO collectivity_members (collectivity_id, member_id)
                                VALUES (?::uuid, ?::uuid)
                                ON CONFLICT (collectivity_id, member_id) DO NOTHING
                            """);
                    PreparedStatement psCheckMemberExists = conn.prepareStatement("""
                                SELECT id, first_name, last_name, birth_date, gender, address,
                                       profession, phone_number, email, occupation
                                FROM members WHERE id = ?::uuid
                            """)) {

                for (CreateCollectivityDTO collectivityDTO : collectivities) {
                    psCollectivity.setString(1, collectivityDTO.getLocation());
                    psCollectivity.setBoolean(2, collectivityDTO.getFederationApproval());

                    ResultSet rsCollectivity = psCollectivity.executeQuery();

                    if (!rsCollectivity.next()) {
                        throw new SQLException("Failed to insert collectivity");
                    }

                    String generatedId = rsCollectivity.getString("id");
                    String location = rsCollectivity.getString("location");
                    Boolean federationApproval = rsCollectivity.getBoolean("federation_approval");

                    CreateCollectivityStructureDTO structure = collectivityDTO.getStructure();

                    Member president = getMemberIfExists(conn, structure.getPresident());
                    Member vicePresident = getMemberIfExists(conn, structure.getVicePresident());
                    Member treasurer = getMemberIfExists(conn, structure.getTreasurer());
                    Member secretary = getMemberIfExists(conn, structure.getSecretary());

                    psStructure.setString(1, generatedId);
                    psStructure.setString(2, president.getId());
                    psStructure.setString(3, vicePresident.getId());
                    psStructure.setString(4, treasurer.getId());
                    psStructure.setString(5, secretary.getId());
                    psStructure.executeUpdate();

                    List<Member> collectivityMembers = new ArrayList<>();
                    if (collectivityDTO.getMembers() != null) {
                        for (String memberId : collectivityDTO.getMembers()) {
                            Member member = getMemberIfExists(conn, memberId);
                            collectivityMembers.add(member);

                            psCollectivityMember.setString(1, generatedId);
                            psCollectivityMember.setString(2, member.getId());
                            psCollectivityMember.executeUpdate();
                        }
                    }

                    Collectivity collectivity = new Collectivity();
                    collectivity.setId(generatedId);
                    collectivity.setLocation(location);
                    collectivity.setFederationApproval(federationApproval);

                    CollectivityStructure responseStructure = new CollectivityStructure();
                    responseStructure.setPresident(president);
                    responseStructure.setVicePresident(vicePresident);
                    responseStructure.setTreasurer(treasurer);
                    responseStructure.setSecretary(secretary);
                    collectivity.setStructure(responseStructure);
                    collectivity.setMembers(collectivityMembers);

                    newCollectivities.add(collectivity);
                }

                conn.commit();
                return newCollectivities;

            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Database error: " + e.getMessage(), e);
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Validation error: " + e.getMessage(), e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Connection error: " + e.getMessage(), e);
        }
    }

    private Member getMemberIfExists(Connection conn, String memberId) throws Exception {
        if (memberId == null || memberId.isEmpty()) {
            throw new Exception("Member ID cannot be null or empty");
        }

        String sql = "SELECT id, first_name, last_name, birth_date, gender, address, " +
                "profession, phone_number, email, occupation " +
                "FROM members WHERE id = ?::uuid";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, memberId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                throw new Exception("Member not found with ID: " + memberId);
            }

            Member member = new Member();
            member.setId(rs.getString("id"));
            member.setFirstName(rs.getString("first_name"));
            member.setLastName(rs.getString("last_name"));

            String birthDateStr = rs.getString("birth_date");
            if (birthDateStr != null) {
                member.setBirthDate(LocalDate.parse(birthDateStr));
            }

            member.setGender(Gender.valueOf(rs.getString("gender")));
            member.setAddress(rs.getString("address"));
            member.setProfession(rs.getString("profession"));

            Object phoneNumber = rs.getObject("phone_number");
            member.setPhoneNumber(phoneNumber != null ? phoneNumber.toString() : null);

            member.setEmail(rs.getString("email"));
            member.setOccupation(MemberOccupation.valueOf(rs.getString("occupation")));

            return member;
        } catch (SQLException e) {
            throw new Exception("Database error while fetching member: " + e.getMessage(), e);
        }
    }

    public Collectivity updateCollectivity(String collectivityId, UpdateCollectivityDTO updateDTO) throws Exception {
        String checkSql = "SELECT unique_number, unique_name FROM collectivities WHERE id = ?::uuid";

        try (Connection conn = dataSourceConfig.dataSource().getConnection()) {
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setString(1, collectivityId);
                ResultSet rs = psCheck.executeQuery();

                if (!rs.next()) {
                    throw new CollectivityNotFoundException(collectivityId);
                }

                String updateSql = """
                            UPDATE collectivities
                            SET location = ?, federation_approval = ?, updated_at = NOW()
                            WHERE id = ?::uuid
                            RETURNING id, location, federation_approval, unique_number, unique_name
                        """;

                try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                    psUpdate.setString(1, updateDTO.getLocation());
                    psUpdate.setBoolean(2, updateDTO.getFederationApproval());
                    psUpdate.setString(3, collectivityId);

                    ResultSet rsUpdate = psUpdate.executeQuery();

                    if (!rsUpdate.next()) {
                        throw new SQLException("Failed to update collectivity");
                    }

                    if (updateDTO.getStructure() != null) {
                        String structureSql = """
                                    UPDATE collectivity_structure
                                    SET president_id = ?::uuid, vice_president_id = ?::uuid,
                                        treasurer_id = ?::uuid, secretary_id = ?::uuid
                                    WHERE collectivity_id = ?::uuid
                                """;

                        try (PreparedStatement psStructure = conn.prepareStatement(structureSql)) {
                            psStructure.setString(1, updateDTO.getStructure().getPresident());
                            psStructure.setString(2, updateDTO.getStructure().getVicePresident());
                            psStructure.setString(3, updateDTO.getStructure().getTreasurer());
                            psStructure.setString(4, updateDTO.getStructure().getSecretary());
                            psStructure.setString(5, collectivityId);
                            psStructure.executeUpdate();
                        }
                    }

                    if (updateDTO.getMembers() != null) {
                        String deleteMembersSql = "DELETE FROM collectivity_members WHERE collectivity_id = ?::uuid";
                        try (PreparedStatement psDelete = conn.prepareStatement(deleteMembersSql)) {
                            psDelete.setString(1, collectivityId);
                            psDelete.executeUpdate();
                        }

                        String insertMemberSql = "INSERT INTO collectivity_members (collectivity_id, member_id) VALUES (?::uuid, ?::uuid)";
                        for (String memberId : updateDTO.getMembers()) {
                            try (PreparedStatement psInsert = conn.prepareStatement(insertMemberSql)) {
                                psInsert.setString(1, collectivityId);
                                psInsert.setString(2, memberId);
                                psInsert.executeUpdate();
                            }
                        }
                    }

                    Collectivity collectivity = new Collectivity();
                    collectivity.setId(rsUpdate.getString("id"));
                    collectivity.setLocation(rsUpdate.getString("location"));
                    collectivity.setFederationApproval(rsUpdate.getBoolean("federation_approval"));
                    collectivity.setNumber(rsUpdate.getString("unique_number"));
                    collectivity.setName(rsUpdate.getString("unique_name"));

                    return collectivity;
                }
            }
        }
    }

    public boolean existsById(UUID id) {
        String sql = "select 1 from collectivities where id = ?";
        try (Connection conn = dataSourceConfig.dataSource().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (
                    ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Collectivity findCollectivityById(String id) {
        String sql = """
                    SELECT c.id, c.location, c.federation_approval, c.name, c.number,
                           cs.president_id, cs.vice_president_id, cs.treasurer_id, cs.secretary_id
                    FROM collectivities c
                    LEFT JOIN collectivity_structure cs ON cs.collectivity_id = c.id
                    WHERE c.id = ?::uuid
                """;

        String membersSql = """
                    SELECT m.id, m.first_name, m.last_name, m.birth_date, m.gender,
                           m.address, m.profession, m.phone_number, m.email, m.occupation
                    FROM members m
                    JOIN collectivity_members cm ON cm.member_id = m.id
                    WHERE cm.collectivity_id = ?::uuid
                """;

        try (Connection conn = dataSourceConfig.dataSource().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                PreparedStatement psMembers = conn.prepareStatement(membersSql)) {

            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                throw new CollectivityNotFoundException(id);
            }

            Collectivity collectivity = new Collectivity();
            collectivity.setId(rs.getString("id"));
            collectivity.setLocation(rs.getString("location"));
            collectivity.setFederationApproval(rs.getBoolean("federation_approval"));
            collectivity.setName(rs.getString("name"));
            collectivity.setNumber(rs.getString("number"));

            CollectivityStructure structure = new CollectivityStructure();
            structure.setPresident(getMemberIfExists(conn, rs.getString("president_id")));
            structure.setVicePresident(getMemberIfExists(conn, rs.getString("vice_president_id")));
            structure.setTreasurer(getMemberIfExists(conn, rs.getString("treasurer_id")));
            structure.setSecretary(getMemberIfExists(conn, rs.getString("secretary_id")));
            collectivity.setStructure(structure);

            psMembers.setString(1, id);
            ResultSet rsMembers = psMembers.executeQuery();
            List<Member> members = new ArrayList<>();
            while (rsMembers.next()) {
                Member member = new Member();
                member.setId(rsMembers.getString("id"));
                member.setFirstName(rsMembers.getString("first_name"));
                member.setLastName(rsMembers.getString("last_name"));
                member.setBirthDate(LocalDate.parse(rsMembers.getString("birth_date")));
                member.setGender(Gender.valueOf(rsMembers.getString("gender")));
                member.setAddress(rsMembers.getString("address"));
                member.setProfession(rsMembers.getString("profession"));
                member.setPhoneNumber(rsMembers.getString("phone_number"));
                member.setEmail(rsMembers.getString("email"));
                member.setOccupation(MemberOccupation.valueOf(rsMembers.getString("occupation")));
                members.add(member);
            }
            collectivity.setMembers(members);

            return collectivity;

        } catch (Exception e) {
            throw new RuntimeException("Error fetching collectivity: " + e.getMessage(), e);
        }
    }

    public List<FinancialAccount> getFinancialAccountsByCollectivityId(String collectivityId, LocalDate atDate) {
        List<FinancialAccount> accounts = new ArrayList<>();

        String sql = """
                    SELECT id, account_type, amount, holder_name, mobile_banking_service, mobile_number,
                    bank_name, bank_code, bank_branch_code, bank_account_number, bank_account_key
                    FROM financial_accounts
                    WHERE collectivity_id = ?::uuid
                    ORDER BY created_at DESC
                """;

        try (Connection conn = dataSourceConfig.dataSource().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, collectivityId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String accountType = rs.getString("account_type");
                String id = rs.getString("id");
                Double amount = rs.getDouble("amount");

                // If atDate is provided, calculate balance at that date
                if (atDate != null) {
                    amount = calculateBalanceAtDate(conn, id, atDate);
                }

                FinancialAccount account = null;

                if ("CASH".equals(accountType)) {
                    CashAccount cashAccount = new CashAccount();
                    cashAccount.setId(id);
                    cashAccount.setAmount(amount);
                    account = cashAccount;
                } else if ("MOBILE_BANKING".equals(accountType)) {
                    MobileBankingAccount mobileAccount = new MobileBankingAccount();
                    mobileAccount.setId(id);
                    mobileAccount.setAmount(amount);
                    mobileAccount.setHolderName(rs.getString("holder_name"));
                    String serviceName = rs.getString("mobile_banking_service");
                    if (serviceName != null) {
                        mobileAccount.setMobileBankingService(MobileBankingService.valueOf(serviceName));
                    }
                    mobileAccount.setMobileNumber(rs.getString("mobile_number"));
                    account = mobileAccount;
                } else if ("BANK".equals(accountType)) {
                    BankAccount bankAccount = new BankAccount();
                    bankAccount.setId(id);
                    bankAccount.setAmount(amount);
                    bankAccount.setHolderName(rs.getString("holder_name"));
                    String bankName = rs.getString("bank_name");
                    if (bankName != null) {
                        bankAccount.setBankName(Bank.valueOf(bankName));
                    }
                    bankAccount.setBankCode(rs.getInt("bank_code"));
                    bankAccount.setBankBranchCode(rs.getInt("bank_branch_code"));
                    bankAccount.setBankAccountNumber(rs.getInt("bank_account_number"));
                    bankAccount.setBankAccountKey(rs.getInt("bank_account_key"));
                    account = bankAccount;
                }

                if (account != null) {
                    accounts.add(account);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching financial accounts: " + e.getMessage(), e);
        }

        return accounts;
    }

    private Double calculateBalanceAtDate(Connection conn, String accountId, LocalDate atDate) throws SQLException {
        // Start with the initial amount from financial_accounts
        String sqlInitialBalance = """
                    SELECT amount FROM financial_accounts WHERE id = ?::uuid
                """;

        Double balance = 0.0;

        try (PreparedStatement ps = conn.prepareStatement(sqlInitialBalance)) {
            ps.setString(1, accountId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                balance = rs.getDouble("amount");
            }
        }

        // Calculate transactions up to the atDate
        String sqlTransactions = """
                    SELECT COALESCE(SUM(amount), 0) as total
                    FROM collectivity_transactions
                    WHERE account_credited_id = ?::uuid
                    AND creation_date <= ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sqlTransactions)) {
            ps.setString(1, accountId);
            ps.setDate(2, java.sql.Date.valueOf(atDate));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                balance += rs.getDouble("total");
            }
        }

        // Also include member payments
        String sqlMemberPayments = """
                    SELECT COALESCE(SUM(amount), 0) as total
                    FROM member_payments
                    WHERE account_credited_id = ?::uuid
                    AND creation_date <= ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sqlMemberPayments)) {
            ps.setString(1, accountId);
            ps.setDate(2, java.sql.Date.valueOf(atDate));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                balance += rs.getDouble("total");
            }
        }

        return balance;
    }

}