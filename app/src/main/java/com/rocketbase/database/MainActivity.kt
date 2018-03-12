package com.rocketbase.database

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.flamebase.core.Flamebase
import com.flamebase.database.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Flamebase
    }
}
