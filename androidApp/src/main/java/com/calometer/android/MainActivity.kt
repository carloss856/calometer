package com.calometer.android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.calometer.shared.core.StepSnapshot
import com.calometer.shared.features.nutrition.NutritionStore
import com.calometer.shared.features.steps.StepsStore
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val nutritionStore: NutritionStore by inject()
    private val stepsStore: StepsStore by inject()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) stepsStore.refreshToday()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestActivityRecognition()
        stepsStore.refreshToday()
        setContent {
            MaterialTheme { CalometerHome(nutritionStore, stepsStore) }
        }
    }

    private fun requestActivityRecognition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val permissionState = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION,
            )
            if (permissionState != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
    }
}

@Composable
fun CalometerHome(nutritionStore: NutritionStore, stepsStore: StepsStore) {
    val state by nutritionStore.state.collectAsState()
    val stepSnapshot by stepsStore.today.collectAsState()
    var note by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Calometer") }) },
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Registrar comida", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = state.input,
                    onValueChange = nutritionStore::onInputChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Describe tu comida") },
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nota opcional") },
                )
                Button(onClick = { nutritionStore.parse() }, enabled = !state.isLoading) {
                    Text("Analizar con IA")
                }
                state.parsedLines.forEach { line ->
                    val confidenceLabel = if (line.confidence < 0.75) "(Revisar)" else ""
                    Text(
                        text = "• ${line.foodName ?: line.raw} ${line.amount ?: "?"} ${line.unit ?: ""} ${confidenceLabel}",
                        fontWeight = if (line.confidence < 0.75) FontWeight.Bold else FontWeight.Normal,
                    )
                    line.computed?.let {
                        Text("≈ ${it.kcal.toInt()} kcal (${it.protein}P/${it.carbs}C/${it.fat}F)")
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Button(onClick = { nutritionStore.confirmAndSave(note.ifBlank { null }) }) {
                    Text("Confirmar y guardar")
                }
                Spacer(modifier = Modifier.height(24.dp))
                StepsCard(stepSnapshot)
            }
        }
    }
}

@Composable
fun StepsCard(snapshot: StepSnapshot?) {
    Surface(tonalElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Pasos de hoy", style = MaterialTheme.typography.titleMedium)
            Text(text = snapshot?.steps?.toString() ?: "—")
            Text(text = "Fuente: ${snapshot?.source ?: "Sin datos"}")
        }
    }
}
