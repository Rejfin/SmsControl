package com.rejfin.smscontrol

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

    private var component:ComponentName? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home,container,false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // get sms receiver name //
        component = ComponentName(requireContext(),
            SmsBroadcastReceiver::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // set default settings for progress wheel //
        progress_wheel.setInstantProgress(0f)
        progress_wheel.spinSpeed = 0.8f

        // check if app still have permission //
        if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_DENIED){
            requireActivity().packageManager.setComponentEnabledSetting(
                component!!,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP)
        }

        // set view and receiver state when created //
        if(isReceiverRunning(requireContext(),component!!)){
            progress_wheel.progress = 1f
        }else{
            progress_wheel.progress = 0f
        }

        // set listener when clicked //
        imageView_mail.setOnClickListener {
            // check if permissions are granted if not ask for it //
            if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_DENIED){
                MaterialAlertDialogBuilder(context)
                    .setMessage(getString(R.string.sms_permission_info))
                    .setNegativeButton(getString(R.string.cancel)
                    ) { dialog, _ -> dialog?.dismiss() }
                    .setPositiveButton(getString(R.string.ok)
                    ) { _, _ -> requestPermissions(arrayOf(Manifest.permission.RECEIVE_SMS),1) }
                    .show()
            }else{
                setReceiverState(!isReceiverRunning(requireContext(),component!!))
            }
        }

        // set info about receiver state based on progress wheel value //
        progress_wheel.setCallback {
            if(it == 0f){
                textView_status.visibility = View.INVISIBLE
            }else if(it == 1f){
                textView_status.visibility = View.VISIBLE
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            1 -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    setReceiverState(!isReceiverRunning(requireContext(),component!!))
                }else{
                    MaterialAlertDialogBuilder(context)
                        .setMessage(getString(R.string.sms_permission_denied))
                        .setPositiveButton(getString(R.string.understand)
                        ) { dialog, _ -> dialog?.dismiss() }
                        .show()
                }
            }
        }
    }

    // check if receiver is running //
    private fun isReceiverRunning(context: Context, component: ComponentName) : Boolean{
        val status = context.packageManager.getComponentEnabledSetting(component)
        return status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    }

    // set right state for sms receiver //
    private fun setReceiverState(state:Boolean){
        if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED){
            if(state){
                progress_wheel.progress = 1f
                requireActivity().packageManager.setComponentEnabledSetting(
                    component!!,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP)
            }else{
                progress_wheel.progress = 0f
                requireActivity().packageManager.setComponentEnabledSetting(
                    component!!,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP)
            }
        }else{
            progress_wheel.progress = 0f
            requireActivity().packageManager.setComponentEnabledSetting(
                component!!,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP)
        }
    }
}