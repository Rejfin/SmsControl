package com.rejfin.smscontrol.ui

import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rejfin.smscontrol.R
import com.rejfin.smscontrol.ui.other.BlacklistItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.blacklist_viewer_layout.view.*
import kotlinx.android.synthetic.main.log_viewer_layout.view.toolbar

class BlacklistDialog : DialogFragment() {

    private lateinit var toolbar:Toolbar
    private lateinit var recycle:RecyclerView
    private val adapter = GroupAdapter<GroupieViewHolder>()
    private lateinit var pref:SharedPreferences
    private val myTAG:String = "DIALOG_BLACKLIST"

    fun showDialog(fm:FragmentManager):BlacklistDialog{
        val blacklistDialog = BlacklistDialog()
        blacklistDialog.show(fm,myTAG)
        return blacklistDialog
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
        val view = inflater.inflate(R.layout.blacklist_viewer_layout,container,false)
        toolbar = view.toolbar
        recycle = view.recycle_blacklist
        pref = PreferenceManager.getDefaultSharedPreferences(context)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setNavigationOnClickListener {
            dismiss()
        }
        toolbar.title = getString(R.string.blacklist)
        toolbar.inflateMenu(R.menu.dialog_menu_logs_blacklist)
        toolbar.menu.findItem(R.id.item_one).setIcon(R.drawable.ic_add)

        recycle.layoutManager = LinearLayoutManager(context)
        recycle.addItemDecoration(DividerItemDecoration(context,DividerItemDecoration.VERTICAL))
        recycle.adapter = adapter

        // load saved numbers //
        readFromPreference().forEach {
            val number = it.split(Regex(","))[1]
            val name = it.split(Regex(","))[0]
            adapter.add(BlacklistItem(number,name))
        }

        toolbar.setOnMenuItemClickListener {
            if(it.itemId == R.id.item_one){
                //set inflater and dialog then get field number and name//
                val inflater = layoutInflater.inflate(R.layout.dialog_add_blacklist,(view as ViewGroup),false)
                val nameField = inflater.findViewById<EditText>(R.id.editText_name)
                val numberField = inflater.findViewById<EditText>(R.id.editText_number)

                val dialog = MaterialAlertDialogBuilder(context)
                    .setView(inflater)
                    .setPositiveButton(R.string.add){_,_-> }
                    .setNegativeButton(R.string.cancel){_,_-> dismiss() }
                    .create()

                // override positive button behaviour //
                dialog.setOnShowListener {
                    val buttonPositive = dialog.getButton(Dialog.BUTTON_POSITIVE)
                    buttonPositive.setOnClickListener {
                        if(!numberField.text.isNullOrEmpty() && !nameField.text.isNullOrEmpty()){
                            adapter.add(BlacklistItem(numberField.text.toString(), nameField.text.toString()))
                            saveToPreference(nameField.text.toString(),numberField.text.toString())
                            dialog.dismiss()
                        }else{
                            if(numberField.text.isNullOrEmpty()){
                                numberField.error = getString(R.string.cant_be_empty)
                            }

                            if(nameField.text.isNullOrEmpty())
                            {
                                nameField.error = getString(R.string.cant_be_empty)
                            }
                        }
                    }
                }
                dialog.show()
            }else if(it.itemId == R.id.help_item){
                MaterialAlertDialogBuilder(context)
                    .setMessage(R.string.help_blacklist_message)
                    .setPositiveButton(getString(R.string.understand)){dialog,_->
                        dialog.dismiss()
                    }
                    .show()
            }
            true
        }

        adapter.setOnItemLongClickListener { item, adapterView ->
            val selectedItemId = recycle.getChildAdapterPosition(adapterView)
            val selectedItem = item as BlacklistItem
            MaterialAlertDialogBuilder(activity)
                .setMessage(getString(R.string.warn_message_blacklist,selectedItem.name))
                .setPositiveButton(R.string.yes){_,_->
                    adapter.removeGroupAtAdapterPosition(selectedItemId)
                    removeFromPreference(selectedItem.name,selectedItem.number)
                }
                .setNegativeButton(R.string.cancel){_,_-> }
                .show()
            true
        }
    }

    private fun saveToPreference(name:String,number:String){
        val set = mutableSetOf<String>()
        set.add("$name,$number")
        set.addAll(pref.getStringSet("blacklist", mutableSetOf<String>())!!)
        pref.edit().putStringSet("blacklist",set).apply()
    }

    private fun readFromPreference():MutableSet<String>{
        return pref.getStringSet("blacklist", mutableSetOf<String>())!!
    }

    private fun removeFromPreference(name:String,number:String){
        val set = pref.getStringSet("blacklist",null)
        if(set != null){
            val newSet = mutableSetOf<String>()
            newSet.addAll(set)
            newSet.remove("$name,$number")
            pref.edit().putStringSet("blacklist",newSet).apply()
        }
    }
}