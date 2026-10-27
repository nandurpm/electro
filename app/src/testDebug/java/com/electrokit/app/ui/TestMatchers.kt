package com.electrokit.app.ui

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.semantics.SemanticsProperties

/** Selects the actual content list rather than a horizontally scrolling category row.
 * @return Matcher requiring both index-based scrolling and a vertical scroll axis.
 */
fun verticalList(): SemanticsMatcher = hasScrollToIndexAction() and SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
