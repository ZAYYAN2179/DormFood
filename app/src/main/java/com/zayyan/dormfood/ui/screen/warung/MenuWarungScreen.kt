package com.zayyan.dormfood.ui.screen.warung

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.zayyan.dormfood.ui.component.BottomBarWarung
import com.zayyan.dormfood.ui.component.TopAppBar
import com.zayyan.dormfood.ui.model.MenuItem
import com.zayyan.dormfood.ui.viewmodel.MenuViewModel
import com.zayyan.dormfood.ui.theme.Background
import com.zayyan.dormfood.ui.theme.DormRed
import java.util.Locale

private fun formatRupiah(amount: Int): String {
    val localeID = Locale.forLanguageTag("id-ID")
    return String.format(localeID, "%,d", amount)
}

@Composable
fun MenuWarung(
    navController: NavHostController,
    viewModel: MenuViewModel = viewModel()
) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = "Menu Warung")
        },
        bottomBar = {
            BottomBarWarung(
                navController = navController,
                currentRoute = currentRoute
            )
        },
        containerColor = Background
    ) { paddingValues ->
        MenuWarungContent(
            paddingValues = paddingValues,
            viewModel = viewModel,
            isLoading = isLoading
        )
    }
}

@Composable
fun MenuWarungContent(
    paddingValues: PaddingValues,
    viewModel: MenuViewModel,
    isLoading: Boolean
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Semua", "Makanan", "Minuman")

    val menuList by viewModel.menuList.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedMenuItem by remember { mutableStateOf<MenuItem?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var menuToDelete by remember { mutableStateOf<MenuItem?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    var successMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            successMessage = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TabSelector(
                selectedTab = selectedTab,
                tabs = tabs,
                onTabSelected = { selectedTab = it }
            )

            MenuList(
                menuList = menuList,
                selectedCategory = tabs[selectedTab],
                viewModel = viewModel,
                onEditClick = { item ->
                    selectedMenuItem = item
                    showEditDialog = true
                },
                onDeleteClick = { item ->
                    menuToDelete = item
                    showDeleteConfirm = true
                }
            )
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = DormRed,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Tambah Menu"
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DormRed)
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }

    if (showAddDialog) {
        AddMenuDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { nama, harga, tipe ->
                viewModel.addMenu(
                    namaMenu = nama,
                    harga = harga,
                    tipe = tipe,
                    onSuccess = {
                        showAddDialog = false
                        successMessage = "Menu berhasil ditambahkan"
                    },
                    onError = { error ->
                        successMessage = "Gagal menambahkan menu: $error"
                    }
                )
            }
        )
    }

    if (showEditDialog && selectedMenuItem != null) {
        EditMenuDialog(
            menuItem = selectedMenuItem!!,
            onDismiss = {
                showEditDialog = false
                selectedMenuItem = null
            },
            onConfirm = { nama, harga, tipe, ketersediaan ->
                viewModel.updateMenu(
                    menuId = selectedMenuItem!!.menuId,
                    namaMenu = nama,
                    harga = harga,
                    tipe = tipe,
                    ketersediaan = ketersediaan,
                    onSuccess = {
                        showEditDialog = false
                        selectedMenuItem = null
                        successMessage = "Menu berhasil diperbarui"
                    },
                    onError = { error ->
                        successMessage = "Gagal memperbarui menu: $error"
                    }
                )
            }
        )
    }

    if (showDeleteConfirm && menuToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirm = false
                menuToDelete = null
            },
            title = { Text("Hapus Menu") },
            text = { Text("Apakah Anda yakin ingin menghapus menu \"${menuToDelete!!.namaMenu}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMenu(
                            menuId = menuToDelete!!.menuId,
                            onSuccess = {
                                showDeleteConfirm = false
                                menuToDelete = null
                                successMessage = "Menu berhasil dihapus"
                            },
                            onError = { error ->
                                showDeleteConfirm = false
                                menuToDelete = null
                                successMessage = "Gagal menghapus menu: $error"
                            }
                        )
                    }
                ) {
                    Text("Hapus", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    menuToDelete = null
                }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun TabSelector(
    selectedTab: Int,
    tabs: List<String>,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEachIndexed { index, tab ->
            TabButton(
                text = tab,
                isSelected = selectedTab == index,
                onClick = { onTabSelected(index) }
            )
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) DormRed
                else Color(0xFFE8E8E8)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Black,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun MenuList(
    menuList: List<MenuItem>,
    selectedCategory: String,
    viewModel: MenuViewModel,
    onEditClick: (MenuItem) -> Unit,
    onDeleteClick: (MenuItem) -> Unit
) {
    val filteredItems = remember(menuList, selectedCategory) {
        if (selectedCategory == "Semua") {
            menuList
        } else {
            menuList.filter { it.tipe == selectedCategory }
        }
    }

    val groupedItems = remember(filteredItems) {
        filteredItems.groupBy { it.tipe }
    }

    if (filteredItems.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Belum ada menu",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap tombol + untuk menambah menu",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            groupedItems.forEach { (category, items) ->
                item(key = "header_$category") {
                    Text(
                        text = category,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(
                    items = items,
                    key = { it.menuId }
                ) { menuItem ->
                    MenuItemCard(
                        menuItem = menuItem,
                        onToggleAvailability = {
                            viewModel.toggleKetersediaan(
                                menuId = menuItem.menuId,
                                currentStatus = menuItem.ketersediaan,
                                onError = { /* Handled by ViewModel */ }
                            )
                        },
                        onEditClick = { onEditClick(menuItem) },
                        onDeleteClick = { onDeleteClick(menuItem) }
                    )
                }
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun MenuItemCard(
    menuItem: MenuItem,
    onToggleAvailability: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val switchChecked by remember(menuItem.ketersediaan) {
        mutableStateOf(menuItem.ketersediaan)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = menuItem.namaMenu,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Rp ${formatRupiah(menuItem.harga)}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Switch(
                    checked = switchChecked,
                    onCheckedChange = {
                        onToggleAvailability()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = DormRed,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.Gray
                    )
                )

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = Color.Gray
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                showMenu = false
                                onEditClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Hapus", color = Color.Red) },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddMenuDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String) -> Unit
) {
    var namaMenu by remember { mutableStateOf("") }
    var harga by remember { mutableStateOf("") }
    var selectedTipe by remember { mutableStateOf("Makanan") }
    val tipeOptions = listOf("Makanan", "Minuman")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Tambah Menu",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = namaMenu,
                    onValueChange = { namaMenu = it },
                    label = { Text("Nama Menu") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = harga,
                    onValueChange = {
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            harga = it
                        }
                    },
                    label = { Text("Harga") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix = { Text("Rp ") },
                    singleLine = true
                )

                Column {
                    Text(
                        text = "Tipe Menu",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tipeOptions.forEach { tipe ->
                            FilterChip(
                                selected = selectedTipe == tipe,
                                onClick = { selectedTipe = tipe },
                                label = { Text(tipe) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DormRed,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val hargaInt = harga.toIntOrNull() ?: 0
                            onConfirm(namaMenu, hargaInt, selectedTipe)
                        },
                        enabled = namaMenu.isNotBlank() && harga.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DormRed
                        )
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

@Composable
fun EditMenuDialog(
    menuItem: MenuItem,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String, Boolean) -> Unit
) {
    var namaMenu by remember { mutableStateOf(menuItem.namaMenu) }
    var harga by remember { mutableStateOf(menuItem.harga.toString()) }
    var selectedTipe by remember { mutableStateOf(menuItem.tipe) }
    var ketersediaan by remember { mutableStateOf(menuItem.ketersediaan) }
    val tipeOptions = listOf("Makanan", "Minuman")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Edit Menu",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = namaMenu,
                    onValueChange = { namaMenu = it },
                    label = { Text("Nama Menu") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = harga,
                    onValueChange = {
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            harga = it
                        }
                    },
                    label = { Text("Harga") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix = { Text("Rp ") },
                    singleLine = true
                )

                Column {
                    Text(
                        text = "Tipe Menu",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tipeOptions.forEach { tipe ->
                            FilterChip(
                                selected = selectedTipe == tipe,
                                onClick = { selectedTipe = tipe },
                                label = { Text(tipe) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DormRed,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ketersediaan")
                    Switch(
                        checked = ketersediaan,
                        onCheckedChange = { ketersediaan = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = DormRed
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val hargaInt = harga.toIntOrNull() ?: 0
                            onConfirm(namaMenu, hargaInt, selectedTipe, ketersediaan)
                        },
                        enabled = namaMenu.isNotBlank() && harga.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DormRed
                        )
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}