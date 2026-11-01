package com.electrokit.app.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.electrokit.app.R

/**
 * Aligned comparison rows retain readable columns and allow horizontal panning.
 *
 * @param names Column titles for the selected records.
 * @param rows Parameter labels and one cell per selected record.
 * @return A table whose row heights accommodate the largest wrapped cell.
 */
@Composable
fun ComparisonTable(names: List<String>, rows: List<Pair<String, List<String>>>) {
    Text(stringResource(R.string.compare_scroll_help), style = MaterialTheme.typography.labelMedium)
    Column(Modifier.horizontalScroll(rememberScrollState())) {
        Row(Modifier.semantics { heading() }) {
            Text(
                stringResource(R.string.compare_parameter),
                Modifier.width(144.dp).padding(8.dp),
                fontWeight = FontWeight.Bold,
            )
            names.forEach {
                Text(it, Modifier.width(220.dp).padding(8.dp), fontWeight = FontWeight.Bold)
            }
        }
        rows.forEach { (label, cells) ->
            HorizontalDivider()
            Row {
                Text(label, Modifier.width(144.dp).padding(8.dp), fontWeight = FontWeight.SemiBold)
                cells.forEach { Text(it, Modifier.width(220.dp).padding(8.dp)) }
            }
        }
    }
}
