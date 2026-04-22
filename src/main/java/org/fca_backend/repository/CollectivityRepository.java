package org.fca_backend.repository;

import lombok.AllArgsConstructor;
import org.fca_backend.DTO.CreateCollectivityDTO;
import org.fca_backend.DTO.CreateCollectivityStructureDTO;
import org.fca_backend.config.DataSourceConfig;
import org.fca_backend.entity.*;
import org.fca_backend.validator.CollectivityValidator;
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
}