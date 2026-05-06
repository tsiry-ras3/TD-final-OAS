package org.fca_backend.DTO;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CollectivityOverallDTO {
   private CollectivityInfoDTO collectivityInfo;
   private Integer newMembersNumber;
   private BigDecimal  overallMemberCurrentDuePercentage; 
}
