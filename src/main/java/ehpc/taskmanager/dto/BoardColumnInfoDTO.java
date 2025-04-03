package ehpc.taskmanager.dto;
import ehpc.taskmanager.persistence.entity.BoardColumnKindEnum;

public record BoardColumnInfoDTO (Long id, int order, BoardColumnKindEnum kind){
}
