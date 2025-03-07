import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.navigation.NavController
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.sasindu.personaldetailsapp.AuthState
import com.sasindu.personaldetailsapp.AuthViewModel
import com.sasindu.personaldetailsapp.Expenses
import com.sasindu.personaldetailsapp.MainActivity
import com.sasindu.personaldetailsapp.R
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min


@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    // Redirect to Sign-In screen if user is not authenticated
    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Unauthenticated) {
            navController.navigate(MainActivity.Routes.SignIn.name)
        }
    }

    var selectedItem by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedItemIndex = selectedItem,
                onItemSelected = { selectedItem = it }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (selectedItem) {
                0 -> HomeContent(authViewModel)
                1 -> SummaryContent()
                2 -> SettingContent(authViewModel, navController)
            }
        }
    }
}

@Composable
fun HomeContent(authViewModel: AuthViewModel) {
    var expenseName by rememberSaveable { mutableStateOf("") }
    var expenseAmount by rememberSaveable { mutableStateOf("") }
    var selectedDateText by rememberSaveable { mutableStateOf("") }
    var selectedCategoryText by rememberSaveable { mutableStateOf("") }
    val contextHome = LocalContext.current

    Column(
        modifier = Modifier
            .padding(16.dp, 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        //note image
        Image(
            painter = painterResource(R.drawable.background_two),
            contentDescription = "background image",
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        //space
        Spacer(modifier = Modifier.height(10.dp))

        //expense name
        ExpensesTextField(
            modifier = Modifier.fillMaxWidth(),
            value = expenseName,
            label = { Text(text = "Expense Name") },
            onValueChange = { expenseName = it },
            placeholder = { Text(text = "Please enter expense") },
            trailingIcon = {
                Icon(Icons.Default.DriveFileRenameOutline, contentDescription = "expenses")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        //space
        Spacer(modifier = Modifier.height(10.dp))

        //expense amount
        ExpensesTextField(
            modifier = Modifier.fillMaxWidth(),
            value = expenseAmount,
            label = { Text(text = "Expense Amount") },
            onValueChange = { expenseAmount = it },
            placeholder = { Text(text = "Please enter expense amount") },
            trailingIcon = {
                Icon(Icons.Default.CreditCard, contentDescription = "Amount")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            )
        )

        //space
        Spacer(modifier = Modifier.height(10.dp))

        //date picker
        DatePickerFieldToModal(
            selectedDateText = selectedDateText,
            onDateTextChange = { newDate -> selectedDateText = newDate }
        )

        //space
        Spacer(modifier = Modifier.height(10.dp))

        //dropdown
        DropDown(
            selectedCategoryText = selectedCategoryText,
            onCategorySelected = { selectedCategoryText = it }
        )

        //add button
        Button(
            onClick = {
                if (TextUtils.isEmpty(expenseName)) {
                    Toasty.warning(
                        contextHome,
                        "Please enter expense name !",
                        Toast.LENGTH_SHORT,
                        true
                    ).show()
                } else if (TextUtils.isEmpty(expenseAmount)) {
                    Toasty.warning(
                        contextHome,
                        "FPlease enter expense amount !",
                        Toast.LENGTH_SHORT,
                        true
                    ).show()
                } else if (TextUtils.isEmpty(selectedDateText)) {
                    Toasty.warning(contextHome, "Please select a date !", Toast.LENGTH_SHORT, true)
                        .show()
                } else if (TextUtils.isEmpty(selectedCategoryText)) {
                    Toasty.warning(
                        contextHome,
                        "Please select a category !",
                        Toast.LENGTH_SHORT,
                        true
                    ).show()

                } else {
                    // on below line adding data to
                    // firebase firestore database.
                    addDataToFirebase(
                        expenseName,
                        expenseAmount,
                        selectedDateText,
                        selectedCategoryText,
                        contextHome,
                        authViewModel
                    ) {
                        // ✅ Reset fields after saving
                        expenseName = ""
                        expenseAmount = ""
                        selectedDateText = ""
                        selectedCategoryText = ""
                    }
                }
            },

            // adding modifier to button.
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // on below line we are adding text for our button
            Text(text = "Add Expense", modifier = Modifier.padding(8.dp), fontSize = 18.sp)
        }
    }
}

// add data to fire store
fun addDataToFirebase(
    expensesName: String,
    expensesAmount: String,
    selectedDateText: String,
    selectedCategoryText: String,
    context: Context,
    authViewModel: AuthViewModel,
    onSuccess: () -> Unit
) {
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    val dbCourses: CollectionReference = db.collection("Expenses")

    authViewModel.refreshUser()

    val courses = Expenses(
        expensesAmount,
        authViewModel.firebaseUser?.email.toString(),
        expensesName,
        selectedDateText,
        selectedCategoryText
    )

    dbCourses.add(courses).addOnSuccessListener { documentReference ->
        val documentId = documentReference.id

        documentReference.update("documentReference", documentId)
            .addOnSuccessListener {
                Toasty.success(
                    context,
                    "Expenses details has been added !",
                    Toast.LENGTH_SHORT,
                    true
                )
                    .show()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error updating document ID: $e")
            }


        //set empty string to itext fields
        onSuccess()

    }.addOnFailureListener { e ->
        Toasty.error(context, "Fail to add expenses details !", Toast.LENGTH_SHORT, true).show()
    }
}

@Composable
fun DatePickerFieldToModal(
    selectedDateText: String,
    onDateTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var showModal by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedDate?.let { convertMillisToDate(it) } ?: selectedDateText,
        onValueChange = { },
        label = { Text("Date") },
        placeholder = { Text("MM/DD/YYYY") },
        trailingIcon = {
            Icon(Icons.Default.DateRange, contentDescription = "Select date")
        },
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    if (upEvent != null) {
                        showModal = true
                    }
                }
            }
    )

    if (showModal) {
        DatePickerModal(
            onDateSelected = { millis ->
                selectedDate = millis
                onDateTextChange(convertMillisToDate(millis!!))
                showModal = false
            },
            onDismiss = { showModal = false }
        )
    }
}

fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}


@Composable
fun ExpensesTextField(
    modifier: Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    label: @Composable (() -> Unit)? = null,
) {

    OutlinedTextField(
        modifier = modifier,
        value = value,
        keyboardOptions = keyboardOptions,
        leadingIcon = leadingIcon,
        onValueChange = onValueChange,
        trailingIcon = trailingIcon,
        placeholder = placeholder,
        label = label,
    )
}

@Composable
fun SettingContent(authViewModel: AuthViewModel, navController: NavController) {
    SettingScreen(navController, authViewModel)
}

@Composable
fun SummaryContent() {
    val authViewModelForUser = AuthViewModel()
    var barChartSelected by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .padding(16.dp, 16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Expenses Summary",
            color = Color.Blue,
            fontFamily = FontFamily.SansSerif,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        val courseList = remember { mutableStateListOf<Expenses?>() }
        val context = LocalContext.current

        LaunchedEffect(Unit) {
            val db = FirebaseFirestore.getInstance()

            db.collection("Expenses")
                .whereEqualTo("email", authViewModelForUser.firebaseUser?.email.toString())
                .get()
                .addOnSuccessListener { queryDocumentSnapshots ->
                    if (!queryDocumentSnapshots.isEmpty) {
                        val list = queryDocumentSnapshots.documents
                        courseList.clear()
                        for (d in list) {
                            val expense: Expenses? = d.toObject(Expenses::class.java)
                            expense?.let { courseList.add(it) }
                        }
                    } else {
                        Toasty.warning(context, "No data found !", Toast.LENGTH_SHORT, true).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toasty.error(context, "Failed to get data !", Toast.LENGTH_SHORT, true).show()
                }
        }

        LaunchedEffect(barChartSelected, courseList) {

        }
        if (!courseList.isEmpty()) {

            //chart selected buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = {
                        barChartSelected = true
                    }
                ) {
                    when (barChartSelected) {
                        true -> Icon(
                            imageVector = Icons.Filled.BarChart,
                            contentDescription = "Bar Chart",
                            tint = Color.Blue
                        )

                        else -> Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Bar Chart",
                            tint = Color.Black
                        )
                    }
                }

                Spacer(Modifier.width(10.dp))

                IconButton(
                    onClick = {
                        barChartSelected = false
                    }
                ) {
                    when (barChartSelected) {
                        true -> Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = "Pie Chart",
                            tint = Color.Black
                        )

                        else -> Icon(
                            imageVector = Icons.Filled.PieChart,
                            contentDescription = "Pie Chart",
                            tint = Color.Blue
                        )
                    }

                }
            }



            when (barChartSelected) {
                true -> SetupBarChart(courseList)
                else -> SetupGraphs(courseList)
            }
            firebaseUI(LocalContext.current, courseList)
        }
    }

}


@Composable
fun SetupGraphs(courseList: SnapshotStateList<Expenses?>) {

    val chartColors = listOf(

        Color(0xFF2196F3), // Blue
        Color(0xFFFF9800), // Orange
        Color(0xFF4CAF50), // Green
        Color(0xFFFFC107), // Amber
        Color(0xFFE91E63), // Pink
        Color(0xFF9C27B0), // Purple
        Color(0xFF673AB7), // Deep Purple
        Color(0xFFFF5722), // Deep Orange
        Color(0xFF009688)  // Teal
    )

    val categorySumMap = courseList.filterNotNull().groupBy { it.category }.mapValues { entry ->
        entry.value.sumOf { it.amount.toInt() }
    }

    val chartValues: List<Int> = categorySumMap.values.map { it.toInt() }


    PieChart(
        modifier = Modifier.padding(20.dp),
        colors = chartColors.take(chartValues.size),
        inputValues = chartValues.map { it.toFloat() },
        textColor = MaterialTheme.colorScheme.primary
    )

}


@Composable
fun firebaseUI(context: Context, courseList: SnapshotStateList<Expenses?>) {
    val coroutineScope = rememberCoroutineScope()
    var openAlertDialog by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        LazyColumn {
            itemsIndexed(courseList) { index, item ->
                Card(
                    onClick = {

                    },

                    modifier = Modifier.padding(8.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    ) {

                        courseList[index]?.description?.let {

                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {

                                Row(
                                    modifier = Modifier.wrapContentWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {

                                    Text(
                                        text = it,
                                        modifier = Modifier.padding(4.dp),
                                        color = Color.Black,
                                        textAlign = TextAlign.Center,
                                        style = TextStyle(
                                            fontSize = 20.sp, fontWeight = FontWeight.Bold
                                        )
                                    )

                                    Image(
                                        painter = findDrawable(courseList[index]?.category),
                                        contentDescription = "",
                                        modifier = Modifier
                                            .height(20.dp)
                                            .width(20.dp)
                                            .clip(shape = CircleShape)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        openAlertDialog = true
                                    },
                                )
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.Blue
                                    )

                                    if (openAlertDialog) {
                                        AlertDialogExample(
                                            onDismissRequest = { openAlertDialog = false },
                                            onConfirmation = {
                                                openAlertDialog = false
                                                coroutineScope.launch {
                                                    DeleteItem(
                                                        courseList,
                                                        courseList[index],
                                                        context
                                                    )
                                                }
                                            },
                                            dialogTitle = "Warning",
                                            dialogText = "Are you sure you want to remove this item?",
                                            icon = Icons.Default.Warning
                                        )
                                    }
                                }
                            }


                        }

                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = "Expense Category - ",
                                modifier = Modifier.padding(4.dp),
                                color = Color.Blue,
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    fontSize = 15.sp
                                )
                            )

                            courseList[index]?.category?.let {
                                Text(
                                    text = it,
                                    modifier = Modifier.padding(4.dp),
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    style = TextStyle(
                                        fontSize = 15.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(5.dp))

                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = "Expense Amount - ",
                                modifier = Modifier.padding(4.dp),
                                color = Color.Blue,
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    fontSize = 15.sp
                                )
                            )

                            courseList[index]?.amount?.let {
                                Text(
                                    text = it + " LKR",
                                    modifier = Modifier.padding(4.dp),
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    style = TextStyle(fontSize = 15.sp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(5.dp))

                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = "Expense Date - ",
                                modifier = Modifier.padding(4.dp),
                                color = Color.Blue,
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    fontSize = 15.sp
                                )
                            )

                            courseList[index]?.date?.let {
                                Text(
                                    text = it,
                                    modifier = Modifier.padding(4.dp),
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    style = TextStyle(fontSize = 15.sp)
                                )
                            }
                        }

                    }
                }
            }
        }
    }
}

@Composable
fun findDrawable(category: String?): Painter {

    when (category) {

        "Food" -> return painterResource(R.drawable.background_tree)
        "Transport" -> return painterResource(R.drawable.background_tree)
        "Bank Charges" -> return painterResource(R.drawable.background_tree)
        "Insurance" -> return painterResource(R.drawable.background_tree)
        "Rent" -> return painterResource(R.drawable.background_tree)
        "Bills" -> return painterResource(R.drawable.background_tree)
        "Repairs" -> return painterResource(R.drawable.background_tree)
        "Other" -> return painterResource(R.drawable.background_tree)
        else -> return painterResource(R.drawable.logo)

    }


}


suspend fun DeleteItem(
    courseList: SnapshotStateList<Expenses?>,
    expenses: Expenses?,
    context: Context
) {

    val db = FirebaseFirestore.getInstance()

    expenses?.documentReference?.let { documentReference ->

        db.collection("Expenses")
            .document(documentReference)
            .delete()
            .addOnSuccessListener {

                Toasty.success(context, "Expense successfully deleted!", Toast.LENGTH_SHORT)
                    .show()
                courseList.remove(expenses)
            }
            .addOnFailureListener { e ->

                Toasty.error(
                    context,
                    "Failed to delete expense: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    } ?: run {

        Toasty.error(context, "Expense data is missing or invalid.", Toast.LENGTH_SHORT).show()
    }


}


@Composable
fun DropDown(
    selectedCategoryText: String,
    onCategorySelected: (String) -> Unit
) {

    var mExpanded by remember { mutableStateOf(false) }

    val expensesCategories = listOf(
        "Food",
        "Transport",
        "Bank Charges",
        "Insurance",
        "Rent",
        "Bills",
        "Repairs",
        "Other"
    )

    var mTextFieldSize by remember { mutableStateOf(Size.Zero) }

    val icon = if (mExpanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            readOnly = true,
            value = selectedCategoryText,
            onValueChange = { },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    mTextFieldSize = coordinates.size.toSize()
                },
            label = { Text("Category") },
            trailingIcon = {
                Icon(icon, "contentDescription",
                    Modifier.clickable { mExpanded = !mExpanded })
            }
        )

        DropdownMenu(
            expanded = mExpanded,
            onDismissRequest = { mExpanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current) { mTextFieldSize.width.toDp() })
        ) {
            expensesCategories.forEach { label ->
                DropdownMenuItem(
                    onClick = {
                        onCategorySelected(label)
                        mExpanded = false
                    },
                    text = { Text(text = label) },
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    containerColor: Color = NavigationBarDefaults.containerColor,
    contentColor: Color = MaterialTheme.colorScheme.contentColorFor(containerColor),
    tonalElevation: Dp = NavigationBarDefaults.Elevation,
    windowInsets: WindowInsets = NavigationBarDefaults.windowInsets,
    selectedItemIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    NavigationBar(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        windowInsets = windowInsets
    ) {
        val items = listOf("Home", "Summary", "Settings")
        val selectedIcons = listOf(Icons.Filled.Home, Icons.Filled.NoteAlt, Icons.Filled.Settings)
        val unselectedIcons =
            listOf(Icons.Outlined.Home, Icons.Outlined.NoteAlt, Icons.Outlined.Settings)

        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selectedItemIndex == index) selectedIcons[index] else unselectedIcons[index],
                        contentDescription = item
                    )
                },
                label = { Text(item) },
                selected = selectedItemIndex == index,
                onClick = { onItemSelected(index) }
            )
        }
    }
}

@Composable
internal fun PieChart(
    modifier: Modifier = Modifier,
    colors: List<Color>,
    inputValues: List<Float>,
    textColor: Color = MaterialTheme.colorScheme.primary,
    animated: Boolean = true,
    enableClickInfo: Boolean = true
) {

    val chartDegrees = 360f // circle shape

    // start drawing clockwise (top to right)
    var startAngle = 270f

    // calculate each input percentage
    val proportions = inputValues.map {
        it * 100 / inputValues.sum()
    }

    // calculate each input slice degrees
    val angleProgress = proportions.map { prop ->
        chartDegrees * prop / 100
    }

    // clicked slice index
//    var clickedItemIndex by remember {
//        mutableStateOf(emptyIndex)
//    }

    // calculate each slice end point in degrees, for handling click position
    val progressSize = mutableListOf<Float>()

    LaunchedEffect(angleProgress) {
        progressSize.add(angleProgress.first())
        for (x in 1 until angleProgress.size) {
            progressSize.add(angleProgress[x] + progressSize[x - 1])
        }
    }

    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {

        val canvasSize = min(constraints.maxWidth, constraints.maxHeight)
        val size = Size(canvasSize.toFloat(), canvasSize.toFloat())
        val canvasSizeDp = with(LocalDensity.current) { canvasSize.toDp() }

        Canvas(modifier = Modifier.size(canvasSizeDp)) {

            angleProgress.forEachIndexed { index, angle ->
                drawArc(
                    color = colors[index],
                    startAngle = startAngle,
                    sweepAngle = angle,
                    useCenter = true,
                    size = size,
                    // style = PageSize.Fill
                )
                startAngle += angle
            }

        }

    }

}

@Composable
fun BarChart(
    modifier: Modifier = Modifier,
    colors: List<Color>,
    inputValues: List<Float>,
    labels: List<String>,
    textColor: Color = MaterialTheme.colorScheme.primary
) {
    val maxInputValue = inputValues.maxOrNull() ?: 1f

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val barWidth = size.width / (inputValues.size * 2)
            val maxHeight = size.height

            inputValues.forEachIndexed { index, value ->
                val barHeight = (value / maxInputValue) * maxHeight
                drawRect(
                    color = colors.getOrElse(index) { Color.Gray },
                    topLeft = Offset(
                        x = index * barWidth * 2 + barWidth / 2,
                        y = maxHeight - barHeight
                    ),
                    size = Size(barWidth, barHeight)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            labels.forEachIndexed { index, label ->
                Text(text = label, color = textColor, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SetupBarChart(courseList: SnapshotStateList<Expenses?>) {

    val chartColors = listOf(
        Color(0xFF2196F3), // Blue
        Color(0xFFFF9800), // Orange
        Color(0xFF4CAF50), // Green
        Color(0xFFFFC107), // Amber
        Color(0xFFE91E63), // Pink
        Color(0xFF9C27B0), // Purple
        Color(0xFF673AB7), // Deep Purple
        Color(0xFFFF5722), // Deep Orange
        Color(0xFF009688)  // Teal
    )

    val categorySumMap = courseList.filterNotNull().groupBy { it.category }.mapValues { entry ->
        entry.value.sumOf { it.amount.toInt() }
    }

    val chartValues: List<Float> = categorySumMap.values.map { it.toFloat() }
    val labels: List<String> = categorySumMap.keys.toList()

    BarChart(
        modifier = Modifier.padding(20.dp),
        colors = chartColors.take(chartValues.size),
        inputValues = chartValues,
        labels = labels,
        textColor = MaterialTheme.colorScheme.primary
    )
}


@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {

}
