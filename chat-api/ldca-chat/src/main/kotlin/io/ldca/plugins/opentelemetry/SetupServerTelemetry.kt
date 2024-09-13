import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.semconv.ResourceAttributes



fun setupOpenTelemetry(): OpenTelemetry {

    /// Define the resource for service attributes (name, version, environment)
    val resource: Resource = Resource.create(
            Attributes.builder()
                .put("service.name", "ldca")
                .put("service.version", "tv1")
                .put("deployment.environment", "dev")
                .build()
            );

    // Configure the OTLP gRPC exporter
    val spanExporter : OtlpGrpcSpanExporter = OtlpGrpcSpanExporter.builder()
//        .setEndpoint("http://host.docker.internal:8200")
        .setEndpoint("http://fleet-server:8200")
        .addHeader("Authorization", "Bearer supersecrettoken")
        .build();

    // Create a BatchSpanProcessor to send spans to the exporter
    val sdkTracerProvider: SdkTracerProvider = SdkTracerProvider.builder()
        .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
        .setResource(resource)
        .build();

    // Set up the OpenTelemetry SDK
    return OpenTelemetrySdk.builder()
        .setTracerProvider(sdkTracerProvider)
        .buildAndRegisterGlobal()
}