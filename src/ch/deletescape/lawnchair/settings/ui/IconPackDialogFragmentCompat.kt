/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.deletescape.lawnchair.settings.ui

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.preference.PreferenceDialogFragmentCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import ch.deletescape.lawnchair.colors.ColorEngine
import ch.deletescape.lawnchair.getIcon
import ch.deletescape.lawnchair.iconpack.IconPackManager
import ch.deletescape.lawnchair.isVisible
import com.android.launcher3.R
import com.android.launcher3.Utilities

class IconPackDialogFragmentCompat : PreferenceDialogFragmentCompat(), AdapterView.OnItemClickListener {

    private val prefs by lazy { Utilities.getLawnchairPrefs(context) }
    private var pack = ""

    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pack = prefs.iconPack
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)

        listView = view.findViewById(R.id.pack_list)
        listView.adapter = IconAdapter(context!!, prefs.iconPack, prefs.showDebugInfo)
        listView.onItemClickListener = this
        val accent = ColorEngine.getInstance(context!!).accent
        listView.backgroundTintList = ColorStateList.valueOf(accent)
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        parent?.findViewWithTag<View>(pack)?.findViewById<RadioButton>(R.id.radio)?.isChecked = false
        pack = (listView.adapter.getItem(position) as IconPackManager.IconPackInfo).packageName
        view?.findViewById<RadioButton>(R.id.radio)?.isChecked = true
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) prefs.iconPack = pack
    }

    companion object {

        fun newInstance(key: String?) = IconPackDialogFragmentCompat().apply {
            arguments = Bundle(1).apply {
                putString(ARG_KEY, key)
            }
        }
    }

    private class IconAdapter internal constructor(context: Context, internal var current: String, internal var debug: Boolean) : BaseAdapter() {
        internal var packs = IconPackManager.getInstance(context).getPackProviderInfos().values.toMutableList()
        internal var layoutInflater: LayoutInflater = LayoutInflater.from(context)

        init {
            packs = packs.sortedBy { it.label.toString() }.toMutableList()
            val label = context.getString(R.string.iconpack_none)
            packs.add(0, IconPackManager.IconPackInfo("", context.getIcon(), label))
        }

        override fun getItem(position: Int): Any = packs[position]

        override fun getItemId(position: Int): Long = 0

        override fun getCount(): Int = packs.size

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val info = packs[position]
            return (convertView ?: layoutInflater.inflate(R.layout.icon_pack_dialog_item, parent, false)).apply {
                tag = info.packageName
                findViewById<TextView>(android.R.id.title).text = info.label
                findViewById<ImageView>(android.R.id.icon).setImageDrawable(info.icon)
                findViewById<RadioButton>(R.id.radio).apply {
                    isChecked = info.packageName == current
                    buttonTintList = ColorStateList.valueOf(ColorEngine.getInstance(context).accent)
                }
                findViewById<TextView>(android.R.id.text1).apply {
                    text = info.packageName
                    isVisible = debug
                }
            }
        }
    }
}