package org.fca_backend.DTO;


import org.fca_backend.entity.MemberOccupation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MemberDescriptionDto {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private MemberOccupation occupation;
}
