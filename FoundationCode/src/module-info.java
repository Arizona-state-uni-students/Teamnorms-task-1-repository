module FoundationCode {
	requires javafx.controls;
	requires java.sql;
	requires javafx.graphics;
	    
	    opens application to javafx.graphics, javafx.fxml, org.junit.platform.commons;
	    opens databasePart1 to org.junit.platform.commons;
}
