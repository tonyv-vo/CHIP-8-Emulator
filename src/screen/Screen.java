package screen;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.application.Platform;
import javafx.event.ActionEvent;

import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Screen {
    private Map<String, Integer> buttonMap = new HashMap<String, Integer>();
    @FXML
    private BorderPane pane;
    @FXML
    private Canvas canvas;
    private Stage stage;
    private GraphicsContext gContext;
    private double pixelScale = 8;
    private Chip8CPU CPU;
    private FileChooser fileChooser = new FileChooser();
    private String filePath;
    private ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(2);
    private ScheduledFuture<?> cpuThread;
    private ScheduledFuture<?> displayThread;

    public Screen() {
        buttonMap.put("1", 1);
        buttonMap.put("2", 2);
        buttonMap.put("3", 3);
        buttonMap.put("4", 12);
        buttonMap.put("q", 4);
        buttonMap.put("w", 5);
        buttonMap.put("e", 6);
        buttonMap.put("r", 13);
        buttonMap.put("a", 7);
        buttonMap.put("s", 8);
        buttonMap.put("d", 9);
        buttonMap.put("f", 14);
        buttonMap.put("z", 10);
        buttonMap.put("x", 0);
        buttonMap.put("c", 11);
        buttonMap.put("v", 15);
    }

    @FXML
    private void handleLoad() {
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            filePath = file.toString();
            restartCPU();
        }
    }

    @FXML
    private void handleRestartAction(ActionEvent event) {
        restartCPU();
    }

    @FXML
    private void handleKeyPressed(KeyEvent event) {
        System.out.println("Pressed: " + event.getText());

        if (buttonMap.containsKey(event.getText())) {
            CPU.setKeyAtIndex(buttonMap.get(event.getText()), 1);
        }
    }

    @FXML
    private void handleKeyReleased(KeyEvent event) {
        System.out.println("Released: " + event.getText());

        if (buttonMap.containsKey(event.getText())) {
            CPU.setKeyAtIndex(buttonMap.get(event.getText()), 0);
        }
    }

    public void init() {
        stage = (Stage) canvas.getScene().getWindow();
        canvas.setFocusTraversable(true);
        pane.setStyle("-fx-background-color: black");
        pane.setCenter(canvas);
        gContext = canvas.getGraphicsContext2D();
        CPU = new Chip8CPU();
        CPU.initialize();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
    }

    public void updateDisplay() {
        int[][] gfx = CPU.getGFX();
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 64; x++) {
                if (gfx[x][y] == 1) {
                    gContext.setFill(Color.WHITE);
                } else {
                    gContext.setFill(Color.BLACK);
                }
                gContext.fillRect(x*pixelScale, y*pixelScale, pixelScale, pixelScale);
            }
        }
    }

    public void startThreads() {
        cpuThread = threadPool.scheduleWithFixedDelay(() -> {
            CPU.emulateCPUCycle();
        }, 2, 2, TimeUnit.MILLISECONDS);

        displayThread = threadPool.scheduleWithFixedDelay(() -> {
            CPU.updateTimers();
            if (CPU.isVF()) {
                Platform.runLater(() -> {
                    updateDisplay();
                    CPU.setVF(false);
                });
            }
        }, 17, 17, TimeUnit.MILLISECONDS);
    }

    public void stopThreads() {
        if (cpuThread != null) {
            cpuThread.cancel(true);
            displayThread.cancel(true);
        }
    }

    public void stopPool() {
        threadPool.shutdownNow();
    }

    public void restartCPU() {
        if (filePath != null) {
            stopThreads();
            CPU = new Chip8CPU();
            CPU.initialize();
            CPU.loadROM(filePath);
            startThreads();
        }
    }
}
