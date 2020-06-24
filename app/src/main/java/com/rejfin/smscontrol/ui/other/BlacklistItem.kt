package com.rejfin.smscontrol.ui.other

import com.rejfin.smscontrol.R
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.blacklist_item.view.*

class BlacklistItem(val number:String, val name:String) : Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.blacklist_item
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textView_name.text = name
        viewHolder.itemView.textView_number.text = number
    }
}