package com.mcreater.amclcore.concurrent;

import com.mcreater.amclcore.exceptions.report.ExceptionReporter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

import static com.mcreater.amclcore.exceptions.report.ExceptionReporter.report;

public class ConcurrentExecutors {
    /**
     * Re-implementation for {@link ForkJoinPool.ForkJoinWorkerThreadFactory}<br>
     * 对 {@link ForkJoinPool.ForkJoinWorkerThreadFactory} 的重新实现
     */
    public static class ForkJoinWorkerThreadFactoryImpl implements ForkJoinPool.ForkJoinWorkerThreadFactory {
        public static final ForkJoinWorkerThreadFactoryImpl INSTANCE = new ForkJoinWorkerThreadFactoryImpl();

        private ForkJoinWorkerThreadFactoryImpl() {
        }

        public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            ForkJoinWorkerThread t = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
            t.setDaemon(true);
            return t;
        }
    }

    /**
     * Main event queue(launch, task shells...)<br>
     * 主要事件队列(启动，任务外壳...)
     */
    public static final ExtendForkJoinPool EVENT_QUEUE_EXECUTOR = new ExtendForkJoinPool(
            32,
            new ForkJoinWorkerThreadFactoryImpl(),
            (t, e) -> report(e, ExceptionReporter.ExceptionType.CONCURRENT),
            true
    );
    /**
     * Download event queue<br>
     * 下载事件队列
     */
    public static final ExtendForkJoinPool DOWNLOAD_QUEUE_EXECUTOR = new ExtendForkJoinPool(
            32,
            ForkJoinWorkerThreadFactoryImpl.INSTANCE,
            (t, e) -> report(e, ExceptionReporter.ExceptionType.CONCURRENT),
            true
    );
    /**
     * OAuth login queue<br>
     * OAuth 登录队列
     */
    public static final ExtendForkJoinPool OAUTH_LOGIN_EXECUTOR = new ExtendForkJoinPool(
            8,
            ForkJoinWorkerThreadFactoryImpl.INSTANCE,
            (t, e) -> report(e, ExceptionReporter.ExceptionType.CONCURRENT),
            true
    );
    /**
     * OAuth event queue<br>
     * OAuth 事件队列
     */
    public static final ExtendForkJoinPool OAUTH_EVENT_EXECUTOR = new ExtendForkJoinPool(
            32,
            ForkJoinWorkerThreadFactoryImpl.INSTANCE,
            (t, e) -> report(e, ExceptionReporter.ExceptionType.CONCURRENT),
            true
    );
    /**
     * Swing event queue(clipboard, desktop api...)<br>
     * Swing 事件队列(剪贴板，桌面API...)
     */
    public static final ExtendForkJoinPool AWT_EVENT_EXECUTOR = new ExtendForkJoinPool(
            8,
            ForkJoinWorkerThreadFactoryImpl.INSTANCE,
            (t, e) -> report(e, ExceptionReporter.ExceptionType.CONCURRENT),
            true
    );
    /**
     * interface event queue<br>
     * 接口事件队列
     */
    public static final Map<Object, ForkJoinPool> INTERFACE_EVENT_EXECUTORS = new HashMap<>();

    public static ForkJoinPool createInterfaceEventExecutor() {
        return new ForkJoinPool(
                1,
                ForkJoinWorkerThreadFactoryImpl.INSTANCE,
                (t, e) -> report(e, ExceptionReporter.ExceptionType.CONCURRENT),
                true
        );
    }
}
