package dev.cluvex.strata.core.data

import dev.cluvex.strata.core.model.BehaviorPrefs
import dev.cluvex.strata.core.model.StartFolder

object StartFolders {

    fun resolve(b: BehaviorPrefs, last: LastFolders, home: String): Pair<String, String> {
        fun one(own: StartFolder?, ownPath: String, lastPath: String?): String {
            val sf = own ?: b.startFolder
            val sp = if (own != null) ownPath else b.startPath
            return when (sf) {
                StartFolder.HOME -> home
                StartFolder.LAST -> usable(lastPath, home)
                StartFolder.CUSTOM -> usable(sp, home)
            }
        }
        return one(b.leftStart, b.leftPath, last.left) to one(b.rightStart, b.rightPath, last.right)
    }

    private fun usable(path: String?, home: String): String =
        path?.trim()?.takeIf { it.startsWith("/") && !it.contains('\u0000') }?.trimEnd('/')?.ifEmpty { "/" } ?: home
}
