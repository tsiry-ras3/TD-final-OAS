package org.fca_backend.service;

import lombok.AllArgsConstructor;
import org.fca_backend.DTO.CreateMemberDTO;
import org.fca_backend.DTO.CreateMemberPaymentDTO;
import org.fca_backend.entity.Member;
import org.fca_backend.entity.MemberPayment;
import org.fca_backend.exception.BadRequestException;
import org.fca_backend.repository.MemberPaymentRepository;
import org.fca_backend.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class MemberService {
    private MemberRepository memberRepository;
    private MemberPaymentRepository memberPaymentRepository;

    public List<Member> createMember(List<CreateMemberDTO> createMemberDTO) {
        try {
            return memberRepository.addNewListOfMembers(createMemberDTO);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    public List<MemberPayment> createMemberPayments(String memberId, List<CreateMemberPaymentDTO> payments) {
        try {
            return memberPaymentRepository.createMemberPayments(memberId, payments);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }
}