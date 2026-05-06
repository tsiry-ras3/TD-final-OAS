package org.fca_backend.entity;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CollectivityOverall {
   private CollectivityInfo collectivityInfo;
   private Integer newMembersNumber;
   private BigDecimal  overallMemberCurrentDuePercentage; 
}
