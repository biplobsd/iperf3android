import '../../domain/repositories/iperf_repository.dart';
import '../datasource/iperf_data_source.dart';

class IperfRepositoryImpl implements IperfRepository {
  final IperfDataSource dataSource;

  IperfRepositoryImpl({required this.dataSource});

  @override
  Future<void> startTest(String ip, int port) {
    return dataSource.startIperfTest(ip, port);
  }

  @override
  Stream<String> getOutputStream() {
    return dataSource.getIperfOutputStream();
  }
}
