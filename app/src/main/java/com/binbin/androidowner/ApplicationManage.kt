package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build.VERSION
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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


@Composable
fun ApplicationManage(myDpm:DevicePolicyManager, myComponent:ComponentName,myContext:Context){
    var pkgName by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("以下功能都需要DeviceOwner权限")
        TextField(
            value = pkgName,
            onValueChange = {
                pkgName = it
            },
            label = { Text("包名") }
        )
        val isSuspended = {
            try{
                myDpm.isPackageSuspended(myComponent,pkgName)
            }catch(e:NameNotFoundException){
                false
            }
        }
        AppManageItem(R.string.hide,R.string.isapphidden_desc,myDpm, {myDpm.isApplicationHidden(myComponent,pkgName)},
            {b -> myDpm.setApplicationHidden(myComponent,pkgName,b)})
        AppManageItem(R.string.suspend,R.string.place_holder,myDpm, isSuspended,
            {b -> myDpm.setPackagesSuspended(myComponent, arrayOf(pkgName) ,b)})
        /*AppManageItem(R.string.block_unins,R.string.sometimes_not_avaliable,myDpm, {myDpm.isUninstallBlocked(myComponent,pkgName)},
            {b -> myDpm.setUninstallBlocked(myComponent,pkgName,b)})*/
        Text("因为无法获取某个应用是否防卸载，无法使用开关控制防卸载")
        Row {
            Button(onClick = {myDpm.setUninstallBlocked(myComponent,pkgName,false)}) {
                Text("取消防卸载")
            }
            Spacer(Modifier.padding(horizontal = 2.dp))
            Button(onClick = {myDpm.setUninstallBlocked(myComponent,pkgName,true)}) {
                Text("防卸载")
            }
        }
        if(VERSION.SDK_INT>=30){
            AppManageItem(R.string.user_ctrl_disabled,R.string.user_ctrl_disabled_desc,myDpm, {pkgName in myDpm.getUserControlDisabledPackages(myComponent)},
                {b->myDpm.setUserControlDisabledPackages(myComponent, mutableListOf(if(b){pkgName}else{null}))})
        }
        Spacer(Modifier.padding(5.dp))
    }
}

@Composable
private fun AppManageItem(
    itemName:Int,
    itemDesc:Int,
    myDpm: DevicePolicyManager,
    getMethod:()->Boolean,
    setMethod:(b:Boolean)->Unit
){
    var isEnabled by remember{ mutableStateOf(false) }
    if(myDpm.isDeviceOwnerApp("com.binbin.androidowner")){
        isEnabled = getMethod()
    }
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
        Switch(
            checked = isEnabled,
            onCheckedChange = {
                setMethod(!isEnabled)
                isEnabled = getMethod()
            },
            enabled = myDpm.isDeviceOwnerApp("com.binbin.androidowner")
        )
    }
}
