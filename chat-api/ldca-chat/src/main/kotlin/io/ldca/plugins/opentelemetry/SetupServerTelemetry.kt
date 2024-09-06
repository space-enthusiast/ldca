import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor

fun setupOpenTelemetry(): OpenTelemetry {
    // Configure the exporter
    val spanExporter = OtlpGrpcSpanExporter.builder().build()

    // Configure the span processor
    val spanProcessor = BatchSpanProcessor.builder(spanExporter).build()

    // Configure the SDK tracer provider
    val tracerProvider = SdkTracerProvider.builder()
        .addSpanProcessor(spanProcessor)
        .build()

    // Set up the OpenTelemetry SDK
    return OpenTelemetrySdk.builder()
        .setTracerProvider(tracerProvider)
        .build()
}