package com.mcreater.amclcore.java

import com.mcreater.amclcore.command.CommandArg


class JVMArgument protected constructor(command: String?) : CommandArg(command) {
    companion object {
        @JvmStatic
        val FILE_ENCODING = create("-Dfile.encoding=\${encoding}")

        @JvmStatic
        val MINECRAFT_CLIENT_JAR = create("-Dminecraft.client.jar=\${jar_path}")

        @JvmStatic
        val UNLOCK_EXPERIMENTAL_OPTIONS = create("-XX:+UnlockExperimentalVMOptions")

        @JvmStatic
        val USE_G1GC = create("-XX:+UseG1GC")

        @JvmStatic
        val USE_ZGC = create("-XX:+UseZGC")

        @JvmStatic
        val STDOUT_ENCODING = create("-Dstdout.encoding=\${encoding}")

        @JvmStatic
        val STDERR_ENCODING = create("-Dstderr.encoding=\${encoding}")

        @JvmStatic
        val GC_YOUNG_SIZE_PERCENT = create("-XX:G1NewSizePercent=\${percent}")

        @JvmStatic
        val GC_RESERVE_SIZE_PERCENT = create("-XX:G1ReservePercent=\${percent}")

        @JvmStatic
        val MAX_GC_PAUSE = create("-XX:MaxGCPauseMillis=\${millis}")

        @JvmStatic
        val MAX_HEAP_SIZE = create("-Xmx\${size}")

        @JvmStatic
        val MIN_HEAP_SIZE = create("-Xms\${size}")

        @JvmStatic
        val GC_THREAD = create("-XX:ParallelGCThreads=\${n}")

        @JvmStatic
        val HEAP_REGION_SIZE = create("-XX:G1HeapRegionSize=\${size}")

        @JvmStatic
        val ADAPTIVE_SIZE_POLICY = create("-XX:-UseAdaptiveSizePolicy")

        @JvmStatic
        val STACK_TRACE_FAST_THROW = create("-XX:-OmitStackTraceInFastThrow")

        @JvmStatic
        val DONT_COMPILE_HUGE_METHODS = create("-XX:-DontCompileHugeMethods")

        @JvmStatic
        val FML_IGNORE_INVAILD_CERTIFICATES = create("-Dfml.ignoreInvalidMinecraftCertificates=\${enable}")

        @JvmStatic
        val FML_IGNORE_PATCH_DISCREPANCIES = create("-Dfml.ignorePatchDiscrepancies=\${enable}")

        // default false
        @JvmStatic
        val USE_CODEBASE_ONLY = create("-Djava.rmi.server.useCodebaseOnly=\${enable}")

        @JvmStatic
        val TRUST_URL_CODE_BASE = create("-Dcom.sun.jndi.rmi.object.trustURLCodebase=\${enable}")

        @JvmStatic
        val DISABLE_MSG_LOOPUPS = create("-Dlog4j2.formatMsgNoLookups=\${enable}")

        @JvmStatic
        val INTEL_PERFORMANCE =
            create("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump")

        @JvmStatic
        val MINECRAFT_LAUNCHER_BRAND = create("-Dminecraft.launcher.brand=\${launcher_brand}")

        @JvmStatic
        val MINECRAFT_LAUNCHER_VERSION = create("-Dminecraft.launcher.version=\${launcher_version}")

        @JvmStatic
        val JAVA_LIBRARY_PATH = create("-Djava.library.path=\${lib_path}")

        @JvmStatic
        val CLASSPATH = create("-cp")

        @JvmStatic
        fun create(raw: String?): JVMArgument {
            return JVMArgument(raw)
        }
    }
}
