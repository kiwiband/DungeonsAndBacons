package kiwiband.dnb.ui.activities

import kiwiband.dnb.ui.AppContext
import kiwiband.dnb.ui.views.View

/**
 * Activity is an entity that takes an entire screen and handles key presses.
 * Its lifecycle is:
 * - Activity created
 * - start() is called from outside of the activity
 * - onStart() gets invoked
 * - finish() is called from inside of the activity
 * - onFinish() gets invoked
 * After finishing the activity, the screen needs to be refreshed from outside.
 */
abstract class Activity<T>(protected val context: AppContext,
                           private val callback: (T) -> Unit) {
    private val renderer = context.renderer
    private val lock = Object()
    private var result: T? = null
    /**
     * The root view of the activity. It is always placed in a top left corner.
     */
    private lateinit var rootView: View

    /**
     * Refreshes a screen.
     */
    fun drawScene() {
        renderer.clearScreen()
        renderer.setOffset(0, 0)
        beforeDraw()
        rootView.draw(renderer)
        renderer.refreshScreen()
    }

    /**
     * Calls before each rootView draw
     */
    open fun beforeDraw() {}

    private fun close() {
        context.eventBus.pressKey.popLayer()
        val activities = context.activities
        activities.removeLast()
        activities.peekLast()?.also {
            it.onAppear()
            it.drawScene()
        }
    }

    /**
     * Calls each time when activity appears on the screen
     */
    protected open fun onAppear() {}

    /**
     * Finishes the activity.
     * DO NOT call this before calling start() (i.e. in constructor)
     */
    protected fun finish(result: T) {
        close()
        onFinish(result)
        callback(result)
    }

    /**
     * Starts the activity.
     * Does not refresh the screen by default, you can call drawScene() in onStart()
     */
    fun start() {
        rootView = createRootView()
        context.eventBus.pressKey.pushLayer()
        context.activities.addLast(this)
        onStart()
        onAppear()
        drawScene()
        afterStart()
    }

    /**
     * A function that gets called once on activity start (before onStart())
     * @return the root view of the activity
     */
    abstract fun createRootView(): View

    /**
     * A handler that gets invoked upon activity start.
     */
    open fun onStart() {}

    /**
     * Calls after activity start and first draw
     */
    open fun afterStart() {}

    /**
     * A handler that gets invoked upon activity finish.
     */
    open fun onFinish(result: T) {}

    open fun resize(height: Int, width: Int) {
        rootView.resize(width, height)
    }
}