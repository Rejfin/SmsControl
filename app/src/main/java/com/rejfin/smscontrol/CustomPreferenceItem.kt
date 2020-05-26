package com.rejfin.smscontrol

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceViewHolder
import kotlinx.android.synthetic.main.preference_command_layout.view.*

class CustomPreferenceItem : EditTextPreference{
    constructor(context: Context) : super(context)

    constructor(context:Context,attrs:AttributeSet,defStyleAttr:Int,defStyleRes:Int) : super(context,attrs,defStyleAttr,defStyleRes)

    constructor(context:Context,attrs:AttributeSet,defStyleAttr:Int) : super(context,attrs,defStyleAttr)

    constructor(context: Context,attrs: AttributeSet) : super(context,attrs)

    private var state : Boolean = false

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)

        val itemView =  holder!!.itemView
        val linear:LinearLayout = holder.findViewById(R.id.linear_layout) as LinearLayout
        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        state = pref.getBoolean(super.getKey()+"_state",false)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit().putBoolean(super.getKey()+"_state",state).apply()

        itemView.checkbox_state.isChecked = state
        itemView.linear_layout.isEnabled = state
        for( i in 0 until linear.childCount step 1){
            linear.getChildAt(i).isEnabled = itemView.checkbox_state.isChecked
        }

        itemView.checkbox_state.setOnClickListener {
            state = itemView.checkbox_state.isChecked
            for( i in 0 until linear.childCount step 1){
                linear.getChildAt(i).isEnabled = itemView.checkbox_state.isChecked
            }
            pref.edit().putBoolean(super.getKey()+"_state",itemView.checkbox_state.isChecked).apply()
        }
    }

    fun setState(state:Boolean, pref:CustomPreferenceItem){
        //TODO MAYBE SET LISTENER ON 'STATE' VARIABLE
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit().putBoolean(super.getKey()+"_state",state).apply()
        pref.isEnabled = state
    }

    override fun onClick() {
        if(state){
            super.onClick()
        }
    }
}