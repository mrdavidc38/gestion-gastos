package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.AppDatabase
import com.example.data.ExpenseRepository
import com.example.ui.ExpenseViewModel
import com.example.ui.ExpenseViewModelFactory
import com.example.ui.GastoPlanApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ExpenseViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ExpenseRepository(
            expenseDao = database.expenseDao(),
            categoryDao = database.categoryDao(),
            budgetDao = database.budgetDao()
        )
        ExpenseViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    GastoPlanApp(viewModel = viewModel)
                }
            }
        }
    }
}
