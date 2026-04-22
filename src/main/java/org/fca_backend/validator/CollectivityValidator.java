package org.fca_backend.validator;

import org.fca_backend.DTO.CreateCollectivityDTO;
import org.fca_backend.DTO.CreateCollectivityStructureDTO;
import org.fca_backend.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class CollectivityValidator {

    public void validateCreateCollectivity(List<CreateCollectivityDTO> collectivityDTO) throws BadRequestException {
        for (CreateCollectivityDTO createCollectivityDTO : collectivityDTO) {

            if (createCollectivityDTO.getFederationApproval() == null || !createCollectivityDTO.getFederationApproval() || createCollectivityDTO.getStructure() == null) {
                throw new BadRequestException("Collectivity without federation approval or structure missing");
            }
        validateStructure(createCollectivityDTO.getStructure());
        }
    }


    private void validateStructure(CreateCollectivityStructureDTO structure) throws BadRequestException {
        if (structure.getPresident() == null || structure.getPresident().trim().isEmpty()) {
            throw new BadRequestException("president is null or empty");
        }

        if (structure.getVicePresident() == null || structure.getVicePresident().trim().isEmpty()) {
            throw new BadRequestException("vice president is null or empty");
        }

        if (structure.getTreasurer() == null || structure.getTreasurer().trim().isEmpty()) {
            throw new BadRequestException("treasurer is null or empty");
        }

        if (structure.getSecretary() == null || structure.getSecretary().trim().isEmpty()) {
            throw new BadRequestException("secretary is null or empty");
        }
    }
}
