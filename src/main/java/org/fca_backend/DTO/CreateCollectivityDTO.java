package org.fca_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateCollectivityDTO {
    private String location;
    private List<String> members;
    private Boolean federationApproval;
    private CreateCollectivityStructureDTO structure;
    private String uniqueNumber;  // Nouveau champ
    private String uniqueName;     // Nouveau champ
}