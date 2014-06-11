package org.queeg.homa;

import static com.google.common.collect.Maps.newHashMap;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

public class HomaDevice {
  private final Logger log = LoggerFactory.getLogger(getClass());

  private String deviceId;

  private String name;
  private String room;
  private Map<String, HomaControl> controls = newHashMap();

  public HomaDevice(String deviceId) {
    this.deviceId = deviceId;
  }

  public synchronized HomaControl getControl(String controlId) {
    HomaControl control = controls.get(controlId);
    if (control == null) {
      control = new HomaControl(deviceId, controlId);
      controls.put(controlId, control);
    }
    return control;
  }

  void handleMessage(List<String> topic, String message) {
    switch (topic.get(2)) {
    case "meta":
      handleMetaMessage(topic, message);
      break;
    case "controls":
      handleControlsMessage(topic, message);
      break;
    default:
      log.warn("Unknown message type : {}", topic);
    }

  }

  private void handleMetaMessage(List<String> topic, String message) {
    switch (topic.get(3)) {
    case "name":
      name = message;
      log.debug("{} : Set name to {}", deviceId, name);
      break;
    case "room":
      room = message;
      log.debug("{} : Set room to {}", deviceId, room);
      break;
    default:
      log.debug("{} : Unknown meta message {}:{}", deviceId, Joiner.on('/').join(topic), message);
    }
  }

  private void handleControlsMessage(List<String> topic, String message) {
    String controlId = topic.get(3);
    HomaControl control = getControl(controlId);
    control.handleMessage(topic, message);
  }
}
