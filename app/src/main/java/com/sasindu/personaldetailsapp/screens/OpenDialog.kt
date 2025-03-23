import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sasindu.personaldetailsapp.AuthViewModel
import com.sasindu.personaldetailsapp.Expenses
import com.sasindu.personaldetailsapp.MainActivity
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Calendar
import java.util.Locale

@Composable
fun OpenDialog(
    authViewModel: AuthViewModel,
    onDismiss: () -> Unit,
    navController: NavController
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        var monthlyIncome by remember { mutableStateOf("") }
        val courseList = remember { mutableStateListOf<Expenses?>() }
        var expensesList = remember { mutableStateListOf<Expenses?>() }
        val context = LocalContext.current

        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        var startDate by remember { mutableStateOf("") }
        var endDate by remember { mutableStateOf("") }

        val calendar = Calendar.getInstance()

        // Start Date Picker
        val startDatePickerDialog = DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                calendar.set(year, month, dayOfMonth)
                startDate = dateFormatter.format(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // End Date Picker
        val endDatePickerDialog = DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                calendar.set(year, month, dayOfMonth)
                endDate = dateFormatter.format(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )


        Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Monthly Budget Tracker",
                color =if (!isSystemInDarkTheme()) Color(0xFF205781) else Color(0xFFE9762B),
                fontFamily = FontFamily.SansSerif,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )




        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = monthlyIncome,
            onValueChange = { monthlyIncome = it },
            modifier = Modifier.fillMaxWidth(),

            label = { Text(text = "Enter Your Monthly Income") },
            placeholder = { Text(text = "Monthly Income") },
            visualTransformation = VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.CreditCard,
                    contentDescription = "",
                )
            },
            shape = RoundedCornerShape(15.dp)
            )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            textAlign = TextAlign.Center,
            text = "Click the button below to view your monthly expenses categorized by type",
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(15.dp) ,
            onClick = {
                authViewModel.getUserEligibleExpenses(context) { fetchedExpenses ->
                    expensesList.clear()
                    expensesList.addAll(fetchedExpenses)
                }
            }
        ) {
            Text(text = "View Summary", fontSize = 18.sp, modifier = Modifier.padding(8.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row (modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically) {

            Button(
                shape = RoundedCornerShape(15.dp),
                onClick = {
                    startDatePickerDialog.show()
                }
            ) {
              Text(  text = startDate.ifEmpty { "Start Date" },
                  Modifier.padding(10.dp))
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                shape = RoundedCornerShape(15.dp),
                onClick = {
                    endDatePickerDialog.show()
                }
            ) {
                Text(
                    text = when {
                        startDate.isEmpty() -> endDate.ifEmpty { "End Date" }
                        endDate.isNotEmpty() && endDate <= startDate -> "End Date"
                        endDate.isEmpty() ->  "End Date"
                        else -> endDate
                    },
                    modifier = Modifier.padding(10.dp),
                    color = if (endDate.isNotEmpty() && endDate <= startDate) Color.Red else Color.Unspecified // Highlight invalid date
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                modifier = Modifier.wrapContentWidth(),
                shape = RoundedCornerShape(15.dp),
                onClick = {
                    startDate=""
                    endDate=""
                    monthlyIncome=""
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ) {
                Text(text = "Clear",
                    Modifier.padding(top = 10.dp, bottom = 10.dp),
                    color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (!expensesList.isEmpty()) {
            CallCategoryExpensesLazyColumn(monthlyIncome, expensesList,startDate,endDate)
        }


    }
}


@SuppressLint("DefaultLocale")
@Composable
fun CallCategoryExpensesLazyColumn(
    monthlyIncome: String,
    courseList: SnapshotStateList<Expenses?>,
    startDate: String,
    endDate: String
) {

    val numberRegex = Regex("^[0-9]+(\\.[0-9]+)?$")
    val startLocalDate = parseDate(startDate)
    val endLocalDate = parseDate(endDate)

    val filteredExpenses = courseList.filterNotNull().filter { expense ->
        val expenseDate = parseDate(expense.date)

        when {
            expenseDate == null -> false // Skip invalid dates
            startLocalDate == null && endLocalDate == null -> true // No filtering
            startLocalDate == null -> expenseDate <= endLocalDate!!
            endLocalDate == null -> expenseDate >= startLocalDate!!
            else -> expenseDate in startLocalDate..endLocalDate
        }
    }



    val categorySumMap = filteredExpenses.filterNotNull().groupBy { it.category }.mapValues { entry ->
        entry.value.sumOf { it.amount.toDouble() }
    }

    val chartValues: List<Float> = categorySumMap.values.map { it.toFloat() }
    val labels: List<String> = categorySumMap.keys.toList()
    var totalExpenses: Float = 0f
    for (i in chartValues) {
        totalExpenses = (totalExpenses + i);
    }


    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (monthlyIncome.isNotEmpty()
            && monthlyIncome.matches(numberRegex)
            && (monthlyIncome.toFloat() > totalExpenses)
        ) {
            Row(
                Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "Financial Gain : "
                , color =if (!isSystemInDarkTheme()) Color(0xFF205781) else Color.White,
                    fontWeight = FontWeight.Bold)
                Text(
                    text = String.format("%.2f", monthlyIncome.toDouble() - totalExpenses),
                    color = Color(0xFF0b7826),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        } else if (monthlyIncome.isNotEmpty()
            && monthlyIncome.matches(numberRegex)
            && (monthlyIncome.toFloat() < totalExpenses)
        ) {
            Row(
                Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "Financial Decline : ",
                    color =if (!isSystemInDarkTheme()) Color(0xFF205781) else Color.White,
                    fontWeight = FontWeight.Bold)
                Text(
                    text = String.format("%.2f",totalExpenses- monthlyIncome.toDouble()),
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        } else if (monthlyIncome.isNotEmpty()
            && monthlyIncome.matches(numberRegex)
            && (monthlyIncome.toFloat() == totalExpenses)
        ) {
            Row(
                Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "Total Balance: ",
                    color =  if (!isSystemInDarkTheme()) Color(0xFF205781) else Color.White,
                    fontWeight = FontWeight.Bold)
                Text(
                    text = "0",
                    color = Color.Blue,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        LazyColumn {
            items(categorySumMap.entries.toList()) { (category, sum) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = category,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (!isSystemInDarkTheme()) Color(0xFF205781) else Color(0xFFE9762B),
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Image(
                                painter = findDrawable(category),
                                contentDescription = "",
                                modifier = Modifier
                                    .height(25.dp)
                                    .width(25.dp)
                                    .clip(shape = CircleShape)
                            )

                        }

                        Text(
                            text = "$sum",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color =  if (!isSystemInDarkTheme()) Color(0xFF205781) else Color(0xFFE9762B),
                        )
                    }
                }
            }
        }

    }
}

fun parseDate(dateStr: String?): LocalDate? {
    if (dateStr.isNullOrEmpty()) return null

    val formats = listOf(
        DateTimeFormatter.ofPattern("MM/dd/yyyy"), // Example: 10/11/2025
        DateTimeFormatter.ofPattern("yyyy-MM-dd")  // Example: 2025-10-11
    )

    for (formatter in formats) {
        try {
            return LocalDate.parse(dateStr, formatter)
        } catch (e: DateTimeParseException) {
            // Try the next format
        }
    }
    return null // If all formats fail
}