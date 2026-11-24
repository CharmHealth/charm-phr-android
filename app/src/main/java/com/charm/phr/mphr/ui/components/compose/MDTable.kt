package com.charm.phr.mphr.ui.components.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.SOAPTemplateTableCell
import com.charm.phr.mphr.ui.Theme.MdTheme

@Composable
fun MDTable(modifier: Modifier = Modifier, tableData: ArrayList<ArrayList<SOAPTemplateTableCell>>, totalColSpan: Int, isEnable: Boolean = true) {
    var cellWidth = 100.dp
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp - dimensionResource(id = R.dimen.questionnaire_card_margin_horizontal) - 45.dp
    val maxColSize = cellWidth * totalColSpan
    if (maxColSize <= screenWidth) {
        cellWidth = screenWidth / totalColSpan
    }
    Row(
        modifier.horizontalScroll(rememberScrollState())
    ) {
        Column(Modifier.border(0.5.dp, Color.Black)) {
            tableData.forEach { rowData ->
                Row(modifier = Modifier.height(IntrinsicSize.Max)) {
                    rowData.forEach {
                        var colspan = cellWidth
                        if (it.colspan.isNotBlank()) {
                            colspan *= it.colspan.toInt()
                        }
                        if (it.type == "text") {
                            var value by remember { mutableStateOf(it.value) }
                            MDBasicTextField(
                                value = value, onValueChange = { changedValue ->
                                    value = changedValue
                                    it.value = changedValue
                                }, style = MdTheme.typography.regular14TextStyle, modifier = Modifier
                                    .width(colspan)
                                    .fillMaxHeight()
                                    .border(0.25.dp, Color.Black)
                                    .padding(8.dp), enabled = isEnable
                            )
                        } else {
                            MDTextView(
                                value = it.value, style = MdTheme.typography.semiBold14TextStyle, modifier = Modifier
                                    .width(colspan)
                                    .fillMaxHeight()
                                    .background(colorResource(id = R.color.milky_blue))
                                    .border(0.25.dp, Color.Black)
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview(showSystemUi = true)
@Composable
private fun PreviewMDTable() {
    val tableData: ArrayList<ArrayList<SOAPTemplateTableCell>> = arrayListOf()
    val cell11 = SOAPTemplateTableCell()
    cell11.value = "testfddhsfhds fdfjisdjfdsjif kjdsf"
    cell11.type = "text"
    val cell12 = SOAPTemplateTableCell()
    cell12.value = "test1"
    cell12.type = "text"
    val cell13 = SOAPTemplateTableCell()
    cell13.value = "test2"
    val row1: ArrayList<SOAPTemplateTableCell> = arrayListOf()
    row1.add(cell11)
    row1.add(cell12)
    //row1.add(cell13)
    val cell21 = SOAPTemplateTableCell()
    cell21.value = "test"
    val cell22 = SOAPTemplateTableCell()
    cell22.value = "test1"
    val cell23 = SOAPTemplateTableCell()
    cell23.value = "test2"
    val row2: ArrayList<SOAPTemplateTableCell> = arrayListOf()
    row2.add(cell21)
    row2.add(cell22)
    // row2.add(cell23)
    tableData.add(row1)
    tableData.add(row2)
    MDTable(modifier = Modifier, tableData, 3)
}
