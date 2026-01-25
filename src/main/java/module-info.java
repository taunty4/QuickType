module org.example.quicktype {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens me.taunty.quicktype to javafx.fxml;
    exports me.taunty.quicktype;
}