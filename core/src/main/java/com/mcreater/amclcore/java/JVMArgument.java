package com.mcreater.amclcore.java;

import com.mcreater.amclcore.command.CommandArg;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JVMArgument {
    public static final JVMArgument FILE_ENCODING = create("-Dfile.encoding=${encoding}");
    public static final JVMArgument MINECRAFT_CLIENT_JAR = create("-Dminecraft.client.jar=${jar_path}");
    public static final JVMArgument UNLOCK_EXPERIMENTAL_OPTIONS = create("-XX:+UnlockExperimentalVMOptions");
    public static final JVMArgument USE_G1GC = create("-XX:-UseG1GC");
    public static final JVMArgument USE_ZGC = create("-XX:-UseZGC");
    public static final JVMArgument STDOUT_ENCODING = create("-Dstdout.encoding=${encoding}");
    public static final JVMArgument STDERR_ENCODING = create("-Dstderr.encoding=${encoding}");
    public static final JVMArgument GC_YOUNG_SIZE_PERCENT = create("-XX:G1NewSizePercent=${percent}");
    public static final JVMArgument GC_RESERVE_SIZE_PERCENT = create("-XX:G1ReservePercent=${percent}");
    public static final JVMArgument MAX_GC_PAUSE = create("-XX:MaxGCPauseMillis=${millis}");
    public static final JVMArgument MAX_HEAP_SIZE = create("-Xmx${size}m");
    public static final JVMArgument MIN_HEAP_SIZE = create("-Xms${size}m");
    public static final JVMArgument HEAP_REGION_SIZE = create("-XX:HeapRegionSize=${size}");
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
    private String raw;

    public static JVMArgument create(String raw) {
        return new JVMArgument(raw);
    }

    public CommandArg toCommandArg() {
        return toCommandArg(new HashMap<>());
    }

    public CommandArg toCommandArg(@NotNull Map<String, Object> parseMap) {
        AtomicReference<String> result = new AtomicReference<>(raw);
        parseMap.forEach((s, o) -> {
            String rep = String.format("${%s}", s);
            if (raw.contains(rep)) result.set(raw.replace(rep, o.toString()));
        });
        return CommandArg.create(result.get());
    }
}
