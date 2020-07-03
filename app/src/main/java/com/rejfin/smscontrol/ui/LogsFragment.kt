package com.rejfin.smscontrol.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rejfin.smscontrol.R
import com.rejfin.smscontrol.helpers_class.LogManager
import kotlinx.android.synthetic.main.log_viewer_layout.*
import kotlinx.android.synthetic.main.log_viewer_layout.view.*

class LogsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.log_viewer_layout,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.inflateMenu(R.menu.dialog_menu_logs_blacklist)
        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        toolbar.setOnMenuItemClickListener {
            if(it.itemId == R.id.item_one){
                MaterialAlertDialogBuilder(requireContext())
                    .setMessage(R.string.clear_logs_warn_message)
                    .setPositiveButton(getString(R.string.yes)){dialog,_->
                        if(LogManager.clearLogs(requireContext())){
                            view.textView_logs.text = getString(R.string.empty_log_file)
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton(getString(R.string.cancel)){_,_->}
                    .show()
            }else if(it.itemId == R.id.help_item){
                MaterialAlertDialogBuilder(requireContext())
                    .setMessage(R.string.help_log_message)
                    .setPositiveButton(getString(R.string.understand)){dialog,_->
                        dialog.dismiss()
                    }
                    .show()
            }
            true
        }
        val text = LogManager.readFromLog(requireContext())
        view.textView_logs.text = HtmlCompat.fromHtml(
            text.replace("[ERROR]","<font color=#D63A3A>[ERROR]</font>").replace("\n","<br/>"),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
    }
}