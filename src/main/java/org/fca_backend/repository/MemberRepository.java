package org.fca_backend.repository;

import lombok.AllArgsConstructor;
import org.fca_backend.DTO.CreateMemberDTO;
import org.fca_backend.config.DataSourceConfig;
import org.fca_backend.entity.*;
import org.fca_backend.validator.MemberValidator;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Service
public class MemberRepository {
    private MemberValidator memberValidator;
    private DataSourceConfig dataSourceConfig;

    public List<Member> addNewListOfMembers(List<CreateMemberDTO> members) {
        memberValidator.validateCreateMember(members);

        List<Member> newMembers = new ArrayList<>();

        try (Connection conn = dataSourceConfig.dataSource().getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psMember = conn.prepareStatement("""
                INSERT INTO members (first_name, last_name, birth_date, gender, address, 
                                   profession, phone_number, email, occupation, 
                                   registration_fee_paid, membership_dues_paid, collectivity_id)
                VALUES (?, ?, ?, ?::gender, ?, ?, ?::bigint, ?, ?::member_occupation, ?, ?, ?::uuid)
                RETURNING id, first_name, last_name, birth_date, gender, address, 
                          profession, phone_number, email, occupation
            """);
                 PreparedStatement psReferees = conn.prepareStatement("""
                INSERT INTO member_referees (member_id, referee_id)
                VALUES (?::uuid, ?::uuid)
                ON CONFLICT (member_id, referee_id) DO NOTHING
            """);
                 PreparedStatement psCheckMemberExists = conn.prepareStatement("""
                SELECT id, first_name, last_name, birth_date, gender, address, 
                       profession, phone_number, email, occupation
                FROM members WHERE id = ?::uuid
            """);
                 PreparedStatement psCheckCollectivityExists = conn.prepareStatement("""
                SELECT id FROM collectivities WHERE id = ?::uuid
            """)) {

                for (CreateMemberDTO memberDTO : members) {
                    psMember.setString(1, memberDTO.getFirstName());
                    psMember.setString(2, memberDTO.getLastName());
                    psMember.setDate(3, Date.valueOf(memberDTO.getBirthDate()));
                    psMember.setString(4, memberDTO.getGender().name());
                    psMember.setString(5, memberDTO.getAddress());
                    psMember.setString(6, memberDTO.getProfession());

                    String phoneNumber = memberDTO.getPhoneNumber();
                    if (phoneNumber != null && !phoneNumber.isEmpty()) {
                        psMember.setLong(7, Long.parseLong(phoneNumber));
                    } else {
                        psMember.setNull(7, Types.BIGINT);
                    }

                    psMember.setString(8, memberDTO.getEmail());
                    psMember.setString(9, memberDTO.getOccupation().name());
                    psMember.setBoolean(10, memberDTO.getRegistrationFeePaid());
                    psMember.setBoolean(11, memberDTO.getMembershipDuesPaid());

                    if (memberDTO.getCollectivityIdentifier() != null && !memberDTO.getCollectivityIdentifier().isEmpty()) {
                        psMember.setString(12, memberDTO.getCollectivityIdentifier());
                    } else {
                        psMember.setNull(12, Types.OTHER);
                    }

                    ResultSet rsMember = psMember.executeQuery();

                    if (!rsMember.next()) {
                        throw new SQLException("Failed to insert member");
                    }

                    String generatedId = rsMember.getString("id");
                    String firstName = rsMember.getString("first_name");
                    String lastName = rsMember.getString("last_name");
                    LocalDate birthDate = rsMember.getDate("birth_date").toLocalDate();
                    Gender gender = Gender.valueOf(rsMember.getString("gender"));
                    String address = rsMember.getString("address");
                    String profession = rsMember.getString("profession");
                    String phoneNumberFromDb = rsMember.getString("phone_number");
                    String email = rsMember.getString("email");
                    MemberOccupation occupation = MemberOccupation.valueOf(rsMember.getString("occupation"));

                    List<Member> refereesList = new ArrayList<>();
                    if (memberDTO.getReferees() != null) {
                        for (String refereeId : memberDTO.getReferees()) {
                            Member referee = getMemberIfExists(conn, psCheckMemberExists, refereeId);
                            refereesList.add(referee);

                            psReferees.setString(1, generatedId);
                            psReferees.setString(2, referee.getId());
                            psReferees.executeUpdate();
                        }
                    }

                    Member member = new Member();
                    member.setId(generatedId);
                    member.setFirstName(firstName);
                    member.setLastName(lastName);
                    member.setBirthDate(birthDate);
                    member.setGender(gender);
                    member.setAddress(address);
                    member.setProfession(profession);
                    member.setPhoneNumber(phoneNumberFromDb);
                    member.setEmail(email);
                    member.setOccupation(occupation);
                    member.setReferees(refereesList);

                    newMembers.add(member);
                }

                conn.commit();
                return newMembers;

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

    private Member getMemberIfExists(Connection conn, PreparedStatement ps, String memberId) throws Exception {
        if (memberId == null || memberId.isEmpty()) {
            throw new Exception("Member ID cannot be null or empty");
        }

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
    }
}