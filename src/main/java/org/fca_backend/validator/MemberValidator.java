package org.fca_backend.validator;

import org.fca_backend.DTO.CreateMemberDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MemberValidator {

    public void validateCreateMember(List<CreateMemberDTO> members) {
        if (members == null || members.isEmpty()) {
            throw new IllegalArgumentException("Members list cannot be null or empty");
        }

        for (CreateMemberDTO member : members) {
            validateMember(member);
        }
    }

    private void validateMember(CreateMemberDTO member) {
        if (member.getFirstName() == null || member.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }

        if (member.getLastName() == null || member.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }

        if (member.getBirthDate() == null) {
            throw new IllegalArgumentException("Birth date is required");
        }

        if (member.getGender() == null) {
            throw new IllegalArgumentException("Gender is required");
        }

        if (member.getOccupation() == null) {
            throw new IllegalArgumentException("Occupation is required");
        }

        if (member.getRegistrationFeePaid() == null || !member.getRegistrationFeePaid()) {
            throw new IllegalArgumentException("Registration fee must be paid");
        }

        if (member.getMembershipDuesPaid() == null || !member.getMembershipDuesPaid()) {
            throw new IllegalArgumentException("Membership dues must be paid");
        }

        if (member.getEmail() != null && !member.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (member.getReferees() != null && !member.getReferees().isEmpty()) {
            validateReferees(member.getReferees());
        }
    }

    private void validateReferees(List<String> referees) {
        for (String refereeId : referees) {
            if (refereeId == null || refereeId.trim().isEmpty()) {
                throw new IllegalArgumentException("Referee ID cannot be null or empty");
            }
        }
    }
}