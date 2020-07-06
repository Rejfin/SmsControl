package com.rejfin.smscontrol.ui.other

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceViewHolder
import com.rejfin.smscontrol.R
import kotlinx.android.synthetic.main.preference_command_layout.view.*


class CustomPreferenceItem : EditTextPreference{
    constructor(context: Context) : super(context)

    constructor(context:Context,attrs:AttributeSet,defStyleAttr:Int,defStyleRes:Int) : super(context,attrs,defStyleAttr,defStyleRes)

    constructor(context:Context,attrs:AttributeSet,defStyleAttr:Int) : super(context,attrs,defStyleAttr)

    constructor(context: Context,attrs: AttributeSet) : super(context,attrs)

    private var state : Boolean = false
    private var mListener : OnStateChangeEventListener? = null
    private var holder:PreferenceViewHolder? = null
    private lateinit var itemView : View

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        this.holder = holder
        itemView = holder!!.itemView
        val linear: LinearLayout = holder.findViewById(R.id.linear_layout) as LinearLayout
        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        // prevent entering a space character //
        super.setOnBindEditTextListener {
            it.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s!!.contains(" ")) {
                        it.error = context.getString(R.string.space_not_allowed)
                    } else {
                        it.error = null
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            })
        }

        state = pref.getBoolean(super.getKey() + "_state", false)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit().putBoolean(super.getKey() + "_state", state).apply()

        itemView.checkbox_state.isChecked = state
        for (i in 0 until linear.childCount step 1) {
            linear.getChildAt(i).isEnabled = itemView.checkbox_state.isChecked
        }

        itemView.checkbox_state.setOnClickListener {
            state = itemView.checkbox_state.isChecked
            for (i in 0 until linear.childCount step 1) {
                linear.getChildAt(i).isEnabled = itemView.checkbox_state.isChecked
            }
            pref.edit().putBoolean(super.getKey() + "_state", itemView.checkbox_state.isChecked)
                .apply()
            mListener?.onStateChange()
        }
    }

    fun setState(state:Boolean){
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit().putBoolean(super.getKey()+"_state",state).apply()
        this.state = state
        itemView.checkbox_state.isChecked = state
        val linear:LinearLayout = holder?.findViewById(R.id.linear_layout) as LinearLayout
        for( i in 0 until linear.childCount step 1){
            linear.getChildAt(i).isEnabled = itemView.checkbox_state.isChecked
        }
    }

    fun setAvailability(state:Boolean){
        this.isEnabled = state
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit().putBoolean(super.getKey()+"_state",state).apply()
    }

    interface OnStateChangeEventListener {
        fun onStateChange()
    }

    fun setStateChangeListener(eventListener: OnStateChangeEventListener) {
        mListener = eventListener
    }

    override fun onClick() {
        if(state){
            super.onClick()
        }
    }
}