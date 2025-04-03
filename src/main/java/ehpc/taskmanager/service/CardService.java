package ehpc.taskmanager.service;

import ehpc.taskmanager.dto.BoardColumnInfoDTO;
import ehpc.taskmanager.exception.CardBlockedException;
import ehpc.taskmanager.exception.CardFinishedException;
import ehpc.taskmanager.exception.EntityNotFoundException;
import ehpc.taskmanager.persistence.dao.BlockDAO;
import ehpc.taskmanager.persistence.dao.CardDAO;
import ehpc.taskmanager.persistence.entity.CardEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static ehpc.taskmanager.persistence.entity.BoardColumnKindEnum.FINAL;
import static ehpc.taskmanager.persistence.entity.BoardColumnKindEnum.CANCEL;

@AllArgsConstructor
public class CardService {

    private final Connection connection;

    public CardEntity create(final CardEntity entity) throws SQLException {
        try {
            var dao = new CardDAO(connection);
            dao.insert(entity);
            connection.commit();
            return entity;
        } catch (SQLException ex){
            connection.rollback();
            throw ex;
        }
    }

    public void moveToNextColumn(final Long cardId, final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException{
        try{
            var dao = new CardDAO(connection);
            var optional = dao.findById(cardId);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundException("Card with id %s not found".formatted(cardId))
            );
            if (dto.blocked()){
                var message = "Card %s is blocked, it must be unblocked to be moved".formatted(cardId);
                throw new CardBlockedException(message);
            }
            var currentColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.id().equals(dto.columnId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Card belongs to another board"));
            if (currentColumn.kind().equals(FINAL)){
                throw new CardFinishedException("Card has been concluded");
            }
            var nextColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.order() == currentColumn.order() + 1)
                    .findFirst().orElseThrow(() -> new IllegalStateException("Card is cancelled"));
            dao.moveToColumn(nextColumn.id(), cardId);
            connection.commit();
        }catch (SQLException ex){
            connection.rollback();
            throw ex;
        }
    }

    public void cancel(final Long cardId, final Long cancelColumnId ,
                       final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException{
        try{
            var dao = new CardDAO(connection);
            var optional = dao.findById(cardId);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundException("Card %s not found".formatted(cardId))
            );
            if (dto.blocked()){
                var message = "Card %s is blocked, it must be unblocked to be moved".formatted(cardId);
                throw new CardBlockedException(message);
            }
            var currentColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.id().equals(dto.columnId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Card belongs to another board"));
            if (currentColumn.kind().equals(FINAL)){
                throw new CardFinishedException("Card has been concluded");
            }
            boardColumnsInfo.stream()
                    .filter(bc -> bc.order() == currentColumn.order() + 1)
                    .findFirst().orElseThrow(() -> new IllegalStateException("Card is cancelled"));
            dao.moveToColumn(cancelColumnId, cardId);
            connection.commit();
        }catch (SQLException ex){
            connection.rollback();
            throw ex;
        }
    }

    public void block(final Long id, final String reason, final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException {
        try{
            var dao = new CardDAO(connection);
            var optional = dao.findById(id);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundException("Card %s not found".formatted(id))
            );
            if (dto.blocked()){
                var message = "Card %s is already blocked".formatted(id);
                throw new CardBlockedException(message);
            }
            var currentColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.id().equals(dto.columnId()))
                    .findFirst()
                    .orElseThrow();
            if (currentColumn.kind().equals(FINAL) || currentColumn.kind().equals(CANCEL)){
                var message = "Card is in column of type %s and cannot be blocked"
                        .formatted(currentColumn.kind());
                throw new IllegalStateException(message);
            }
            var blockDAO = new BlockDAO(connection);
            blockDAO.block(reason, id);
            connection.commit();
        }catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }
    }

    public void unblock(final Long id, final String reason) throws SQLException {
        try{
            var dao = new CardDAO(connection);
            var optional = dao.findById(id);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundException("Card %s not found".formatted(id))
            );
            if (!dto.blocked()){
                var message = "Card %s is not blocked".formatted(id);
                throw new CardBlockedException(message);
            }
            var blockDAO = new BlockDAO(connection);
            blockDAO.unblock(reason, id);
            connection.commit();
        }catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }
    }

}
