module com.vityazev_egor {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.github.kwhat.jnativehook;
    requires com.sun.jna.platform;
    requires jna;

    opens com.vityazev_egor to javafx.fxml;
    exports com.vityazev_egor;
}
