package com.plannermvp.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.plannermvp.app.PlannerApp
import com.plannermvp.app.data.repository.ImportRepository
import com.plannermvp.app.data.repository.ImportResult
import com.plannermvp.app.domain.importing.CsvPlanParser
import com.plannermvp.app.domain.importing.ImportValidator
import com.plannermvp.app.domain.importing.TxtPlanParser
import com.plannermvp.app.domain.importing.ValidatedImportItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ImportUiState {
    data object Idle : ImportUiState
    data class Preview(val items: List<ValidatedImportItem>, val selected: List<Boolean>) : ImportUiState
    data class Done(val result: ImportResult) : ImportUiState
    data class Error(val message: String) : ImportUiState
}

/**
 * Drives the Import screen through Section 43's pipeline:
 * File -> Reader -> Parser -> Validation -> Normalized Model -> Preview ->
 * User Confirmation -> Repository -> Room. Everything left of "Preview"
 * happens synchronously in [onFileContentPicked] since the parsers are
 * pure/fast; only [confirmImport] touches the database.
 */
class ImportViewModel(private val importRepository: ImportRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    fun onFileContentPicked(fileName: String?, content: String) {
        if (content.isBlank()) {
            _uiState.value = ImportUiState.Error("This file is empty.")
            return
        }

        val looksLikeCsv = fileName?.endsWith(".csv", ignoreCase = true) == true ||
            (fileName?.endsWith(".txt", ignoreCase = true) != true && firstLineLooksLikeCsv(content))
        val items = if (looksLikeCsv) CsvPlanParser.parse(content) else TxtPlanParser.parse(content)

        if (items.isEmpty()) {
            _uiState.value = ImportUiState.Error("No tasks were found in this file. Check the format and try again.")
            return
        }

        val validated = ImportValidator.validate(items)
        _uiState.value = ImportUiState.Preview(
            items = validated,
            selected = validated.map { !it.hasError }
        )
    }

    fun onFileReadFailed(message: String) {
        _uiState.value = ImportUiState.Error(message)
    }

    private fun firstLineLooksLikeCsv(content: String): Boolean {
        val firstLine = content.lineSequence().firstOrNull { it.isNotBlank() } ?: return false
        return firstLine.contains(",") && !firstLine.contains(":")
    }

    fun toggleSelected(index: Int) {
        val state = _uiState.value as? ImportUiState.Preview ?: return
        val updated = state.selected.toMutableList().also { it[index] = !it[index] }
        _uiState.value = state.copy(selected = updated)
    }

    fun confirmImport() {
        val state = _uiState.value as? ImportUiState.Preview ?: return
        val selectedItems = state.items.filterIndexed { index, _ -> state.selected[index] }
        viewModelScope.launch {
            val result = importRepository.confirmImport(selectedItems)
            _uiState.value = ImportUiState.Done(result)
        }
    }

    fun reset() {
        _uiState.value = ImportUiState.Idle
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PlannerApp)
                ImportViewModel(app.importRepository)
            }
        }
    }
}
