package ehpc.taskmanager.dto;

import ehpc.taskmanager.persistence.entity.BoardColumnKindEnum;

public record BoardColumnDTO (Long id,
                             String name,
                             BoardColumnKindEnum kind,
                             int cardsAmount){
}
