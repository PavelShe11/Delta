package io.github.pavelshel1.delta

import android.app.Application
import ru.ok.tracer.CoreTracerConfiguration
import ru.ok.tracer.HasTracerConfiguration
import ru.ok.tracer.TracerConfiguration
import ru.ok.tracer.crash.report.CrashFreeConfiguration
import ru.ok.tracer.crash.report.CrashReportConfiguration
import ru.ok.tracer.disk.usage.DiskUsageConfiguration
import ru.ok.tracer.heap.dumps.HeapDumpConfiguration

class DeltaApp: Application(), HasTracerConfiguration {
    override val tracerConfiguration: List<TracerConfiguration>
        get() = listOf(
            CoreTracerConfiguration.build {},
            CrashReportConfiguration.build {
                setEnabled(true)
                setNativeEnabled(true)
                setSendAnr(true)
            },
            CrashFreeConfiguration.build {
                setEnabled(true)
            },
            HeapDumpConfiguration.build {
                setEnabled(true)
            },
            DiskUsageConfiguration.build {
                setEnabled(true)
                setInterestingSize(3L * 1024 * 1024 * 1024) // 3GB. Default 10GB
                setProbability(100)
            }
        )

}