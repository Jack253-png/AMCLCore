package com.mcreater.amclcore.resources

import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLClassLoader
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream


class ResourceFetcher {
    companion object {
        @JvmStatic
        private var CLASSLOADER: URLClassLoader? =
            URLClassLoader(arrayOfNulls(0), ResourceFetcher::class.java.getClassLoader())

        @JvmStatic
        fun classloader(): URLClassLoader? {
            return CLASSLOADER
        }

        @JvmStatic
        @Throws(IOException::class)
        fun addRes(urls: Array<URL>?) {
            val u = CLASSLOADER!!.urLs
            CLASSLOADER!!.close()
            CLASSLOADER = null
            CLASSLOADER = URLClassLoader(
                Stream.concat(Arrays.stream(u), Arrays.stream(urls)).toArray { arrayOf() },
                ResourceFetcher::class.java.getClassLoader()
            )
        }

        @JvmStatic
        @Throws(IOException::class)
        fun addRes(url: URL) {
            addRes(arrayOf(url))
        }

        @JvmStatic
        @Throws(IOException::class)
        fun addRes(url: List<URL>) {
            addRes(url.toTypedArray<URL>())
        }

        @JvmStatic
        @Throws(IOException::class)
        fun removeRes(urls: Array<URL>?) {
            val u = CLASSLOADER!!.urLs
            val u2 = Arrays.stream(u).collect(Collectors.toList())
            u2.removeAll(Arrays.stream(urls).collect(Collectors.toList()))
            CLASSLOADER!!.close()
            CLASSLOADER = null
            CLASSLOADER = URLClassLoader(u2.toTypedArray<URL>(), ResourceFetcher::class.java.getClassLoader())
        }

        @JvmStatic
        @Throws(IOException::class)
        fun removeRes(url: URL) {
            removeRes(arrayOf(url))
        }

        @JvmStatic
        @Throws(IOException::class)
        fun removeRes(url: List<URL>) {
            removeRes(url.toTypedArray<URL>())
        }

        @JvmStatic
        operator fun get(id: String?, name: String?): InputStream? {
            return ResourceFetcher[String.format("assets/%s/%s", id, name)]
        }

        @JvmStatic
        operator fun get(path: String?): InputStream? {
            return CLASSLOADER!!.getResourceAsStream(path)
        }

        @JvmStatic
        @Throws(IOException::class)
        fun getFiles(path: String?): List<URL> {
            return Collections.list(CLASSLOADER!!.getResources(path))
        }
    }
}
