package me.jbuelow.rov.wet.service.camera;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.ds.v4l4j.V4l4jDriver;
import java.awt.Dimension;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import me.jbuelow.rov.wet.service.camera.agent.impl.ServerAgent;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CameraService {
  //TODO make start a server for many cameras

  //TODO make these values configurable from the config file
  private Webcam webcam;
  private Dimension resolution = new Dimension(1280, 720);
  private int fps = 30;
  private int bitrate = 12000000;
  private int quality = 10;
  private boolean quick = true;


  @PostConstruct
  public void initialize() {
    setLibraryOptions();
    try {
      webcam = Webcam.getDefault();
    } catch (WebcamException e) {
      log.error("Exception was thrown while fetching camera. is a compatible webcam installed?", e);
      return;
    }
    if (Objects.isNull(webcam)) {
      log.error("No cameras were found. make sure your camera is properly installed.");
      return;
    }
    log.debug("Starting camera module for {}", webcam);
    webcam.setCustomViewSizes(resolution);
    webcam.setViewSize(resolution);

    ServerAgent agent = new ServerAgent(webcam, resolution, fps, bitrate, quality, quick);
    agent.start(new InetSocketAddress("localhost", 20000)); //TODO make port a part of application.yml config
  }

  private void setLibraryOptions() {
    log.debug("Setting camera lib options");
    String osName = System.getProperty("os.name");
    boolean nix = Pattern.matches("^.*(nux)|(nix)|(aix).*$", osName);
    if (nix) {
      log.debug("This is a *nix system. Using v4l driver");
      Webcam.setDriver(new V4l4jDriver());
    }
  }

}
