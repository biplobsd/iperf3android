import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:iperf3android/features/start_test/presentation/providers/iperf_listen_to_output_provider.dart';
import '../providers/iperf_providers.dart';

class IperfScreen extends ConsumerWidget {
  const IperfScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final ipController = TextEditingController();
    final portController = TextEditingController();

    return Scaffold(
      appBar: AppBar(title: const Text('iPerf3 Test')),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            TextField(
              controller: ipController,
              decoration: const InputDecoration(labelText: 'Server IP Address'),
              onChanged: (value) => ref.read(ipProvider.notifier).state = value,
            ),
            TextField(
              controller: portController,
              decoration: const InputDecoration(labelText: 'Port Number'),
              keyboardType: TextInputType.number,
              onChanged: (value) =>
                  ref.read(portProvider.notifier).state = value,
            ),
            ElevatedButton(
              onPressed: () {
                final startIperfTest = ref.read(startIperfTestProvider);
                final ip = ref.read(ipProvider);
                final port = int.tryParse(ref.read(portProvider)) ?? 5201;
                startIperfTest.execute(ip, port);
              },
              child: const Text('Start Test'),
            ),
            Expanded(
              child: Consumer(
                builder: (context, ref, child) {
                  final outputStream = ref.watch(iperfListenToOutputProvider);
                  return switch (outputStream) {
                    AsyncData(:final value) => ListView.builder(
                        itemCount: value.length,
                        itemBuilder: (context, index) {
                          final message = value[index];
                          return Text(message);
                        },
                      ),
                    AsyncError(:final error) => Text(error.toString()),
                    _ => const CircularProgressIndicator(),
                  };
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}
