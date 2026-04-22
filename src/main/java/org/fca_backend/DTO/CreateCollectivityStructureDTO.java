package org.fca_backend.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateCollectivityStructureDTO {
    private String president;
    private String vicePresident;
    private String treasurer;
    private String secretary;
}
