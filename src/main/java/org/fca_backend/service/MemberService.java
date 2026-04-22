package org.fca_backend.service;

import lombok.AllArgsConstructor;
import org.fca_backend.DTO.CreateMemberDTO;
import org.fca_backend.entity.Member;
import org.fca_backend.exception.BadRequestException;
import org.fca_backend.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class MemberService {
    private MemberRepository memberRepository;

    public List<Member> createMember(List<CreateMemberDTO> createMemberDTO) {
        try {
            return memberRepository.addNewListOfMembers(createMemberDTO);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }
}