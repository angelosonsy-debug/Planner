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
import com.plannermvp.app.domain.importing.DateAnalysisSummary
import com.plannermvp.app.domain.importing.ImportDateSanityChecker
import com.plannermvp.app.domain.importing.ImportValidator
import com.plannermvp.app.domain.importing.TxtPlanParser
import com.plannermvp.app.domain.importing.ValidatedImportItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ImportUiState {
    data object Idle : ImportUiState
    data class Preview(
        val items: List<ValidatedImportItem>,
        val selected: List<Boolean>,
        /** Release 1.0: null when no dated items, otherwise anomaly analysis. */
        val dateAnalysis: DateAnalysisSummary? = null
    ) : ImportUiState
    data class Done(val result: ImportResult) : ImportUiState
    data class Error(val message: String) : ImportUiState
}

class ImportViewModel(private val importRepository: ImportRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    fun onFileContentPicked(fileName: String?, content: String) {
        if (content.isBlank()) {
            _uiState.value = ImportUiState.Error("الملف فارغ.")
            return
        }
        val looksLikeCsv = fileName?.endsWith(".csv", ignoreCase = true) == true ||
            (fileName?.endsWith(".txt", ignoreCase = true) != true && firstLineLooksLikeCsv(content))
        val items = if (looksLikeCsv) CsvPlanParser.parse(content) else TxtPlanParser.parse(content)
        if (items.isEmpty()) {
            _uiState.value = ImportUiState.Error("لم يتم العثور على مهام في هذا الملف. تحقق من التنسيق وحاول مجددًا.")
            return
        }
        val validated = ImportValidator.validate(items)

        // Release 1.0: default selection respects date plausibility
        // OLD_PAST and VERY_OLD items are unchecked by default
        val selected = validated.map { item ->
            !item.hasError && item.datePlausibility.isDefaultChecked
        }

        // Anomaly analysis
        val dateAnalysis = ImportDateSanityChecker.analyseAll(validated.map { it.resolvedDate })

        _uiState.value = ImportUiState.Preview(
            items        = validated,
            selected     = selected,
            dateAnalysis = if (dateAnalysis.total > 0) dateAnalysis else null
        )
    }

    fun onFileReadFailed(message: String) { _uiState.value = ImportUiState.Error(message) }

    fun toggleSelected(index: Int) {
        val state = _uiState.value as? ImportUiState.Preview ?: return
        val updated = state.selected.toMutableList().also { it[index] = !it[index] }
        _uiState.value = state.copy(selected = updated)
    }

    fun confirmImport() {
        val state = _uiState.value as? ImportUiState.Preview ?: return
        val selectedItems = state.items.filterIndexed { i, _ -> state.selected[i] }
        viewModelScope.launch {
            _uiState.value = ImportUiState.Done(importRepository.confirmImport(selectedItems))
        }
    }

    fun reset() { _uiState.value = ImportUiState.Idle }

    private fun firstLineLooksLikeCsv(content: String): Boolean {
        val first = content.lineSequence().firstOrNull { it.isNotBlank() } ?: return false
        return first.contains(",") && !first.contains(":")
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
