package com.example.glamping

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.glamping.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )
        setContent { GlampingTheme { AppNav() } }
    }
}

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val vm: MainViewModel = viewModel()
    val idArg = listOf(navArgument("id") { type = NavType.IntType })

    NavHost(
        nav, startDestination = "welcome",
        enterTransition = { fadeIn(tween(220)) }, exitTransition = { fadeOut(tween(160)) },
        popEnterTransition = { fadeIn(tween(220)) }, popExitTransition = { fadeOut(tween(160)) }
    ) {
        composable("welcome") { WelcomeScreen { nav.navigate("home") { popUpTo("welcome") { inclusive = true } } } }
        composable("home") { HomeScreen(vm, nav) }
        composable("objects") { ObjectsScreen(vm, nav) }
        composable("object_edit/{id}", idArg) { ObjectFormScreen(vm, nav, it.arguments?.getInt("id") ?: 0) }
        composable("clients") { ClientsScreen(vm, nav) }
        composable("client_edit/{id}", idArg) { ClientFormScreen(vm, nav, it.arguments?.getInt("id") ?: 0) }
        composable("settlements") { SettlementsScreen(vm, nav) }
        composable("settlement_edit/{id}", idArg) { SettlementFormScreen(vm, nav, it.arguments?.getInt("id") ?: 0) }
        composable("services") { ServicesScreen(vm, nav) }
        composable("service_edit/{id}", idArg) { ServiceFormScreen(vm, nav, it.arguments?.getInt("id") ?: 0) }
        composable("categories") { CategoriesScreen(vm, nav) }
        composable("category_edit/{id}", idArg) { CategoryFormScreen(vm, nav, it.arguments?.getInt("id") ?: 0) }
    }
}
