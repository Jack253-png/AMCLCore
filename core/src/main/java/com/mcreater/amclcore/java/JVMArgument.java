package com.mcreater.amclcore.java;

import com.mcreater.amclcore.command.CommandArg;

public class JVMArgument extends CommandArg {
    public static final JVMArgument FILE_ENCODING = create("-Dfile.encoding=${encoding}");
    public static final JVMArgument MINECRAFT_CLIENT_JAR = create("-Dminecraft.client.jar=${jar_path}");
    public static final JVMArgument UNLOCK_EXPERIMENTAL_OPTIONS = create("-XX:+UnlockExperimentalVMOptions");
    public static final JVMArgument USE_G1GC = create("-XX:+UseG1GC");
    public static final JVMArgument USE_ZGC = create("-XX:+UseZGC");
    public static final JVMArgument STDOUT_ENCODING = create("-Dstdout.encoding=${encoding}");
    public static final JVMArgument STDERR_ENCODING = create("-Dstderr.encoding=${encoding}");
    public static final JVMArgument GC_YOUNG_SIZE_PERCENT = create("-XX:G1NewSizePercent=${percent}");
    public static final JVMArgument GC_RESERVE_SIZE_PERCENT = create("-XX:G1ReservePercent=${percent}");
    public static final JVMArgument MAX_GC_PAUSE = create("-XX:MaxGCPauseMillis=${millis}");
    public static final JVMArgument MAX_HEAP_SIZE = create("-Xmx${size}");
    public static final JVMArgument MIN_HEAP_SIZE = create("-Xms${size}");
    public static final JVMArgument GC_THREAD = create("-XX:ParallelGCThreads=${n}");
    public static final JVMArgument HEAP_REGION_SIZE = create("-XX:G1HeapRegionSize=${size}");
    public static final JVMArgument ADAPTIVE_SIZE_POLICY = create("-XX:-UseAdaptiveSizePolicy");
    public static final JVMArgument STACK_TRACE_FAST_THROW = create("-XX:-OmitStackTraceInFastThrow");
    public static final JVMArgument DONT_COMPILE_HUGE_METHODS = create("-XX:-DontCompileHugeMethods");
    public static final JVMArgument FML_IGNORE_INVAILD_CERTIFICATES = create("-Dfml.ignoreInvalidMinecraftCertificates=${enable}");
    public static final JVMArgument FML_IGNORE_PATCH_DISCREPANCIES = create("-Dfml.ignorePatchDiscrepancies=${enable}");
    // default false
    public static final JVMArgument USE_CODEBASE_ONLY = create("-Djava.rmi.server.useCodebaseOnly=${enable}");
    public static final JVMArgument TRUST_URL_CODE_BASE = create("-Dcom.sun.jndi.rmi.object.trustURLCodebase=${enable}");
    public static final JVMArgument DISABLE_MSG_LOOPUPS = create("-Dlog4j2.formatMsgNoLookups=${enable}");
    public static final JVMArgument INTEL_PERFORMANCE = create("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");
    public static final JVMArgument MINECRAFT_LAUNCHER_BRAND = create("-Dminecraft.launcher.brand=${launcher_brand}");
    public static final JVMArgument MINECRAFT_LAUNCHER_VERSION = create("-Dminecraft.launcher.version=${launcher_version}");
    public static final JVMArgument JAVA_LIBRARY_PATH = create("-Djava.library.path=${lib_path}");
    public static final JVMArgument CLASSPATH = create("-cp");
    private String raw;

    protected JVMArgument(String command) {
        super(command);
    }

    public static JVMArgument create(String raw) {
        return new JVMArgument(raw);
    }
}
