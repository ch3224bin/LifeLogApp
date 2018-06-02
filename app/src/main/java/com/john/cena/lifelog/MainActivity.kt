package com.john.cena.lifelog

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.john.cena.lifelog.permission.PermissionManager

class MainActivity : AppCompatActivity() {

    private val pm: PermissionManager by lazy {
        PermissionManager(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    public override fun onResume() {
        super.onResume()

        pm.tedPermission {
            startTabbedActivity()
            finish()
        }
    }

    private fun startTabbedActivity() {
        val intent = Intent(this, TabbedActivity::class.java)
        startActivity(intent)
    }
}
