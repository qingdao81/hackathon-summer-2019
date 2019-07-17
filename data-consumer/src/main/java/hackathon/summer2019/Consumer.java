package hackathon.summer2019;

import io.prometheus.client.exporter.HTTPServer;
import java.util.logging.Logger;

public class Consumer {

  private static final Logger LOGGER = Logger.getLogger(Consumer.class.getName());

  private static final int GRPC_PORT = 50051;

  private static final int PROMETHEUS_PORT = 9999;

  static final int METHOD_STRINGIFY = 1;
  static final int METHOD_STREAMING = 2;
  static final int METHOD_CRYPTO = 3;

  private static String NO_INPUT_MSG = "Please provide a method selection:\n "
    + "\t\"1\": process data with \"creating BigInteger-Objects\"\n"
    + "\t\"2\": process data with \"streaming\"\n"
    + "\t\"3\": process data with \"performing Crypto\"\n";

  public static void main(String[] args) throws Exception {
    int method_switch = -1;
    try{
      method_switch = Integer.parseInt(args[0]);
    } catch (ArrayIndexOutOfBoundsException a){
      LOGGER.info(NO_INPUT_MSG);
      System.exit(-1);
    } catch (NumberFormatException e){
      LOGGER.info(NO_INPUT_MSG);
      System.exit(-1);
    }

    if (method_switch < METHOD_STRINGIFY || method_switch > METHOD_CRYPTO) {
      LOGGER.info(NO_INPUT_MSG);
      System.exit(-1);
    } else {
      GrpcServer grpcServer = new GrpcServer(GRPC_PORT, new Processor(), method_switch);
      LOGGER.info("prometheus server started, listening on " + PROMETHEUS_PORT);
      HTTPServer prometheusServer = new HTTPServer(PROMETHEUS_PORT);
    }
  }
}
