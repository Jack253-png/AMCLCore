package com.mcreater.amclcore.java


class MemorySize private constructor(private val mem: Long = 0) {
    enum class MemoryUnit(val realname: String) {
        BYTES("b"),
        KILOBYTES("k"),
        MEGABYTES("m"),
        GIGABYTES("g")
    }

    override fun toString(): String {
        if (mem < 1024) return mem.toString() + "b"
        if (mem < 1024 * 1024) return (mem / 1024).toInt().toString() + "k"
        return if (mem < 1024 * 1024 * 1024) (mem / (1024 * 1024)).toInt()
            .toString() + "m" else (mem / (1024 * 1024 * 1024)).toInt()
            .toString() + "g"
    }

    companion object {
        @JvmStatic
        fun create(mem: Long): MemorySize {
            return MemorySize(mem)
        }

        @JvmStatic
        fun create(mem: Long, type: MemoryUnit): MemorySize {
            return create(mem.toString() + type.realname)
        }

        @JvmStatic
        fun createBytes(bytes: Long): MemorySize {
            return create(bytes, MemoryUnit.BYTES)
        }

        @JvmStatic
        fun createKiloBytes(kiloBytes: Long): MemorySize {
            return create(kiloBytes, MemoryUnit.KILOBYTES)
        }

        @JvmStatic
        fun createMegaBytes(megaBytes: Long): MemorySize {
            return create(megaBytes, MemoryUnit.MEGABYTES)
        }

        @JvmStatic
        fun createGigaBytes(gigaBytes: Long): MemorySize {
            return create(gigaBytes, MemoryUnit.GIGABYTES)
        }

        @JvmStatic
        fun create(mem: String): MemorySize {
            val bytes = mem.substring(0, mem.length - 1).toLong()
            return when (mem[mem.length - 1]) {
                'B', 'b' -> MemorySize(bytes)
                'K', 'k' -> MemorySize(bytes * 1024)
                'M', 'm' -> MemorySize(bytes * 1024 * 1024)
                'G', 'g' -> MemorySize(bytes * 1024 * 1024 * 1024)
                else -> MemorySize(mem.toLong())
            }
        }
    }
}

