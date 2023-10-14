package com.mcreater.amclcore.java

import java.io.File


class JavaEnvironment private constructor(private val executable: File? = null) {
    fun getExecutable(): File? {
        return executable
    }

    companion object {
        @JvmStatic
        fun create(executable: File?): JavaEnvironment {
            return JavaEnvironment(executable)
        }
    }
}

