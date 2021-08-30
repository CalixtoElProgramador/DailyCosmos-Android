package com.listocalixto.dailycosmos.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.databinding.ActivitySingleBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SingleActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySingleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_DailyCosmos)
        binding = ActivitySingleBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}