abstract class IperfRepository {
  Future<void> startTest(String ip, int port);
  Stream<String> getOutputStream();
}
