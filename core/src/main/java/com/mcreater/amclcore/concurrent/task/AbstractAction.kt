package com.mcreater.amclcore.concurrent.task


abstract class AbstractAction : AbstractTask<Nothing?>() {
    @Throws(Exception::class)
    protected abstract fun execute()

    @Throws(Exception::class)
    override fun call(): Nothing? {
        execute()
        return null
    }
}
