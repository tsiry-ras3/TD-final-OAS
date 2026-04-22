package org.fca_backend.controler;

import lombok.AllArgsConstructor;
import org.fca_backend.DTO.CreateMemberDTO;
import org.fca_backend.DTO.CreateMemberPaymentDTO;
import org.fca_backend.entity.Member;
import org.fca_backend.entity.MemberPayment;
import org.fca_backend.exception.BadRequestException;
import org.fca_backend.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
public class MemberController {
    private MemberService memberService;

    @PostMapping("/members")
    public ResponseEntity<?> createMembers(@RequestBody List<CreateMemberDTO> createMemberDTO) {
        try {
            List<Member> members = memberService.createMember(createMemberDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(members);
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/members/{id}/payments")
    public ResponseEntity<?> createMemberPayments(
            @PathVariable String id,
            @RequestBody List<CreateMemberPaymentDTO> payments) {
        try {
            List<MemberPayment> result = memberService.createMemberPayments(id, payments);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}