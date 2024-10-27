import 'package:flutter/services.dart';

class IperfDataSource {
  final MethodChannel methodChannel;
  final EventChannel eventChannel;

  IperfDataSource({
    required this.methodChannel,
    required this.eventChannel,
  });

  Future<void> startIperfTest(String ip, int port) async {
    await methodChannel.invokeMethod('startIperf', {'ip': ip, 'port': port});
  }

  Stream<String> getIperfOutputStream() {
    return eventChannel.receiveBroadcastStream().map((data) => data ?? '');
  }
}
