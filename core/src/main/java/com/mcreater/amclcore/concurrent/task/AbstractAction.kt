package com.mcreater.amclcore.concurrent.task


abstract class AbstractAction : AbstractTask<Void>() {
    @Throws(Exception::class)
    protected abstract fun execute()

    @Throws(Exception::class)
    override fun call(): Void? {
        execute()
        return null
    }
}
