import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import 'iperf_providers.dart';

part 'iperf_listen_to_output_provider.g.dart';

@riverpod
Stream<List<String>> iperfListenToOutput(Ref ref) async* {
  final useCase = ref.read(startIperfTestProvider);

  var allLogs = const <String>[];
  await for (final logs in useCase.listenToOutput()) {
    allLogs = [...allLogs, logs];
    yield allLogs;
  }
}
