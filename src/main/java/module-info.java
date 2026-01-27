module me.taunty.quicktype {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;


    opens me.taunty.quicktype to javafx.fxml;
    exports me.taunty.quicktype;
}