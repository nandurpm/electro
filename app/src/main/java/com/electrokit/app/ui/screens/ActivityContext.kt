package com.electrokit.app.ui.screens

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity

/** Resolves an activity through theme/context wrappers without an unsafe cast.
 * @return Hosting Compose activity, or null when this context has no activity.
 */
fun Context.componentActivity(): ComponentActivity? {
    var current: Context = this
    val visited = mutableSetOf<Context>()
    while (visited.add(current)) {
        if (current is ComponentActivity) return current
        current = (current as? ContextWrapper)?.baseContext ?: return null
    }
    return null
}
