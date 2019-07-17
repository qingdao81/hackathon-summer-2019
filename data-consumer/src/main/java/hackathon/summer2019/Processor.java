package hackathon.summer2019;

import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.logging.Logger;

public class Processor {

  private static final Logger LOGGER = Logger.getLogger(Processor.class.getSimpleName());

  // https://github.com/prometheus/client_java
  private static final Gauge requests = Gauge.build()
      .name("method_calls_total").help("Amount of methods call from method \"processData(data)\"").register();
  private static final Histogram objectifyString_Duration_Histogram = Histogram.build().buckets(0.000001,0.000005, 0.000006, 0.000007, 0.000008, 0.000009,  0.00001, 0.00005,  0.0001, 0.0005)
           .name("objectify_String_Histogram_seconds").help("Duration of objectify_String in seconds by a histogram metric.").register();

  private static final Histogram useAES_Duration_Histogram = Histogram.build().buckets(0.011, 0.012, 0.013, 0.014, 0.015, 0.016, 0.017, 0.018, 0.019, 0.02, 0.025, 0.03, 0.035)
     .name("useAES_Duration_Histogram_seconds").help("Duration of useAES in seconds by a histogram metric.").register();

  private static final Histogram streamingHistogram = Histogram.build().buckets(0.011, 0.012, 0.013, 0.014, 0.015, 0.016, 0.017, 0.018, 0.019, 0.02, 0.025, 0.03, 0.035)
      .name("streaming_Histogram_seconds").help("duration of streaming test").register();

  static int[] values = new int[1000000];

  {
    for (int i = 0; i < 1000000; i++) {
      values[i] = i;
    }
  }

   private void processAESRequest(String data) {
     Histogram.Timer requestTimer = useAES_Duration_Histogram.startTimer();
     try {
         useAES(data);
     } catch (Exception e) {
       LOGGER.info("Encryption failed: " + e.getMessage());
     } finally {
       requestTimer.observeDuration();
       increase_gauge();
     }
   }

  private void processObjectifyStringRequest(String data) {
    Histogram.Timer requestTimer = objectifyString_Duration_Histogram.startTimer();
    try {
        objectify_String(data);
    } catch (Exception e) {
      LOGGER.info("Encryption failed: " + e.getMessage());
    } finally {
      requestTimer.observeDuration();
      increase_gauge();
    }
  }

  private void processStreaming(String data) {

    Histogram.Timer requestTimer = streamingHistogram.startTimer();
    try {
      Arrays.stream(values)
          .map(x -> x + 1)
          .map(x -> x * 2)
          .map(x -> x + 2)
          .reduce(0, Integer::sum);
    } finally {
      requestTimer.observeDuration();
    }
  }

  public void processData(String data, int method) {

    if(method == Consumer.METHOD_STRINGIFY){
      processObjectifyStringRequest(data);
    } else if (method == Consumer.METHOD_STREAMING){
      processStreaming(data);
    } else if(method == Consumer.METHOD_CRYPTO){
      processAESRequest(data);
    }    
  }

  private void useAES(String data) throws Exception{
    Cryptor cryptor = new Cryptor();
    byte[] key = cryptor.getKey();
    byte[] encoded = cryptor.encrypt(data.getBytes("UTF-8"), new String(key, "UTF-8"));
    byte[] decoded = cryptor.decrypt(encoded, new String(key, "UTF-8"));
    if (data.equals(new String(decoded))) {
      //LOGGER.info("successfully using crypto");
    } else {
      //LOGGER.info("crypto is not working");
    }
  }

  private void objectify_String(String data){
    for (Character a : data.toCharArray()){
      BigInteger bigNumber = new BigInteger(String.valueOf((int)a.charValue()));
      bigNumber.multiply((bigNumber != null) ? bigNumber : new BigInteger(new byte[]{01}));
    }
  }

  private void increase_gauge(){
    requests.inc();
  }

}
