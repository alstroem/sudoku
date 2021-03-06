package dk.alstroem.sudoku

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dk.alstroem.logic.data.Grid
import dk.alstroem.logic.data.GridSize
import dk.alstroem.sudoku.model.SelectedCell
import dk.alstroem.sudoku.ui.theme.SudokuTheme
import timber.log.Timber

@Composable
fun GridScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SudokuGrid(
                data = uiState.grid,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(4.dp),
                selectedCell = uiState.selectedCell,
                onSelected = { row, column -> viewModel.selectCell(row, column) },
                showAutoCandidates = uiState.showAutoCandidates
            )

            Spacer(modifier = Modifier.size(16.dp))
            Text(text = "Input", style = MaterialTheme.typography.bodyLarge)
            NumberInput(
                size = uiState.grid.size,
                textStyle = MaterialTheme.typography.headlineMedium,
                onNumberSelected = { viewModel.addInput(it) },
                onNumberDeleted = { viewModel.clearInput() }
            )

            Spacer(modifier = Modifier.size(16.dp))
            Text(text = "Candidates", style = MaterialTheme.typography.bodyLarge)
            NumberInput(
                size = uiState.grid.size,
                textStyle = MaterialTheme.typography.titleMedium,
                onNumberSelected = { viewModel.addCandidate(it) },
                onNumberDeleted = { viewModel.clearCandidates() }
            )

            Spacer(modifier = Modifier.size(8.dp))
            AutoCandidatesSelection(
                selected = viewModel.uiState.showAutoCandidates,
                onSelectionChanged = { viewModel.showAutoCandidates(it) }
            )

            Spacer(modifier = Modifier.size(16.dp))

        }
    }
}

@Composable
fun SudokuGrid(
    data: Grid,
    modifier: Modifier = Modifier,
    selectedCell: SelectedCell = SelectedCell(-1, -1),
    onSelected: (Int, Int) -> Unit,
    showAutoCandidates: Boolean = false
) {
    CustomGrid(size = data.size, modifier = modifier) {
        for (row in data.size.indexRange) {
            for (column in data.size.indexRange) {
                Cell(
                    data = data[row, column],
                    candidates = data.candidates[row][column],
                    showAutoCandidates = showAutoCandidates,
                    isSelected = row == selectedCell.row && column == selectedCell.column,
                    onSelected = onSelected
                )
            }
        }
    }
}

@Composable
fun CustomGrid(
    size: GridSize,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->

        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }

        val cellHeights = Array(size.count) { 0 }
        val cellWidths = Array(size.count) { 0 }

        val padding = 1.dp.roundToPx()
        val middlePadding = 4.dp.roundToPx()

        placeables.forEachIndexed { index, placeable ->
            val row = index / size.count
            val column = index % size.count

            val verticalPadding = if (column.plus(1) % size.nonetSize == 0) middlePadding else padding
            val horizontalPadding = if (row.plus(1) % size.nonetSize == 0) middlePadding else padding

            cellWidths[row] += placeable.width + verticalPadding
            cellHeights[column] += placeable.height + horizontalPadding
        }

        val width = (cellWidths.maxOrNull()?.minus(middlePadding) ?: constraints.minWidth).coerceAtMost(constraints.maxWidth)
        val height = (cellHeights.maxOrNull()?.minus(middlePadding) ?: constraints.minHeight).coerceAtMost(constraints.maxHeight)

        layout(width, height) {
            val cellX = Array(size.count) { 0 }
            val cellY = Array(size.count) { 0 }

            placeables.forEachIndexed { index, placeable ->
                val row = index / size.count
                val column = index % size.count

                val verticalPadding = if (column.plus(1) % size.nonetSize == 0) middlePadding else padding
                val horizontalPadding = if (row.plus(1) % size.nonetSize == 0) middlePadding else padding

                placeable.placeRelative(
                    x = cellX[row],
                    y = cellY[column]
                )

                cellX[row] += placeable.width + verticalPadding
                cellY[column] += placeable.height + horizontalPadding
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GridPreview() {
    SudokuTheme {
        SudokuGrid(data = Grid(), onSelected = { _, _ -> Timber.d("Cell clicked") })
    }
}
