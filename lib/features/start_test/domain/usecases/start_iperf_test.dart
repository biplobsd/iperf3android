import '../repositories/iperf_repository.dart';

class StartIperfTest {
  final IperfRepository repository;

  StartIperfTest(this.repository);

  Future<void> execute(String ip, int port) {
    return repository.startTest(ip, port);
  }

  Stream<String> listenToOutput() {
    return repository.getOutputStream();
  }
}
