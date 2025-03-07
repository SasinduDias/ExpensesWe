import android.content.Context
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sasindu.personaldetailsapp.AuthState
import com.sasindu.personaldetailsapp.AuthViewModel
import com.sasindu.personaldetailsapp.MainActivity.Routes
import com.sasindu.personaldetailsapp.R
import com.google.accompanist.drawablepainter.rememberDrawablePainter


@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun SplashScreen(navController: NavController, authViewModel: AuthViewModel) {
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxHeight()
            .fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val animatedDrawable = remember {
            createAnimatedImageDrawableFromImageDecoder(
                context,
                Uri.parse("android.resource://${context.packageName}/${R.drawable.loader}")
            )
        }

        animatedDrawable?.start() // Start animation if drawable is valid

        Image(
            modifier = Modifier
                .height(150.dp)
                .width(150.dp),
            painter = rememberDrawablePainter(drawable = animatedDrawable),
            contentDescription = "animated gif"
        )


        LaunchedEffect(authState.value) {
            when (authState.value) {
                is AuthState.Authenticated -> navController.navigate(Routes.Home.name)
                is AuthState.Error -> navController.navigate(Routes.Home.name)
                else -> navController.navigate(Routes.Home.name)
            }
        }
    }


}


@RequiresApi(Build.VERSION_CODES.P)
private fun createAnimatedImageDrawableFromImageDecoder(
    context: Context,
    uri: Uri
): AnimatedImageDrawable? {
    return try {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        val drawable = ImageDecoder.decodeDrawable(source)
        if (drawable is AnimatedImageDrawable) {
            drawable
        } else {
            null // Return null if the drawable is not an AnimatedImageDrawable
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null // Return null if there's an error
    }
}