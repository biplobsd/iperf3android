import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../data/datasource/iperf_data_source.dart';
import '../../data/repositories/iperf_repository_impl.dart';
import '../../domain/usecases/start_iperf_test.dart';

final ipProvider = StateProvider<String>((ref) => '');
final portProvider = StateProvider<String>((ref) => '5201');

final iperfDataSourceProvider = Provider((ref) => IperfDataSource(
      methodChannel: const MethodChannel('com.example.iperf3/start'),
      eventChannel: const EventChannel('com.example.iperf3/output'),
    ));

final iperfRepositoryProvider = Provider<IperfRepositoryImpl>((ref) {
  return IperfRepositoryImpl(dataSource: ref.read(iperfDataSourceProvider));
});

final startIperfTestProvider = Provider<StartIperfTest>((ref) {
  return StartIperfTest(ref.read(iperfRepositoryProvider));
});

final iperfOutputProvider = StreamProvider.autoDispose<String>((ref) {
  final useCase = ref.read(startIperfTestProvider);
  return useCase.listenToOutput();
});
