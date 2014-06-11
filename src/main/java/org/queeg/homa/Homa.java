package org.queeg.homa;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.Maps.newHashMap;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;

public class Homa implements Closeable {
  private final Logger log = LoggerFactory.getLogger(getClass());

  private final IMqttAsyncClient mqtt;

  private final Map<String, HomaDevice> devices = newHashMap();

  public Homa(String broker) throws MqttException {
    mqtt = new MqttAsyncClient(broker, "homa-fibaro-bridge", new MemoryPersistence());
    mqtt.connect().waitForCompletion();

    mqtt.subscribe("/devices/#", 2).waitForCompletion();
    mqtt.setCallback(new MqttCallback() {
      @Override
      public void messageArrived(String topic, MqttMessage message) throws Exception {
        mqttMessage(topic, new String(message.getPayload(), UTF_8));
      }

      @Override
      public void deliveryComplete(IMqttDeliveryToken token) {
      }

      @Override
      public void connectionLost(Throwable cause) {
      }
    });
  }

  @Override
  public void close() throws IOException {
    try {
      mqtt.disconnect().waitForCompletion();
      mqtt.close();
    } catch (MqttException e) {
      throw new IOException(e);
    }
  }

  private void mqttMessage(String topic, String message) {
    List<String> topicParts = Splitter.on('/').omitEmptyStrings().splitToList(topic);
    String systemId = topicParts.get(1);

    HomaDevice device = getDevice(systemId);
    try {
      device.handleMessage(topicParts, message);
    } catch (Exception e) {
      log.warn("Exception handling message : {}", e.getMessage(), e);
    }
  }

  public synchronized HomaDevice getDevice(String systemId) {
    HomaDevice device = devices.get(systemId);
    if (device == null) {
      device = new HomaDevice(systemId);
      devices.put(systemId, device);
    }
    return device;
  }

  public static void main(String[] args) throws Exception {
    Homa homa = new Homa("tcp://mosquitto.home:1883");
    System.out.println("Back from constructor");
    Thread.sleep(10000);
    homa.close();
  }

}
