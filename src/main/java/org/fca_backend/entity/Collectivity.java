package org.fca_backend.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Collectivity {
    private String id;
    private String location;
    private Boolean federationApproval;
    private CollectivityStructure structure;
    private List<Member> members;
    private String uniqueNumber;  // Nouveau champ
    private String uniqueName;     // Nouveau champ
}