package com.iwanttobeanifbbpro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.iwanttobeanifbbpro.app.ui.IfbbProCoachApp
import com.iwanttobeanifbbpro.app.ui.IfbbProTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IfbbProTheme {
                Surface {
                    IfbbProCoachApp()
                }
            }
        }
    }
}
