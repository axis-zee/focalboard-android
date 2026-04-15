package com.focalboard.android.ui.screens.boarddetail

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.focalboard.android.data.api.BoardRow
import com.focalboard.android.ui.viewmodel.BoardDetailUiState
import com.focalboard.android.ui.viewmodel.BoardDetailViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardDetailScreen(
    boardId: String,
    onBack: () -> Unit
) {
    val application = (LocalContext.current.applicationContext as Application)
    val viewModel: BoardDetailViewModel = remember {
        BoardDetailViewModel(application, boardId)
    }
    val uiState by viewModel.uiState.collectAsState()
    
    var showAddCardDialog by remember { mutableStateOf(false) }
    var showEditCardDialog by remember { mutableStateOf<BoardRow?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<BoardRow?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (val state = uiState) {
                        is BoardDetailUiState.Success -> {
                            Text(state.board.name)
                        }
                        else -> {
                            Text("Board")
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    when (val state = uiState) {
                        is BoardDetailUiState.Success -> {
                            // View selector
                            if (state.views.size > 1) {
                                Box {
                                    IconButton(onClick = { /* TODO: Show view selector */ }) {
                                        Icon(
                                            imageVector = Icons.Default.ListAlt,
                                            contentDescription = "Change view"
                                        )
                                    }
                                }
                            }
                            
                            IconButton(onClick = { viewModel.refresh() }) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Icons.Default.Refresh"
                                )
                            }
                        }
                        else -> {}
                    }
                }
            )
        },
        floatingActionButton = {
            when (uiState) {
                is BoardDetailUiState.Success -> {
                    FloatingActionButton(
                        onClick = { showAddCardDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Icons.Default.Add Card")
                    }
                }
                else -> {}
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is BoardDetailUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Loading board...")
                        }
                    }
                }
                
                is BoardDetailUiState.Success -> {
                    if (state.rows.isEmpty()) {
                        EmptyBoardState(
                            onAddCard = { showAddCardDialog = true }
                        )
                    } else {
                        // Display based on view type
                        val currentView = state.views.find { it.id == state.selectedViewId }
                        when {
                            currentView?.type == "kanban" -> KanbanView(
                                rows = state.rows,
                                statusColumnId = currentView.options?.groupByColumnId,
                                onCardClick = { row -> showEditCardDialog = row },
                                onAddCard = { showAddCardDialog = true }
                            )
                            else -> GridView(
                                rows = state.rows,
                                onCardClick = { row -> showEditCardDialog = row },
                                onAddCard = { showAddCardDialog = true }
                            )
                        }
                    }
                }
                
                is BoardDetailUiState.Error -> {
                    ErrorBoardState(
                        message = state.message,
                        onRetry = { viewModel.refresh() }
                    )
                }
                
                is BoardDetailUiState.NotAuthenticated -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Please login to continue")
                    }
                }
            }
        }
    }
    
    // Icons.Default.Add Card Dialog
    if (showAddCardDialog) {
        AddCardDialog(
            onDismiss = { showAddCardDialog = false },
            onAddCard = { name, statusValue ->
                showAddCardDialog = false
                viewModel.createCard(name, viewModel.getStatusColumnId(), statusValue)
            }
        )
    }
    
    // Icons.Default.Edit Card Dialog
    showEditCardDialog?.let { row ->
        EditCardDialog(
            row = row,
            onDismiss = { showEditCardDialog = null },
            onUpdateCard = { updates ->
                showEditCardDialog = null
                viewModel.updateCard(row.id, updates)
            }
        )
    }
    
    // Icons.Default.Delete Confirmation Dialog
    showDeleteConfirmDialog?.let { row ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            icon = {
                Icon(Icons.Default.Delete, contentDescription = null)
            },
            title = { Text("Icons.Default.Delete Card") },
            text = { Text("Are you sure you want to delete this card?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = null
                        viewModel.deleteCard(row.id)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Icons.Default.Delete", color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanbanView(
    rows: List<BoardRow>,
    statusColumnId: String?,
    onCardClick: (BoardRow) -> Unit,
    onAddCard: () -> Unit
) {
    val groupedRows = remember(rows) {
        if (statusColumnId == null) {
            mapOf("All" to rows)
        } else {
            rows.groupBy { row ->
                row.cells[statusColumnId]?.selectOptionId ?: "Unassigned"
            }
        }
    }
    
    LazyRow(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        groupedRows.forEach { (status, cards) ->
            item {
                KanbanColumn(
                    status = status,
                    cards = cards,
                    onCardClick = onCardClick,
                    onAddCard = onAddCard
                )
            }
        }
        
        // Icons.Default.Add new card column
        item {
            KanbanAddColumn(onAddCard = onAddCard)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanbanColumn(
    status: String,
    cards: List<BoardRow>,
    onCardClick: (BoardRow) -> Unit,
    onAddCard: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Column header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = status.ifEmpty { "Unassigned" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = cards.size.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Cards
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cards, key = { it.id }) { card ->
                    KanbanCard(
                        card = card,
                        onClick = { onCardClick(card) }
                    )
                }
                
                item {
                    Card(
                        onClick = onAddCard,
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Icons.Default.Add card",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanbanAddColumn(onAddCard: () -> Unit) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Icons.Default.Add new column",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanbanCard(
    card: BoardRow,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Get title from cells
            val title = card.cells["title"]?.text ?: "Untitled"
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            // Show other cell values as tags
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                card.cells.filterKeys { it != "title" }.toList().take(3).forEach { (_, cellValue) ->
                    if (cellValue.text != null) {
                        AssistChip(
                            onClick = { },
                            label = { Text(cellValue.text!!, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GridView(
    rows: List<BoardRow>,
    onCardClick: (BoardRow) -> Unit,
    onAddCard: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(rows, key = { it.id }) { card ->
            Card(
                onClick = { onCardClick(card) },
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    val title = card.cells["title"]?.text ?: "Untitled"
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Show status if available
                    card.cells.entries
                        .filter { it.key != "title" }
                        .take(2)
                        .forEach { (_, cellValue) ->
                            if (cellValue.text != null) {
                                Text(
                                    text = cellValue.text!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                }
            }
        }
        
        item {
            Card(
                onClick = onAddCard,
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Icons.Default.Add Card",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyBoardState(onAddCard: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ListAlt,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "No cards yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Icons.Default.Add your first card to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onAddCard,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Icons.Default.Add Card")
            }
        }
    }
}

@Composable
fun ErrorBoardState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Unable to load board",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry")
            }
        }
    }
}

@Composable
fun AddCardDialog(
    onDismiss: () -> Unit,
    onAddCard: (String, String?) -> Unit
) {
    var cardName by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Add, contentDescription = null)
        },
        title = { Text("Icons.Default.Add Card") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = cardName,
                    onValueChange = { cardName = it },
                    label = { Text("Card Name") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter card name...") },
                    singleLine = true
                )
                
                // TODO: Icons.Default.Add status selector dropdown when we have column definitions
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (cardName.isNotBlank()) {
                        onAddCard(cardName.trim(), selectedStatus)
                    }
                },
                enabled = cardName.isNotBlank()
            ) {
                Text("Icons.Default.Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditCardDialog(
    row: BoardRow,
    onDismiss: () -> Unit,
    onUpdateCard: (Map<String, com.focalboard.android.data.api.CellValue>) -> Unit
) {
    var cardName by remember { mutableStateOf(row.cells["title"]?.text ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Edit, contentDescription = null)
        },
        title = { Text("Icons.Default.Edit Card") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = cardName,
                    onValueChange = { cardName = it },
                    label = { Text("Card Name") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter card name...") },
                    singleLine = true
                )
                
                // Display other cell values
                row.cells.filterKeys { it != "title" }.forEach { (columnId, cellValue) ->
                    Column {
                        Text(
                            text = "Property: $columnId",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = cellValue.text ?: cellValue.selectOptionId ?: "N/A",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            // TODO: Show delete confirmation
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Icons.Default.Delete", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Icons.Default.Delete")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updates = mapOf("title" to com.focalboard.android.data.api.CellValue(type = "text", text = cardName))
                    onUpdateCard(updates)
                },
                enabled = cardName.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
