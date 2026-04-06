module sistema {
	requires javafx.controls;
	requires javafx.graphics;
	requires javafx.fxml;
	requires java.sql;
	
	opens application to javafx.base, javafx.graphics, javafx.fxml;
	opens application.view to javafx.base,javafx.graphics, javafx.fxml;
	opens application.controller to javafx.base,javafx.graphics, javafx.fxml;
	opens application.model to javafx.base;
}
