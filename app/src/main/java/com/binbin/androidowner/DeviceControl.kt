package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.os.Build
import android.os.Build.VERSION
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeviceControl(myDpm: DevicePolicyManager, myComponent: ComponentName){
    val wifimac = try {
        myDpm.getWifiMacAddress(myComponent).toString()
    }catch(e:SecurityException){
        "没有权限"
    }
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 20.dp)
    ) {
        DeviceCtrlItem(R.string.disable_cam,R.string.place_holder, myDpm,{myDpm.getCameraDisabled(null)},{b -> myDpm.setCameraDisabled(myComponent,b)})
        DeviceCtrlItem(R.string.disable_scrcap,R.string.aosp_scrrec_also_work,myDpm,{myDpm.getScreenCaptureDisabled(null)},{b -> myDpm.setScreenCaptureDisabled(myComponent,b) })
        if(VERSION.SDK_INT>=34){
            DeviceCtrlItem(R.string.hide_status_bar,R.string.may_hide_notifi_icon_only,myDpm,{myDpm.isStatusBarDisabled},{b -> myDpm.setStatusBarDisabled(myComponent,b) })
        }
        if(VERSION.SDK_INT>=30){
            DeviceCtrlItem(R.string.auto_time,R.string.place_holder,myDpm,{myDpm.getAutoTimeEnabled(myComponent)},{b -> myDpm.setAutoTimeEnabled(myComponent,b) })
            DeviceCtrlItem(R.string.auto_timezone,R.string.place_holder,myDpm,{myDpm.getAutoTimeZoneEnabled(myComponent)},{b -> myDpm.setAutoTimeZoneEnabled(myComponent,b) })
        }
        DeviceCtrlItem(R.string.master_mute,R.string.place_holder,myDpm,{myDpm.isMasterVolumeMuted(myComponent)},{b -> myDpm.setMasterVolumeMuted(myComponent,b) })
        DeviceCtrlItem(R.string.backup_service,R.string.place_holder,myDpm,{myDpm.isBackupServiceEnabled(myComponent)},{b -> myDpm.setBackupServiceEnabled(myComponent,b) })
        Text("隐藏状态栏需要API34")
        Text("自动设置时间和自动设置时区需要API30")
        Button(onClick = {myDpm.reboot(myComponent)}) {
            Text("重启")
        }
        Button(onClick = {myDpm.lockNow()}) {
            Text("锁屏")
        }
        Text("WiFi MAC: $wifimac")
        Text("以下功能需要长按按钮，作者并未测试")
        Button(
            onClick = {},
            modifier = Modifier
                .combinedClickable(onClick = {}, onLongClick = {myDpm.wipeData(0)}),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("WipeData")
        }
        if (VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            Button(
                modifier = Modifier
                    .combinedClickable(onClick = {}, onLongClick = {myDpm.wipeDevice(0)}),
                onClick = {},
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("WipeDevice(API34)")
            }
        }
    }
}

@Composable
private fun DeviceCtrlItem(
    itemName:Int,
    itemDesc:Int,
    myDpm: DevicePolicyManager,
    getMethod:()->Boolean,
    setMethod:(b:Boolean)->Unit
){
    var isEnabled by remember{ mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .clip(RoundedCornerShape(15))
            .background(color = MaterialTheme.colorScheme.primaryContainer)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(itemName),
                style = MaterialTheme.typography.titleLarge
            )
            if(itemDesc!=R.string.place_holder){
                Text(stringResource(itemDesc))
            }
        }
        if(myDpm.isDeviceOwnerApp("com.binbin.androidowner")){
            isEnabled = getMethod()
        }
        Switch(
            checked = isEnabled,
            onCheckedChange = {
                setMethod(!isEnabled)
                isEnabled=getMethod()
            },
            enabled = myDpm.isDeviceOwnerApp("com.binbin.androidowner")
        )
    }
}
