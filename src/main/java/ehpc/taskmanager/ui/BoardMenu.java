package ehpc.taskmanager.ui;

import ehpc.taskmanager.service.BoardColumnQueryService;
import ehpc.taskmanager.dto.BoardColumnInfoDTO;
import ehpc.taskmanager.persistence.entity.BoardColumnEntity;
import ehpc.taskmanager.persistence.entity.BoardEntity;
import ehpc.taskmanager.persistence.entity.CardEntity;
import ehpc.taskmanager.service.BoardQueryService;
import ehpc.taskmanager.service.CardQueryService;
import ehpc.taskmanager.service.CardService;
import lombok.AllArgsConstructor;

import static ehpc.taskmanager.persistence.config.ConnectionConfig.getConnection;

import java.sql.SQLException;
import java.util.Scanner;

@AllArgsConstructor
public class BoardMenu {

    private final Scanner scanner = new Scanner(System.in).useDelimiter("\n");

    private final BoardEntity entity;

    public void execute() {
        try {
            System.out.printf("Welcome to board %s, choose an option\n", entity.getId());
            var option = -1;
            while (option != 9) {
                System.out.println("1 - Create card");
                System.out.println("2 - Move card");
                System.out.println("3 - Block card");
                System.out.println("4 - Unblock card");
                System.out.println("5 - Cancel card");
                System.out.println("6 - See board");
                System.out.println("7 - See column with cards");
                System.out.println("8 - See card");
                System.out.println("9 - Return to previous menu");
                System.out.println("10 - Exit");
                option = scanner.nextInt();
                switch (option) {
                    case 1 -> createCard();
                    case 2 -> moveCardToNextColumn();
                    case 3 -> blockCard();
                    case 4 -> unblockCard();
                    case 5 -> cancelCard();
                    case 6 -> showBoard();
                    case 7 -> showColumn();
                    case 8 -> showCard();
                    case 9 -> System.out.println("Back to previous menu");
                    case 10 -> System.exit(0);
                    default -> System.out.println("Invalid option");
                }
            }
        }catch (SQLException ex){
            ex.printStackTrace();
            System.exit(0);
        }
    }

    private void createCard() throws SQLException{
        var card = new CardEntity();
        System.out.println("Insert card title");
        card.setTitle(scanner.next());
        System.out.println("Insert card description");
        card.setDescription(scanner.next());
        card.setBoardColumn(entity.getInitialColumn());
        try(var connection = getConnection()){
            new CardService(connection).create(card);
        }
    }

    private void moveCardToNextColumn() throws SQLException {
        System.out.println("Insert id of card to be moved to the next column");
        var cardId = scanner.nextLong();
        var boardColumnsInfo = entity.getBoardColumns().stream()
                .map(bc -> new BoardColumnInfoDTO(bc.getId(), bc.getOrder(), bc.getKind()))
                .toList();
        try(var connection = getConnection()){
            new CardService(connection).moveToNextColumn(cardId, boardColumnsInfo);
        } catch (RuntimeException ex){
            System.out.println(ex.getMessage());
        }
    }

    private void blockCard() throws SQLException {
        System.out.println("Insert id of card to be blocked");
        var cardId = scanner.nextLong();
        System.out.println("Inform block motive");
        var reason = scanner.next();
        var boardColumnsInfo = entity.getBoardColumns().stream()
                .map(bc -> new BoardColumnInfoDTO(bc.getId(), bc.getOrder(), bc.getKind()))
                .toList();
        try(var connection = getConnection()){
            new CardService(connection).block(cardId, reason, boardColumnsInfo);
        } catch (RuntimeException ex){
            System.out.println(ex.getMessage());
        }
    }

    private void unblockCard() throws SQLException {
        System.out.println("Insert id of card to be unblocked");
        var cardId = scanner.nextLong();
        System.out.println("Inform unblock motive");
        var reason = scanner.next();
        try(var connection = getConnection()){
            new CardService(connection).unblock(cardId, reason);
        } catch (RuntimeException ex){
            System.out.println(ex.getMessage());
        }
    }

    private void cancelCard() throws SQLException {
        System.out.println("Inform id of card to be moved to cancel column");
        var cardId = scanner.nextLong();
        var cancelColumn = entity.getCancelColumn();
        var boardColumnsInfo = entity.getBoardColumns().stream()
                .map(bc -> new BoardColumnInfoDTO(bc.getId(), bc.getOrder(), bc.getKind()))
                .toList();
        try(var connection = getConnection()){
            new CardService(connection).cancel(cardId, cancelColumn.getId(), boardColumnsInfo);
        } catch (RuntimeException ex){
            System.out.println(ex.getMessage());
        }
    }

    private void showBoard() throws SQLException {
        try(var connection = getConnection()){
            var optional = new BoardQueryService(connection).showBoardDetails(entity.getId());
            optional.ifPresent(b -> {
                System.out.printf("Board [%s,%s]\n", b.id(), b.name());
                b.columns().forEach(c ->
                        System.out.printf("Column [%s] kind: [%s] has %s cards\n", c.name(), c.kind(), c.cardsAmount())
                );
            });
        }
    }

    private void showColumn() throws SQLException {
        var columnsIds = entity.getBoardColumns().stream().map(BoardColumnEntity::getId).toList();
        var selectedColumnId = -1L;
        while (!columnsIds.contains(selectedColumnId)){
            System.out.printf("Choose a column from board %s by id\n", entity.getName());
            entity.getBoardColumns().forEach(c -> System.out.printf("%s - %s [%s]\n", c.getId(), c.getName(), c.getKind()));
            selectedColumnId = scanner.nextLong();
        }
        try(var connection = getConnection()){
            var column = new BoardColumnQueryService(connection).findById(selectedColumnId);
            column.ifPresent(co -> {
                System.out.printf("Column %s kind %s\n", co.getName(), co.getKind());
                co.getCards().forEach(ca -> System.out.printf("Card %s - %s\nDescription: %s",
                        ca.getId(), ca.getTitle(), ca.getDescription()));
            });
        }
    }

    private void showCard() throws SQLException {
        System.out.println("Insert id of card to be viewd");
        var selectedCardId = scanner.nextLong();
        try(var connection  = getConnection()){
            new CardQueryService(connection).findById(selectedCardId)
                    .ifPresentOrElse(
                            c -> {
                                System.out.printf("Card %s - %s.\n", c.id(), c.title());
                                System.out.printf("Description: %s\n", c.description());
                                System.out.println(c.blocked() ?
                                        "Is blocked. Motive: " + c.blockReason() :
                                        "Is not blocked");
                                System.out.printf("Has been blocked %s times\n", c.blocksAmount());
                                System.out.printf("Is in column %s  - %s at the moment\n", c.columnId(), c.columnName());
                            },
                            () -> System.out.printf("Does not exist in board %s\n", selectedCardId));
        }
    }
}
