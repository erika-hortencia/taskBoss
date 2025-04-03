package ehpc.taskmanager.ui;

import ehpc.taskmanager.persistence.entity.BoardColumnEntity;
import ehpc.taskmanager.persistence.entity.BoardColumnKindEnum;
import ehpc.taskmanager.persistence.entity.BoardEntity;
import ehpc.taskmanager.service.BoardQueryService;
import ehpc.taskmanager.service.BoardService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static ehpc.taskmanager.persistence.config.ConnectionConfig.getConnection;
import static ehpc.taskmanager.persistence.entity.BoardColumnKindEnum.*;

public class MainMenu {
    
    private final Scanner scanner = new Scanner(System.in);
    
    public void execute() throws SQLException {
        System.out.println("Welcome to board manager, pick the desired option");
        var option = -1;
        while (true){
            System.out.println("1 - create new board");
            System.out.println("2 - select existing board");
            System.out.println("3 - delete board");
            System.out.println("4 - quit");

            option = scanner.nextInt();
            
            switch (option){
                case 1 -> createBoard();
                case 2 -> selectBoard();
                case 3 -> deleteBoard();
                case 4 -> System.exit(0);
                default -> System.out.println("Invalid option, choose an option from menu");
            }
        }
    }

    private void deleteBoard()  throws SQLException {
        System.out.println("Inform board id for deletion");
        var id = scanner.nextLong();
        try(var connection = getConnection()){
            var service = new BoardService(connection);
            if (service.delete(id)){
                System.out.printf("Board %s was excluded\n", id);
            } else {
                System.out.printf("Board %s not foun\n", id);
            }
        }
    }

    private void selectBoard() throws SQLException{
        System.out.println("Inform board if for selecion");
        var id = scanner.nextLong();
        try(var connection = getConnection()){
            var queryService = new BoardQueryService(connection);
            var optional = queryService.findById(id);
            optional.ifPresentOrElse(
                    b -> new BoardMenu(b).execute(),
                    () -> System.out.printf("Board %s not found\n", id)
            );
        }
    }

    private void createBoard() throws SQLException{
        var entity = new BoardEntity();
        System.out.println("Inform board name");
        entity.setName(scanner.next());

        System.out.println("Inform how many columns beyond the standard 3 your board whill have");
        var additionalColumns = scanner.nextInt();

        List<BoardColumnEntity> columns = new ArrayList<>();

        System.out.println("Inform board's initial column");
        var initialColumnName = scanner.next();
        var initialColumn = createColumn(initialColumnName, INITIAL, 0);
        columns.add(initialColumn);

        for (int i = 0; i < additionalColumns; i++) {
            System.out.println("Inform the column name of the pending task on the board");
            var pendingColumnName = scanner.next();
            var pendingColumn = createColumn(pendingColumnName, PENDING, i + 1);
            columns.add(pendingColumn);
        }

        System.out.println("Inform the name of the final column");
        var finalColumnName = scanner.next();
        var finalColumn = createColumn(finalColumnName, FINAL, additionalColumns + 1);
        columns.add(finalColumn);

        System.out.println("Inform the name of board's cancel column");
        var cancelColumnName = scanner.next();
        var cancelColumn = createColumn(cancelColumnName, CANCEL, additionalColumns + 2);
        columns.add(cancelColumn);

        entity.setBoardColumns(columns);
        try(var connection = getConnection()){
            var service = new BoardService(connection);
            service.insert(entity);
        }
    }

    private BoardColumnEntity createColumn(final String name, final BoardColumnKindEnum kind, final int order){
        var boardColumn = new BoardColumnEntity();
        boardColumn.setName(name);
        boardColumn.setKind(kind);
        boardColumn.setOrder(order);
        return boardColumn;
    }
}
