/*
 * Copyright (c) 2019 Hemanth Savarala.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by
 *  the Free Software Foundation either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package code.name.monkey.retromusic.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.activities.MainActivity
import code.name.monkey.retromusic.extensions.hide
import code.name.monkey.retromusic.helper.MusicPlayerRemote
import code.name.monkey.retromusic.util.NavigationUtil
import code.name.monkey.retromusic.util.PreferenceUtil
import code.name.monkey.retromusic.views.OptionMenuItemView
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView

class OptionsSheetDialogFragment : DialogFragment(), View.OnClickListener {

    override fun onClick(view: View) {
        val mainActivity = activity as MainActivity? ?: return
        when (view.id) {
            R.id.actionSettings -> NavigationUtil.goToSettings(mainActivity)
            R.id.actionDriveMode -> NavigationUtil.gotoDriveMode(mainActivity)
            R.id.actionRate -> NavigationUtil.goToPlayStore(mainActivity)
        }
        materialDialog.dismiss()
    }

    private lateinit var actionSettings: OptionMenuItemView
    private lateinit var actionRate: OptionMenuItemView
    private lateinit var actionDriveMode: OptionMenuItemView
    private lateinit var materialDialog: MaterialDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val layout = LayoutInflater.from(context).inflate(R.layout.fragment_main_options, null)
        actionSettings = layout.findViewById(R.id.actionSettings)
        actionRate = layout.findViewById(R.id.actionRate)
        actionDriveMode = layout.findViewById(R.id.actionDriveMode)

        actionSettings.setOnClickListener(this)
        actionRate.setOnClickListener(this)
        actionDriveMode.setOnClickListener(this)

        if (MusicPlayerRemote.playingQueue.isEmpty()) {
            actionDriveMode.hide()
        }

        materialDialog = MaterialDialog(requireActivity(), BottomSheet(LayoutMode.WRAP_CONTENT))
            .show {
                icon(R.mipmap.ic_launcher_round)
                title(R.string.app_name)
                customView(view = layout, scrollable = true)
                cornerRadius(PreferenceUtil.getInstance(requireContext()).dialogCorner)
            }
        return materialDialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.let {
            (requireActivity() as MainActivity).setNavigationbarColorAuto()
            (requireActivity() as MainActivity).setLightNavigationBar(true)
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    companion object {

        private const val WHICH_ONE = "which_one"
        @JvmField
        var LIBRARY: Int = 0
        @JvmField
        var FOLDER: Int = 1

        @JvmStatic
        fun newInstance(selectedId: Int): OptionsSheetDialogFragment {
            val bundle = Bundle()
            bundle.putInt(WHICH_ONE, selectedId)
            val fragment = OptionsSheetDialogFragment()
            fragment.arguments = bundle
            return fragment
        }

        @JvmStatic
        fun newInstance(): OptionsSheetDialogFragment {
            return OptionsSheetDialogFragment()
        }
    }
}
