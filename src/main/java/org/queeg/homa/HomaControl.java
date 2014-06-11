package org.queeg.homa;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Ints;

public class HomaControl {
  private final Logger log = LoggerFactory.getLogger(getClass());

  private final String deviceId;
  private final String controlId;

  private String value;
  private String type;
  private int order;
  private int max;

  private SetValueHandler setValueHandler = null;

  public HomaControl(String deviceId, String controlId) {
    this.deviceId = deviceId;
    this.controlId = controlId;
  }

  void handleMessage(List<String> topic, String message) {
    if (topic.size() == 4) {
      value = message;
      log.debug("{}.{} : New Value {}", deviceId, controlId, value);
    } else {
      switch (topic.get(4)) {
      case "on":
        handleSetValue(message);
        break;
      case "meta":
        handleMetaMessage(topic, message);
      }
    }
  }

  private void handleMetaMessage(List<String> topic, String message) {
    switch (topic.get(5)) {
    case "type":
      type = message;
      log.debug("{}.{} : New type value {}", deviceId, controlId, type);
      break;
    case "order":
      Integer order = Ints.tryParse(message);
      if (order != null) {
        this.order = order.intValue();
        log.debug("{}.{} : New order value {}", deviceId, controlId, order);
      }
      break;
    case "max":
      Integer max = Ints.tryParse(message);
      if (max != null) {
        this.max = max.intValue();
        log.debug("{}.{} : New max value {}", deviceId, controlId, max);
      }
      break;
    }
  }

  private void handleSetValue(String message) {
    log.debug("{}.{} : calling set handler with {}", deviceId, controlId, message);
    SetValueHandler handler = setValueHandler;
    if (handler != null) {
      handler.set(message);
    }
  }
}
