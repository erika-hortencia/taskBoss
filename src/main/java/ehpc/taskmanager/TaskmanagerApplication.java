package ehpc.taskmanager;


import ehpc.taskmanager.persistence.migration.MigrationStrategy;
import ehpc.taskmanager.ui.MainMenu;

import java.sql.SQLException;

import static ehpc.taskmanager.persistence.config.ConnectionConfig.getConnection;

public class TaskmanagerApplication {

	public static void main(String[] args) throws SQLException{
		try(var connection = getConnection()){
			new MigrationStrategy(connection).executeMigration();
		}
		new MainMenu().execute();
	}

}
