package org.snapshotscience.rov.dry.ui.video;

import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.snapshotscience.rov.dry.discovery.VehicleDiscoveryEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class VideoService {

  private final VideoFrameReceiver frameReceiver;
  private VideoTransport transport;

  @Value("${rov.camera.url}")
  private String videoUrl;

  public VideoService(VideoFrameReceiver frameReceiver) {
    this.frameReceiver = frameReceiver;
  }
  
  @PostConstruct
  public void init() {
    log.info("Started camera service");
  }

  @EventListener
  @Order(1)
  public void initiateConnection(VehicleDiscoveryEvent event) {
    new Thread() {
      @Override
      public void run() {
        startStream();
      }
    }.start();
  }

  private void startStream() {
    log.info("Starting camera stream...");
    transport = new VideoTransport(videoUrl, frameReceiver);
    transport.start();
  }

}
