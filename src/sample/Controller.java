package sample;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class Controller {
    private static final int DISPLAY_TIME = 1500;
    private static final MediaView mediaView = new MediaView();
    private static final int MAX_PROGRAM = 10;
    private static final int MAX_VOLUME = 100;
    private static final ArrayList<String> FILENAMES = new ArrayList<>(Arrays.asList(
            "noise.mp4",
            "program1.mp4",
            "kiepscy.mp4",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
    ));
    private static double programCounter = 0;
    private static boolean turnedOn = false;
    private static boolean initFlag = true;
    private static int program = 1;
    private static int volume = 10;
    @FXML
    private ToggleButton power;
    @FXML
    private Pane screen;
    @FXML
    private Text actualProgramDisplay;
    @FXML
    private Text volumeDisplay;
    @FXML
    private Button ProgramM;
    @FXML
    private Button ProgramP;
    @FXML
    private Button VolumeM;
    @FXML
    private Button VolumeP;
    private boolean mute = false;
    protected static void runCounter() {
        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            if (Double.MAX_VALUE - programCounter < 2.0)
                                programCounter = 0;
                            programCounter++;
                        });
                    }
                }, 0, 1000);
    }

    @FXML
    protected void togglePower(ActionEvent event) {
        turnedOn = !turnedOn;
        if (turnedOn) {
            onTurnOn();
            showProgram(program);
        } else {
            if (mediaView.getMediaPlayer() != null) {
                mediaView.getMediaPlayer().dispose();
                mediaView.setMediaPlayer(null);
            }

        }
    }

    private void onTurnOn() {
        if (initFlag) {
            initFlag = false;
            mediaView.fitWidthProperty().bind(screen.widthProperty());
            mediaView.fitHeightProperty().bind(screen.heightProperty());
            screen.getChildren().add(mediaView);
        }
        playVideo(program);
    }

    private void playVideo(int program) {
        String path = FILENAMES.get(program) != null ? FILENAMES.get(program) : FILENAMES.get(0);
        System.out.println(path);
        MediaPlayer mediaPlayer = new MediaPlayer(new Media(new File(path).toURI().toString()));
        mediaPlayer.setOnReady(() -> rewindTo(mediaPlayer, programCounter));
        createLoop(mediaPlayer);
        if (mediaView.getMediaPlayer() != null)
            mediaView.getMediaPlayer().dispose();
        mediaView.setMediaPlayer(mediaPlayer);
        mediaPlayer.setVolume( volume / 100.0);
    }

    private void createLoop(MediaPlayer player) {
        player.setOnEndOfMedia(() -> {
            player.seek(Duration.ZERO);
            player.play();
        });
    }

    private void rewindTo(MediaPlayer player, double timeInSec) {
        double end = player.getMedia().getDuration().toSeconds();

        System.out.println(end);
        player.seek(Duration.seconds(timeInSec % end));
        player.play();
    }

    private void showProgram(int program) {
        actualProgramDisplay.setText(String.valueOf(program));
        actualProgramDisplay.setVisible(true);
        new Thread(
                new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        Thread.sleep(DISPLAY_TIME);
                        actualProgramDisplay.setVisible(false);
                        return null;
                    }
                }).start();
    }

    private void showVolume(int volume) {
        volumeDisplay.setText("Volume " + volume);
        System.out.println("Volume " + mediaView.getMediaPlayer().getVolume());
        new Thread(
                new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        volumeDisplay.setVisible(true);
                        Thread.sleep(DISPLAY_TIME);
                        volumeDisplay.setVisible(false);
                        return null;
                    }
                }).start();
    }

    @FXML
    protected void volumeHandler(ActionEvent event) {
        if (!turnedOn) return;
        Button b = (Button) event.getSource();
        if(b.getId().equals("VolumeP"))
        {
            if (volume < MAX_VOLUME) {
                volume++;
                mediaView.getMediaPlayer().setVolume(volume / 100.0);
            }
        }
        else
        {
            if (volume > 0) {
                volume--;
                mediaView.getMediaPlayer().setVolume( volume / 100.0);
            }
        }

        showVolume(volume);
    }

    @FXML
    protected void programHandler(ActionEvent event) {
        if (!turnedOn) return;
        Button b = (Button) event.getSource();
        if (b.getId().equals("ProgramP")) {
            program = (program == MAX_PROGRAM ? 1 : ++program);
        } else {
            program = (program == 1 ? MAX_PROGRAM : --program);
        }
        playVideo(program);
        showProgram(program);

    }

    @FXML
    protected void muteHandler(ActionEvent event) {
        if(!mute) {
            mediaView.getMediaPlayer().setVolume(0);
            volumeDisplay.setText("Muted");
            mute = true;
        }
        else
        {
            mediaView.getMediaPlayer().setVolume(volume);
            volumeDisplay.setText("unMuted");
            mute = false;
        }

    }
}
