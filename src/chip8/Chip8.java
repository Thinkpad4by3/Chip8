/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chip8;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 *
 * @author Jasoni7
 */
public class Chip8 extends Application {
    public static WritableImage displayImage = new WritableImage(640,320);
    public static int[][] displayMemory = new int[64][32];
    @Override
    public void start(Stage primaryStage) throws IOException {
        VBox root = new VBox();
        ImageView display = new ImageView();
        display.setImage(displayImage);
        root.getChildren().add(display);
        CPU.loadProgram("C:\\chip8\\brix.rom");
        Scene scene = new Scene(root, 640, 320);
        clearDisplay();
        startExecution();
        primaryStage.setTitle("Chip 8 Emulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    public static void startExecution() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    CPUstart();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Chip8.class.getName()).log(Level.SEVERE, null, ex);
                }
            } 
        };
        Thread backgroundThread = new Thread(task);
        backgroundThread.setDaemon(true);
        backgroundThread.setPriority(Thread.MAX_PRIORITY);
        backgroundThread.start();    
    }
    public static void CPUstart() throws InterruptedException {
        while(true) {
            //Thread.sleep(2);
            CPU.executeInstruction();
        }
    }
    public static void clearDisplay() {
        for(int x = 0;x<64;x++) {
            for(int y = 0;y<32;y++) {
                displayMemory[x][y] = 1;
                drawPixel(x,y);
            }
        }
    }
    public static boolean drawPixel(int x,int y) {
        boolean flag = false;
        if(x >= 0 && x <= 63 && y>=0 && y<=31) {
            int xLoc = x * 10;
            int yLoc = y * 10;                    
            PixelWriter displayWriter = displayImage.getPixelWriter(); 
            if(displayMemory[x][y] < 1) {
                displayMemory[x][y] = 1;
                for(int i = 0;i<10;i++) {
                    for(int j = 0;j<10;j++) {
                        displayWriter.setColor(xLoc+i, yLoc+j, Color.CYAN);
                    }
                }
            }
            else {
                displayMemory[x][y] = 0;
                for(int i = 0;i<10;i++) {
                    for(int j = 0;j<10;j++) {
                        displayWriter.setColor(xLoc+i, yLoc+j, Color.BLACK);
                    }
                }
                flag = true;
            }
            
            
        }
        if(flag == true) {
            return true;
        }
        else{
        return false;
        }
    }
    
}
