package com.john.cena.lifelog.permission

import android.Manifest
import android.content.Context
import android.widget.Toast

import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission

import java.util.ArrayList

class PermissionManager(internal var context: Context) {

    fun tedPermission(execute: () -> Unit) {
        val permissionlistener = object : PermissionListener {
            override fun onPermissionGranted() {
                execute()
            }

            override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {
                Toast.makeText(context, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show()
            }
        }

        TedPermission.with(context)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(*PermissionManager.permissions)
                .check()
    }

    companion object {
        val permissions = arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)
    }
}
