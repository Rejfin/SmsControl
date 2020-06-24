package com.rejfin.smscontrol.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.rejfin.smscontrol.R
import com.rejfin.smscontrol.helpers_class.LogManager
import kotlinx.android.synthetic.main.log_viewer_layout.view.*

class LogDataDialog : DialogFragment() {

    private lateinit var toolbar:Toolbar
    private lateinit var tvLogs:TextView
    private val myTAG:String = "DIALOG_LOGS"

    fun showDialog(fm:FragmentManager):LogDataDialog{
        val logDataDialog = LogDataDialog()
        logDataDialog.show(fm,myTAG)
        return logDataDialog
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.setLayout(width, height)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.log_viewer_layout,container,false)
        toolbar = view.toolbar
        tvLogs = view.textView_logs
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setNavigationOnClickListener {
            dismiss()
        }
        toolbar.title = getString(R.string.log)
        toolbar.inflateMenu(R.menu.dialog_menu_logs_blacklist)
        toolbar.setOnMenuItemClickListener {
            if(it.itemId == R.id.item_one){
                if(LogManager.clearLogs(requireContext())){
                    tvLogs.text = getString(R.string.empty_log_file)
                }
            }
            true
        }
        val text = LogManager.readFromLog(requireContext())
        tvLogs.text = HtmlCompat.fromHtml(
            text.replace("[ERROR]","<font color=#D63A3A>[ERROR]</font>").replace("\n","<br/>"),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
    }
}