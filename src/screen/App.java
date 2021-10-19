package screen;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.net.URL;

public class App extends Application {

    private FXMLLoader fxmlLoader;
    private Screen screen;

    public void start(Stage stage) throws Exception {
        URL location = this.getClass().getResource("Display.fxml");
        fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        BorderPane pane = fxmlLoader.load(location.openStream());
        screen = (Screen) fxmlLoader.getController();

        Scene scene = new Scene(pane);
        stage.setTitle("CHIP-8 Emulator");
        stage.setScene(scene);
        stage.show();

        screen.init();
    }

    public void stop() {
        screen.stopPool();
    }

    public static void main(String[] args) {
        App.launch(args);
    }
}
