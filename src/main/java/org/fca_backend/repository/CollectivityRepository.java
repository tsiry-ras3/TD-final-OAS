package org.fca_backend.repository;

import lombok.AllArgsConstructor;
import org.fca_backend.DTO.CreateCollectivityDTO;
import org.fca_backend.DTO.CreateCollectivityStructureDTO;
import org.fca_backend.DTO.UpdateCollectivityDTO;
import org.fca_backend.config.DataSourceConfig;
import org.fca_backend.entity.*;
import org.fca_backend.validator.CollectivityValidator;
import org.fca_backend.validator.MemberValidator;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Service
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
                 PreparedStatement psStructure = conn.prepareStatement("""
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
                    throw new Exception("Collectivity not found with ID: " + collectivityId);
                }

                String currentUniqueNumber = rs.getString("unique_number");
                String currentUniqueName = rs.getString("unique_name");

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

                    // Mettre à jour la structure
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

                    // Mettre à jour les membres
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

                    // Construire l'objet de réponse
                    Collectivity collectivity = new Collectivity();
                    collectivity.setId(rsUpdate.getString("id"));
                    collectivity.setLocation(rsUpdate.getString("location"));
                    collectivity.setFederationApproval(rsUpdate.getBoolean("federation_approval"));
                    collectivity.setUniqueNumber(rsUpdate.getString("unique_number"));
                    collectivity.setUniqueName(rsUpdate.getString("unique_name"));

                    return collectivity;
                }
            }
        }
    }
}