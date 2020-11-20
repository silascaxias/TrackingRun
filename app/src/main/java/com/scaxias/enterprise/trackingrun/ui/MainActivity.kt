package com.scaxias.enterprise.trackingrun.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.scaxias.enterprise.trackingrun.R
import com.scaxias.enterprise.trackingrun.extensions.gone
import com.scaxias.enterprise.trackingrun.extensions.visible
import com.scaxias.enterprise.trackingrun.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        navigationToTrackingFragmentIfNeeded(intent)
        setupNavController()
    }

    private fun getNavController(): NavController = (supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment)
        .navController

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigationToTrackingFragmentIfNeeded(intent)
    }

    private fun setupNavController() {
        bottomNavigationView.setupWithNavController(getNavController())
        bottomNavigationView.setOnNavigationItemReselectedListener { /* NO-OP */ }
        getNavController().addOnDestinationChangedListener{ _, destination, _ ->
            when(destination.id) {
                R.id.settingsFragment, R.id.runFragment, R.id.statisticsFragment ->
                    bottomNavigationView.visible()
                else -> bottomNavigationView.gone()
            }
        }
    }

    private fun navigationToTrackingFragmentIfNeeded(intent: Intent?) {
        if(intent?.action == ACTION_SHOW_TRACKING_FRAGMENT) {
            getNavController().navigate(R.id.action_global_tracking_fragment)
        }
    }
}