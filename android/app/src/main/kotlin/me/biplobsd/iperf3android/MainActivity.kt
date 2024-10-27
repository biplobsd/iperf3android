package me.biplobsd.iperf3android

import android.os.Bundle
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.EventChannel
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class MainActivity : FlutterActivity() {
    private val CHANNEL = "com.example.iperf3/start"
    private val STREAM = "com.example.iperf3/output"
    private var eventSink: EventChannel.EventSink? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MainActivity", "App started")

        // Call the copy and initialize binary method
        copyAndInitializeBinary()

        flutterEngine?.dartExecutor?.binaryMessenger?.let {
            MethodChannel(it, CHANNEL).setMethodCallHandler { call, result ->
                if (call.method == "startIperf") {
                    val ip = call.argument<String>("ip")
                    val port = call.argument<Int>("port") ?: 5201
                    startIperfTest(ip, port)
                    result.success(null)
                } else {
                    result.notImplemented()
                }
            }
        }

        EventChannel(flutterEngine?.dartExecutor?.binaryMessenger, STREAM).setStreamHandler(object : EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                eventSink = events
                Log.d("MainActivity", "EventChannel onListen triggered")
            }

            override fun onCancel(arguments: Any?) {
                eventSink = null
                Log.d("MainActivity", "EventChannel onCancel triggered")
            }
        })
    }

    private fun copyAndInitializeBinary() {
        val binaryFile = File(filesDir, "iperf3")

        if (!binaryFile.exists()) {
            // Copy the binary from assets
            try {
                val inputStream: InputStream = assets.open("iperf3")
                val outputStream = FileOutputStream(binaryFile)

                val buffer = ByteArray(1024)
                var length: Int
                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                // Set executable permissions
                binaryFile.setExecutable(true)

                Log.d("MainActivity", "iperf3 binary copied and initialized")
            } catch (e: IOException) {
                Log.e("MainActivity", "Error copying iperf3 binary", e)
            }
        } else {
            Log.d("MainActivity", "iperf3 binary already exists")
        }
    }

    private fun startIperfTest(ip: String?, port: Int) {
        Log.d("MainActivity", "Starting iperf3 test")

        Thread {
            try {
                // Start a root shell process
                val process = Runtime.getRuntime().exec("su")
                val outputStream = OutputStreamWriter(process.outputStream)

                // Command to execute iperf3 with the specified IP and port
                val command = "${filesDir.absolutePath}/iperf3 -c ${ip ?: "127.0.0.1"} -p $port\n"
                outputStream.write(command)
                outputStream.flush()
                outputStream.close() // Close the output stream to start the command

                Log.d("MainActivity", "iperf3 command executed: $command")

                // Read both stdout and stderr
                val stdoutReader = BufferedReader(InputStreamReader(process.inputStream))
                val stderrReader = BufferedReader(InputStreamReader(process.errorStream))

                var line: String?

                // Separate threads to read from stdout and stderr
                val stdoutThread = Thread {
                    try {
                        while (stdoutReader.readLine().also { line = it } != null) {
                            runOnUiThread {
                                eventSink?.success(line) // Send stdout to Flutter
                            }
                            Log.d("MainActivity", "stdout: $line")
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            eventSink?.error("STDOUT_ERROR", "Error reading stdout: ${e.message}", null)
                        }
                        Log.e("MainActivity", "Error reading stdout", e)
                    } finally {
                        stdoutReader.close()
                    }
                }

                val stderrThread = Thread {
                    try {
                        while (stderrReader.readLine().also { line = it } != null) {
                            runOnUiThread {
                                eventSink?.success("[Error] $line") // Send stderr to Flutter
                                Log.e("MainActivity", "[Error] $line") // Log the error
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            eventSink?.error("STDERR_ERROR", "Error reading stderr: ${e.message}", null)
                        }
                        Log.e("MainActivity", "Error reading stderr", e)
                    } finally {
                        stderrReader.close()
                    }
                }

                // Start both threads
                stdoutThread.start()
                stderrThread.start()

                // Wait for the process to complete
                stdoutThread.join()
                stderrThread.join()
                process.waitFor()

                // Notify the completion of the command
                runOnUiThread {
                    eventSink?.success("iperf3 test completed")
                }
                Log.d("MainActivity", "iperf3 test completed")

            } catch (e: Exception) {
                runOnUiThread {
                    eventSink?.error("EXECUTION_ERROR", "Error running iperf3: ${e.message}", null)
                }
                Log.e("MainActivity", "Execution error", e)
            }
        }.start()
    }
}
