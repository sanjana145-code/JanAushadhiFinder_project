package com.example.janaushadhifinder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockRequestBottomSheet(
    medicine: Medicine,
    uiState: StockRequestUiState,
    stores: List<JanAushadhiStore>,
    onDismiss: () -> Unit,
    onQuantityChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onStoreSelected: (String) -> Unit,
    onSendRequest: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = { if (!uiState.isSending) onDismiss() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color(0xFFE0F2F1), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.LocalPharmacy, contentDescription = null, tint = TealBg)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Request Stock", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(medicine.brandName, color = Color(0xFF263238), fontWeight = FontWeight.SemiBold)
                    Text(medicine.genericName, color = TealBg, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            StoreSelector(
                selectedStoreName = uiState.selectedStoreName,
                stores = stores,
                enabled = !uiState.isSending,
                onStoreSelected = onStoreSelected
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = uiState.quantity,
                onValueChange = onQuantityChanged,
                label = { Text("Quantity") },
                placeholder = { Text("e.g. 2") },
                enabled = !uiState.isSending,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = stockTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = uiState.userPhone,
                onValueChange = onPhoneChanged,
                label = { Text("Phone number (optional)") },
                placeholder = { Text("10-digit mobile number") },
                enabled = !uiState.isSending,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = stockTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = onNotesChanged,
                label = { Text("Notes (optional)") },
                placeholder = { Text("Preferred strip count, urgency, etc.") },
                enabled = !uiState.isSending,
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = stockTextFieldColors()
            )

            if (uiState.errorMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(uiState.errorMessage, color = RedText, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !uiState.isSending,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 50.dp)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = onSendRequest,
                    enabled = !uiState.isSending,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealBg)
                ) {
                    if (uiState.isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sending")
                    } else {
                        Text("Send Request", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StoreSelector(
    selectedStoreName: String,
    stores: List<JanAushadhiStore>,
    enabled: Boolean,
    onStoreSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { if (enabled) expanded = true },
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Storefront, contentDescription = null, tint = TealBg)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = selectedStoreName,
                modifier = Modifier.weight(1f),
                color = Color(0xFF263238)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            stores.forEach { store ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(store.name, fontWeight = FontWeight.SemiBold)
                            Text(store.address, color = Color.Gray, fontSize = 12.sp)
                        }
                    },
                    onClick = {
                        expanded = false
                        onStoreSelected(store.name)
                    }
                )
            }
        }
    }
}

@Composable
private fun stockTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = TealBg,
    unfocusedBorderColor = Color.LightGray,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White
)
