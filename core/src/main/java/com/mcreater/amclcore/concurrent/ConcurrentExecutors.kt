package com.mcreater.amclcore.concurrent

import com.mcreater.amclcore.exceptions.report.ExceptionReporter
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory
import java.util.concurrent.ForkJoinWorkerThread


open class ConcurrentExecutors {
    companion object {
        @JvmStatic
        val excHandler: Thread.UncaughtExceptionHandler =
            Thread.UncaughtExceptionHandler { t: Thread?, e: Throwable? ->
                ExceptionReporter.report(
                    e,
                    ExceptionReporter.ExceptionType.CONCURRENT
                )
            }

        /**
         * Main event queue(launchAsync, task shells...)<br></br>
         * 主要事件队列(启动，任务外壳...)
         */
        @JvmStatic
        val EVENT_QUEUE_EXECUTOR = AdvancedForkJoinPool(
            32,
            ForkJoinWorkerThreadFactoryImpl.INSTANCE,
            excHandler,
            true
        )

        /**
         * Launch event queue<br></br>
         * 启动事件队列
         */
        @JvmStatic
        val LAUNCH_EVENT_EXECUTOR = AdvancedForkJoinPool(
            32,
            ForkJoinWorkerThreadFactoryImpl.INSTANCE,
            excHandler,
            true
        )

        /**
         * Download event queue<br></br>
         * 下载事件队列
         */
        @JvmStatic
        val DOWNLOAD_QUEUE_EXECUTOR = AdvancedForkJoinPool(
            32,
            ForkJoinWorkerThreadFactoryImpl.INSTANCE,
            excHandler,
            true
        )

        /**
         * OAuth login queue<br></br>
         * OAuth 登录队列
         */
        @JvmStatic
        val OAUTH_LOGIN_EXECUTOR = AdvancedForkJoinPool(
            8,
            ForkJoinWorkerThreadFactoryImpl.INSTANCE,
            excHandler,
            true
        )

        /**
         * OAuth event queue<br></br>
         * OAuth 事件队列
         */
        @JvmStatic
        val OAUTH_EVENT_EXECUTOR = AdvancedForkJoinPool(
            32,
            ForkJoinWorkerThreadFactoryImpl.INSTANCE,
            excHandler,
            true
        )

        /**
         * Swing event queue(clipboard, desktop api...)<br></br>
         * Swing 事件队列(剪贴板，桌面API...)
         */
        @JvmStatic
        val AWT_EVENT_EXECUTOR = AdvancedForkJoinPool(
            8,
            ForkJoinWorkerThreadFactoryImpl.INSTANCE,
            excHandler,
            true
        )

        /**
         * interface event queue<br></br>
         * 接口事件队列
         */
        @JvmStatic
        val INTERFACE_EVENT_EXECUTORS: MutableMap<Any, ForkJoinPool> = HashMap()

        @JvmStatic
        fun createInterfaceEventExecutor(): ForkJoinPool {
            return ForkJoinPool(
                1,
                ForkJoinWorkerThreadFactoryImpl.INSTANCE,
                excHandler,
                true
            )
        }

        /**
         * Re-implementation for [ForkJoinPool.ForkJoinWorkerThreadFactory]<br></br>
         * 对 [ForkJoinPool.ForkJoinWorkerThreadFactory] 的重新实现
         */
    }

    class ForkJoinWorkerThreadFactoryImpl private constructor() : ForkJoinWorkerThreadFactory {
        override fun newThread(pool: ForkJoinPool): ForkJoinWorkerThread {
            val t = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool)
            t.setDaemon(true)
            return t
        }

        companion object {
            @JvmStatic
            val INSTANCE = ForkJoinWorkerThreadFactoryImpl()
        }
    }
}
