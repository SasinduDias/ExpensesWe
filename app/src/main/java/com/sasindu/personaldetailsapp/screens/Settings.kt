import android.text.TextUtils
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.core.util.PatternsCompat
import androidx.navigation.NavController
import com.sasindu.personaldetailsapp.AuthState
import com.sasindu.personaldetailsapp.AuthViewModel
import com.sasindu.personaldetailsapp.MainActivity
import com.sasindu.personaldetailsapp.R
import es.dmoral.toasty.Toasty

@Composable
fun SettingScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var selectedEmailAddress by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val networkObserver = remember { NetworkObserver(context) }
    val isConnected by networkObserver.isConnected.observeAsState(false)
    val authState = authViewModel.authState.observeAsState()
    var openAlertDialog by remember { mutableStateOf(false) }
    val emailPattern = PatternsCompat.EMAIL_ADDRESS

    if (authState.value !is AuthState.Authenticated) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.background_four),
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(7.dp),
            )
        }
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxHeight()
            .fillMaxWidth()
    ) {

        if (authState.value is AuthState.Authenticated) {
            Button(modifier = Modifier
                .padding(16.dp)
                .align(Alignment.End),
                onClick = {
                    openAlertDialog = true
                }
            ) {
                Text(text = "Logout")
            }

            if (openAlertDialog) {
                AlertDialogExample(
                    onDismissRequest = { openAlertDialog = false },
                    onConfirmation = {
                        openAlertDialog = false
                        authViewModel.signout()
                        navController.navigate(MainActivity.Routes.SignIn.name) // Navigate after logout
                    },
                    dialogTitle = "Log Out",
                    dialogText = "Are you sure you want to log out?",
                    icon = Icons.Default.Info
                )
            }
        }


        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier
                    .clip(shape = CircleShape)
                    .width(100.dp)
                    .height(100.dp),
                painter = painterResource(R.drawable.logo),
                contentDescription = ""
            )

            Text(
                modifier = Modifier.paddingFromBaseline(top = 50.dp),
                text = "Forgot Your Password?",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )

            Spacer(Modifier.width(20.dp))

            Text(
                text = "Enter your email address and we will send you \n instructions to reset your password",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(10.dp),
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = selectedEmailAddress,
                modifier = Modifier
                    .height(60.dp)
                    .fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                ),
                leadingIcon = {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = "forget password mail"
                    )
                },
                onValueChange = { selectedEmailAddress = it },
                placeholder = { Text("Email") },
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {

                    if (TextUtils.isEmpty(selectedEmailAddress)) {
                        Toasty.warning(
                            context,
                            "Please add your email !",
                            Toast.LENGTH_SHORT,
                            true
                        ).show()
                    } else if(!emailPattern.matcher(selectedEmailAddress).matches()){
                        Toasty.warning(
                            context,
                            "Please add valid email !",
                            Toast.LENGTH_SHORT,
                            true
                        ).show()
                    }
                    else {
                        if (isConnected) {
                            authViewModel.sendEmailVerification(selectedEmailAddress, context)
                        } else {
                            Toasty.warning(
                                context,
                                "No Internet Connection !",
                                Toast.LENGTH_SHORT,
                                true
                            ).show()
                        }

                    }

                },

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // on below line we are adding text for our button
                Text(text = "Continue", fontSize = 18.sp, modifier = Modifier.padding(8.dp))
            }


            if (authState.value !is AuthState.Authenticated) {
                TextButton(
                    modifier = Modifier.paddingFromBaseline(top = 50.dp),
                    onClick = {
                        navController.navigate(MainActivity.Routes.SignIn.name)
                    }
                ) {
                    Text(text = "Back to the login screen", fontSize = 18.sp)
                }
            }

        }

    }
}


@Composable
fun AlertDialogExample(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "Example Icon")
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Cancel")
            }
        }
    )
}