import android.content.Context
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sasindu.personaldetailsapp.AuthViewModel
import com.sasindu.personaldetailsapp.Expenses

@Composable
fun OpenDialog(
    authViewModel: AuthViewModel,
    onDismiss: () -> Unit
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

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Monthly Budget Tracker",
            color = Color.Blue,
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
                    imageVector = Icons.Filled.Money,
                    contentDescription = "",
                )
            },

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

        if (!expensesList.isEmpty()) {
            CallCategoryExpensesLazyColumn(monthlyIncome, expensesList)
        }


    }
}


@Composable
fun CallCategoryExpensesLazyColumn(
    monthlyIncome: String,
    courseList: SnapshotStateList<Expenses?>
) {

    val categorySumMap = courseList.filterNotNull().groupBy { it.category }.mapValues { entry ->
        entry.value.sumOf { it.amount.toInt() }
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

        if (monthlyIncome.isNotEmpty() && monthlyIncome != "."
            && (monthlyIncome.toFloat() > totalExpenses)
        ) {
            Row(
                Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "Financial Gain : ")
                Text(
                    text = (monthlyIncome.toFloat() - totalExpenses).toString(),
                    color = Color.Green,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        } else if (monthlyIncome.isNotEmpty() && monthlyIncome != "."
            && (monthlyIncome.toFloat() < totalExpenses)
        ) {
            Row(
                Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "Financial Decline : ")
                Text(
                    text = (totalExpenses - monthlyIncome.toFloat()).toString(),
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        } else if (monthlyIncome.isNotEmpty() && monthlyIncome != "."
            && (monthlyIncome.toFloat() == totalExpenses)
        ) {
            Row(
                Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "Total Balance: ")
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
                                fontSize = 16.sp
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
                            color = Color.Blue
                        )
                    }
                }
            }
        }

    }
}
