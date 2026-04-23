package com.example.expensetracker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

val PrimaryColor = Color(0xFF6A4C93)
val SecondaryColor = Color(0xFFD4AF37)
val BackgroundColor = Color(0xFF1A1A1A)
val AccentColor = Color(0xFFC0C0C0)
val SurfaceColor = Color(0xFF2A2A2A)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFC0C0C0)

private val expenseTrackerColorScheme = darkColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    tertiary = AccentColor,
    background = BackgroundColor,
    surface = SurfaceColor,
    onPrimary = TextPrimary,
    onSecondary = BackgroundColor,
    onTertiary = BackgroundColor,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = expenseTrackerColorScheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ExpenseTrackerApp()
                }
            }
        }
    }
}

sealed class Screen {
    object AddExpense : Screen()
    object ExpenseList : Screen()
    data class EditExpense(val expenseId: Long) : Screen()
}

@Composable
fun ExpenseTrackerApp(viewModel: ExpenseViewModel = viewModel()) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.AddExpense) }
    var selectedExpenseId by remember { mutableStateOf<Long?>(null) }

    when (currentScreen) {
        is Screen.AddExpense -> {
            AddExpenseScreen(
                onNavigateToList = { currentScreen = Screen.ExpenseList },
                viewModel = viewModel
            )
        }
        is Screen.ExpenseList -> {
            ExpenseListScreen(
                onNavigateBack = { currentScreen = Screen.AddExpense },
                onEditExpense = { id ->
                    selectedExpenseId = id
                    currentScreen = Screen.EditExpense(id)
                },
                viewModel = viewModel
            )
        }
        is Screen.EditExpense -> {
            selectedExpenseId?.let { id ->
                EditExpenseScreen(
                    expenseId = id,
                    onNavigateBack = { currentScreen = Screen.ExpenseList },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun AddExpenseScreen(
    onNavigateToList: () -> Unit,
    viewModel: ExpenseViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var time by remember { mutableStateOf(getCurrentDate()) }
    var purpose by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "添加开销记录",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("时间 (yyyy-MM-dd)", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryColor,
                unfocusedBorderColor = AccentColor
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = purpose,
            onValueChange = { purpose = it },
            label = { Text("用途", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryColor,
                unfocusedBorderColor = AccentColor
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("总额", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryColor,
                unfocusedBorderColor = AccentColor
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                when {
                    time.isBlank() -> {
                        Toast.makeText(context, "请输入时间", Toast.LENGTH_SHORT).show()
                    }
                    purpose.isBlank() -> {
                        Toast.makeText(context, "请输入用途", Toast.LENGTH_SHORT).show()
                    }
                    amount.isBlank() -> {
                        Toast.makeText(context, "请输入总额", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        try {
                            val amountValue = amount.toDouble()
                            scope.launch {
                                viewModel.insertExpense(
                                    Expense(
                                        time = time,
                                        purpose = purpose,
                                        amount = amountValue
                                    )
                                )
                                Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
                                purpose = ""
                                amount = ""
                                time = getCurrentDate()
                            }
                        } catch (e: NumberFormatException) {
                            Toast.makeText(context, "请输入有效的金额", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
        ) {
            Text("确定", color = TextPrimary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNavigateToList,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = SecondaryColor)
        ) {
            Text("查看所有记录", color = BackgroundColor)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "自动结算",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    viewModel.performWeeklySettlement()
                    Toast.makeText(context, "周结算已完成", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text("周结算", color = TextPrimary)
            }

            Button(
                onClick = {
                    viewModel.performMonthlySettlement()
                    Toast.makeText(context, "月结算已完成", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text("月结算", color = TextPrimary)
            }
        }
    }
}

@Composable
fun ExpenseListScreen(
    onNavigateBack: () -> Unit,
    onEditExpense: (Long) -> Unit,
    viewModel: ExpenseViewModel = viewModel()
) {
    val expenses by viewModel.allExpenses.collectAsState(initial = emptyList())
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var filterType by remember { mutableStateOf<String?>(null) }
    var startDate by remember { mutableStateOf(getCurrentDate()) }
    var endDate by remember { mutableStateOf(getCurrentDate()) }
    var totalAmount by remember { mutableStateOf(0.0) }
    var showTotal by remember { mutableStateOf(false) }

    val filteredExpenses = remember(filterType, expenses) {
        when (filterType) {
            null -> expenses
            "weekly" -> expenses.filter { it.settlementType == "weekly" }
            "monthly" -> expenses.filter { it.settlementType == "monthly" }
            "expense" -> expenses.filter { !it.isSettlement }
            else -> expenses
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "所有记录",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterType == null,
                onClick = { filterType = null },
                label = { Text("全部", color = if (filterType == null) TextPrimary else TextSecondary) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryColor,
                    containerColor = SurfaceColor
                )
            )
            FilterChip(
                selected = filterType == "weekly",
                onClick = { filterType = "weekly" },
                label = { Text("周结算", color = if (filterType == "weekly") TextPrimary else TextSecondary) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryColor,
                    containerColor = SurfaceColor
                )
            )
            FilterChip(
                selected = filterType == "monthly",
                onClick = { filterType = "monthly" },
                label = { Text("月结算", color = if (filterType == "monthly") TextPrimary else TextSecondary) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryColor,
                    containerColor = SurfaceColor
                )
            )
            FilterChip(
                selected = filterType == "expense",
                onClick = { filterType = "expense" },
                label = { Text("普通支出", color = if (filterType == "expense") TextPrimary else TextSecondary) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryColor,
                    containerColor = SurfaceColor
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("日期范围统计", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("开始日期", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("yyyy-MM-dd", color = TextSecondary) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryColor,
                                unfocusedBorderColor = AccentColor
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text("结束日期", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("yyyy-MM-dd", color = TextSecondary) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryColor,
                                unfocusedBorderColor = AccentColor
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        scope.launch {
                            val filtered = expenses.filter { 
                                !it.isSettlement && 
                                it.time >= startDate && 
                                it.time <= endDate 
                            }
                            totalAmount = filtered.sumOf { it.amount }
                            showTotal = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryColor)
                ) {
                    Text("统计此日期范围", color = BackgroundColor)
                }
                
                if (showTotal) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "总支出: ¥${String.format(Locale.getDefault(), "%.2f", totalAmount)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredExpenses.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无记录", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredExpenses) { expense ->
                    ExpenseItem(
                        expense = expense,
                        onEdit = { onEditExpense(expense.id) },
                        onDelete = {
                            viewModel.deleteExpense(expense)
                            Toast.makeText(context, "已删除记录", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseItem(
    expense: Expense,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (expense.isSettlement) 
                PrimaryColor.copy(alpha = 0.3f)
            else 
                SurfaceColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = expense.purpose,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "时间: ${expense.time}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Text(
                    text = "¥${String.format(Locale.getDefault(), "%.2f", expense.amount)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (expense.isSettlement) SecondaryColor else TextPrimary
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "编辑", tint = AccentColor)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "删除", tint = AccentColor)
                }
            }
        }
    }
}

@Composable
fun EditExpenseScreen(
    expenseId: Long,
    onNavigateBack: () -> Unit,
    viewModel: ExpenseViewModel = viewModel()
) {
    val expense by viewModel.getExpenseById(expenseId).collectAsState(initial = null)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    expense?.let { currentExpense ->
        var time by remember { mutableStateOf(currentExpense.time) }
        var purpose by remember { mutableStateOf(currentExpense.purpose) }
        var amount by remember { mutableStateOf(currentExpense.amount.toString()) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "编辑记录",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("时间 (yyyy-MM-dd)", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = AccentColor
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = purpose,
                onValueChange = { purpose = it },
                label = { Text("用途", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = AccentColor
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("总额", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = AccentColor
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    when {
                        time.isBlank() -> {
                            Toast.makeText(context, "请输入时间", Toast.LENGTH_SHORT).show()
                        }
                        purpose.isBlank() -> {
                            Toast.makeText(context, "请输入用途", Toast.LENGTH_SHORT).show()
                        }
                        amount.isBlank() -> {
                            Toast.makeText(context, "请输入总额", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            try {
                                val amountValue = amount.toDouble()
                                scope.launch {
                                    viewModel.updateExpense(
                                        currentExpense.copy(
                                            time = time,
                                            purpose = purpose,
                                            amount = amountValue
                                        )
                                    )
                                    Toast.makeText(context, "更新成功", Toast.LENGTH_SHORT).show()
                                    onNavigateBack()
                                }
                            } catch (e: NumberFormatException) {
                                Toast.makeText(context, "请输入有效的金额", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text("保存修改", color = TextPrimary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
            ) {
                Text("取消", color = BackgroundColor)
            }
        }
    } ?: run {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("记录不存在", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
        }
    }
}

fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}
