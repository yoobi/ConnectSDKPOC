package io.yoobi.poc.connectsdkpoc

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.connectsdk.core.MediaInfo
import com.connectsdk.device.ConnectableDevice
import com.connectsdk.device.ConnectableDeviceListener
import com.connectsdk.device.DevicePicker
import com.connectsdk.discovery.DiscoveryManager
import com.connectsdk.service.DeviceService
import com.connectsdk.service.capability.MediaPlayer
import com.connectsdk.service.command.ServiceCommandError
import io.yoobi.poc.connectsdkpoc.databinding.FragmentFirstBinding

class FirstFragment : Fragment(), ConnectableDeviceListener {

    val mediaInfo: MediaInfo = MediaInfo
        .Builder(
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        "video/mp4"
        )
        .setTitle("Big Buck Bunny")
        .setDescription("By Blender Foundation")
        .build()

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private val isConnected = MutableLiveData(false)
    private var mDevice: ConnectableDevice? = null
    private val mDiscoveryManager = DiscoveryManager.getInstance().apply {
//        setCapabilityFilters(
//            CapabilityFilter(MediaPlayer.Play_Video, MediaControl.Any, VolumeControl.Volume_Up_Down)
//        )
    }
    private val selectDevice = AdapterView.OnItemClickListener { adapter, _, position, _ ->
        mDevice = adapter?.getItemAtPosition(position) as? ConnectableDevice
        Log.e("Device", mDevice.toString())
        mDevice?.addListener(this)
        mDevice?.connect()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mDiscoveryManager.start()

        binding.buttonConnect.setOnClickListener {
            if(isConnected.value == false) showImage(requireActivity())
            else mDevice?.disconnect()
        }

        isConnected.observe(viewLifecycleOwner) {
            binding.buttonVideo.isVisible = it
        }

        binding.buttonVideo.setOnClickListener {
            mDevice?.getCapability(MediaPlayer::class.java)
                ?.playMedia(mediaInfo, false, object: MediaPlayer.LaunchListener {
                    override fun onError(error: ServiceCommandError?) {
                        Log.e("MediaPlayer", error.toString())
                    }

                    override fun onSuccess(`object`: MediaPlayer.MediaLaunchObject?) {
                        Log.e("MediaPlayer", "Success -- ${`object`?.launchSession?.appName}")
                    }

                })
        }

    }



    private fun showImage(activity: Activity) {
        val devicePicker = DevicePicker(activity)
        val dialog: AlertDialog = devicePicker.getPickerDialog("Show Image", selectDevice)
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDeviceReady(p0: ConnectableDevice?) {
        _binding?.buttonConnect?.text = "Connected"
        isConnected.value = true
        Log.e("ConnectableDeviceListener", "onDeviceReady $p0")
    }

    override fun onDeviceDisconnected(p0: ConnectableDevice?) {
        _binding?.buttonConnect?.text = "Connect"
        isConnected.value = false
        Log.e("ConnectableDeviceListener", "onDeviceDisconnected $p0")

    }

    override fun onPairingRequired(
        p0: ConnectableDevice?,
        p1: DeviceService?,
        p2: DeviceService.PairingType?
    ) {
        Log.e("ConnectableDeviceListener", "onPairingRequired $p0 -- $p1 -- $p2")
    }

    override fun onCapabilityUpdated(
        p0: ConnectableDevice?,
        p1: MutableList<String>?,
        p2: MutableList<String>?
    ) {
        Log.e("ConnectableDeviceListener", "onCapabilityUpdated $p0 -- $p1 -- $p2")
    }

    override fun onConnectionFailed(p0: ConnectableDevice?, p1: ServiceCommandError?) {
        isConnected.value = false
        Log.e("ConnectableDeviceListener", "onConnectionFailed $p0 -- $p1")

    }
}