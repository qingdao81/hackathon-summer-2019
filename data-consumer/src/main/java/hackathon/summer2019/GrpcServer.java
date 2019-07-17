package hackathon.summer2019;

import hackathon.summer2019.grpc.DataRequest;
import hackathon.summer2019.grpc.DataResponse;
import hackathon.summer2019.grpc.DataServiceGrpc;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.prometheus.client.Counter;
import java.io.IOException;
import java.util.logging.Logger;

public class GrpcServer {

  private static final Logger LOGGER = Logger.getLogger(GrpcServer.class.getName());

  private static final Counter GRPC_REQUESTS = Counter.build()
      .name("grpc_requests_total").help("Grpc total requests.").register();

  private int method = -1;

  public GrpcServer(int port, Processor processor, int method) throws IOException {
    ServerBuilder.forPort(port)
        .addService(new DataServiceImpl(processor, method))
        .build()
        .start();
    this.method = method;
    LOGGER.info("Grpc server started, listening on " + port);
  }

  static class DataServiceImpl extends DataServiceGrpc.DataServiceImplBase {

    private final Processor processor;

    private final int method;

    public DataServiceImpl(Processor processor, int method) {
      this.processor = processor;
      this.method = method;
    }

    @Override
    public void sendData(DataRequest request, StreamObserver<DataResponse> responseObserver) {
      processor.processData(request.getData(), method);
      DataResponse response = DataResponse.newBuilder().setMessage("done").build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
      GRPC_REQUESTS.inc();
    }
  }
}
