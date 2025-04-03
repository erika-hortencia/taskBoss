package ehpc.taskmanager.ui;

import java.util.Scanner;

public class MainMenu {
    
    private final Scanner scanner = new Scanner(System.in);
    
    public void execute(){
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

    private void deleteBoard() {
    }

    private void selectBoard() {
    }

    private void createBoard() {
    }
}
