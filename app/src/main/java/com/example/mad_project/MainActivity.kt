package com.example.mad_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
<<<<<<< HEAD:app/src/main/java/com/example/mad_project/MainActivity.kt
import androidx.activity.enableEdgeToEdge
import com.example.mad_project.navigation.AppNavGraph
=======
import com.example.nugget.navigation.AppNavGraph
import com.google.firebase.FirebaseApp
>>>>>>> c1512be (firebase connection):app/src/main/java/com/example/nugget/MainActivity.kt

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppNavGraph()
        }
    }
}
