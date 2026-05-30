package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.EarningRepository
import com.example.ui.screens.EarningHubApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.EarningViewModel
import com.example.viewmodel.EarningViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = EarningRepository(database.transactionDao())
        val viewModelFactory = EarningViewModelFactory(repository)

        setContent {
            MyApplicationTheme {
                val viewModel: EarningViewModel = viewModel(factory = viewModelFactory)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EarningHubApp(viewModel = viewModel)
                }
            }
        }
    }
}
