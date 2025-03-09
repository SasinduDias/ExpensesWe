package com.sasindu.personaldetailsapp

import HomeScreen
import OpenDialog
import SettingScreen
import SignInScreen
import SignUpScreen
import SplashScreen
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sasindu.personaldetailsapp.ui.theme.PersonalDetailsAppTheme
import com.sasindu.personaldetailsapp.R


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PersonalDetailsAppTheme {
                MainNavigation()

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @Composable
    fun MainNavigation() {
        val navController = rememberNavController()
        val authViewModel = AuthViewModel()

        LaunchedEffect(Unit) {
            authViewModel.refreshUser()
        }
        NavHost(
            navController,
            startDestination = if (authViewModel.firebaseUser != null)
                Routes.Splash.name
            else Routes.SignIn.name
        )
        {
            composable(route = Routes.SignIn.name) {
                SignInScreen(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }
            composable(route = Routes.Splash.name) {
                SplashScreen(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }
            composable(route = Routes.SignUp.name) {
                SignUpScreen(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }
            composable(route = Routes.Home.name) {
                HomeScreen(
                    authViewModel = authViewModel,
                    navController = navController
                )
            }
            composable(route = Routes.Settings.name) {
                SettingScreen(
                    authViewModel = authViewModel,
                    navController = navController
                )
            }
        }

    }

    enum class Routes(@StringRes val title: Int) {
        SignIn(title = R.string.sign_in_name),
        SignUp(title = R.string.sign_up_name),
        Home(title = R.string.home_name),
        Splash(title = R.string.settings_name),
        Settings(title = R.string.settings_name)
    }
}
