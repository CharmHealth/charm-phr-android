package com.charm.phr.mphr.ui.components.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.OptionData
import com.charm.phr.mphr.ui.Theme.MdTheme

@Composable
fun MDNestedRadioButton(modifier: Modifier, list: List<OptionData>,enable:Boolean=true) {
    val lastOption = list.last()
    val onValueChange: (Boolean, OptionData) -> Unit = { isChecked, optionData ->
        if (isChecked) {
            optionData.checked = true
            list.forEach {
                if (optionData != it && it.checked) {
                    it.checked = false
                    if (optionData.childrenOptionList.isNotEmpty()) {
                        unSelectChildren(optionData.childrenOptionList)
                    }
                }
            }
        }
    }
    Column(
        modifier
            .selectableGroup()
            .padding(top = 4.dp, bottom = 4.dp)
    ) {
        list.forEach { optionData ->
            RenderRadioLayout(optionData, lastOption == optionData, onValueChange,enable)
        }
    }
}

@Composable
fun RenderRadioLayout(optionData: OptionData, isLast: Boolean, onValueChange: (Boolean, OptionData) -> Unit, enable: Boolean) {
    val isParent= optionData.childrenOptionList.isNotEmpty()
    MDCustomIconRadio(optionData.optionString ?: "", optionData.checked, onValueChange = { isChecked ->
        onValueChange(isChecked, optionData)
    }, isParent,enable)
    if (optionData.isOther && optionData.checked) {
        var otherValue by rememberSaveable { mutableStateOf(optionData.otherValue ?: "") }
        MdOutlinedTextField(
            Modifier
                .height(dimensionResource(id = R.dimen.questionnaire_detail_simple_ques_ans_value_height))
                .fillMaxWidth()
                .padding(start = 30.dp, end = 4.dp), otherValue, {
                optionData.otherValue = it
                otherValue = it
            }, {}, MdTheme.typography.regular14TextStyle, colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.grey_trans30),
                unfocusedBorderColor = colorResource(id = R.color.grey_trans30),
            ), singleLine = true, enabled = enable
        )
    }
    if (isParent && optionData.checked) {
        val childrenVerticalLine = arrayListOf<Boolean>()
        if (optionData.viewLevel > 0) {
            childrenVerticalLine.add(!isLast)
        }
        MDNestedCheckBox(optionData.childrenOptionList, childrenVerticalLine, true,enable)
    }
}

@Composable
fun MDNestedCheckBox(list: List<OptionData>, verticalLineArr: ArrayList<Boolean>, isRadio: Boolean = false, enable: Boolean) {
    val lastOption = list.last()
    list.forEach {
        Column {
            RenderCheckBoxLayout(it, lastOption == it, verticalLineArr, isRadio,enable)
        }
    }
}

@Composable
fun RenderCheckBoxLayout(optionData: OptionData, isLast: Boolean, verticalLineArr: ArrayList<Boolean>, isRadio: Boolean,enable: Boolean) {
    val isParent=optionData.childrenOptionList.isNotEmpty()
    Row(
        modifier = Modifier.height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically
    ) {
        if (optionData.viewLevel > 0) {
            RenderDividerLayout(Modifier.align(Alignment.Top), isLast, verticalLineArr, isRadio)
        }

        MDCustomIconCheckBox(optionData.optionString ?: "", optionData.checked, onValueChange = { isChecked ->
            optionData.checked = isChecked
            if (!isChecked && optionData.childrenOptionList.isNotEmpty()) {
                unSelectChildren(optionData.childrenOptionList)
            }
        }, isParent,enable)
    }
    if (optionData.isOther && optionData.checked) {
        var otherValue by rememberSaveable { mutableStateOf(optionData.otherValue ?: "") }
        MdOutlinedTextField(
            Modifier
                .height(dimensionResource(id = R.dimen.questionnaire_detail_simple_ques_ans_value_height))
                .fillMaxWidth()
                .padding(start = 30.dp, end = 4.dp), otherValue, {
                optionData.otherValue = it
                otherValue = it
            }, {}, MdTheme.typography.regular14TextStyle, colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.grey_trans30),
                unfocusedBorderColor = colorResource(id = R.color.grey_trans30),
            ), singleLine = true
        )
    }
    if (isParent && optionData.checked) {
        val childrenVerticalLine = arrayListOf<Boolean>()
        childrenVerticalLine.addAll(verticalLineArr)
        if (optionData.viewLevel > 0) {
            childrenVerticalLine.add(!isLast)
        }
        MDNestedCheckBox(optionData.childrenOptionList, childrenVerticalLine, isRadio, enable)
    }
}

@Composable
fun RenderDividerLayout(modifier: Modifier = Modifier, isLast: Boolean, verticalLineArr: ArrayList<Boolean>, isRadio: Boolean) {
    //Render Vertical Line
    var paddingStart: Dp = if (isRadio) 16.dp else 12.dp
    verticalLineArr.forEach {
        if (it) {
            MDVerticalLine(
                modifier
                    .padding(start = paddingStart), 0.5.dp, colorResource(R.color.grey)
            )
            paddingStart = 22.dp
        } else {
            paddingStart += 22.7.dp
        }
    }
    if (isLast) {
        MDVerticalLine(
            modifier
                .padding(start = paddingStart)
                .height(14.dp), 0.5.dp, colorResource(R.color.grey)
        )
    } else {
        MDVerticalLine(modifier.padding(start = paddingStart), 0.5.dp, colorResource(R.color.grey))
    }
    MDHorizontalLine(
        modifier
            .width(10.dp)
            .padding(top = 14.dp), 0.5.dp, colorResource(R.color.grey)
    )
}

fun unSelectChildren(childrenOptionList: List<OptionData>) {
    childrenOptionList.forEach {
        it.checked = false
        if (it.childrenOptionList.isNotEmpty()) {
            unSelectChildren(it.childrenOptionList)
        }
    }
}



//region Preview
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NestedCheckListPreview() {
    val optionList: ArrayList<OptionData> = arrayListOf()
    val opt1 = OptionData()
    opt1.checked = false
    opt1.optionString =
        "opt1"
    optionList.add(opt1)
    val optionList1: ArrayList<OptionData> = arrayListOf()
    val opt11 = OptionData()
    opt11.optionString =
        "opt1.1"
    opt11.viewLevel = 1
    val optionList11: ArrayList<OptionData> = arrayListOf()
    val opt111 = OptionData()
    opt111.optionString =
        "opt1.1.1"
    opt111.viewLevel = 2
    optionList11.add(opt111)
    val opt112 = OptionData()
    opt112.optionString =
        "opt1.1.2"
    opt112.viewLevel = 2
    optionList11.add(opt112)
    opt11.childrenOptionList = optionList11
    val optionList111: ArrayList<OptionData> = arrayListOf()
    val opt1111 = OptionData()
    opt1111.optionString = "opt1.1.1.1"
    opt1111.viewLevel = 3
    val optionList1111: ArrayList<OptionData> = arrayListOf()
    val opt11111 = OptionData()
    opt11111.optionString = "opt1.1.1.1.1"
    opt11111.viewLevel = 4
    optionList1111.add(opt11111)
    opt1111.childrenOptionList = optionList1111
    optionList111.add(opt1111)
    opt111.childrenOptionList = optionList111
    optionList1.add(opt11)
    val opt12 = OptionData()
    opt12.optionString = "opt1.2"
    opt12.viewLevel = 1
    optionList1.add(opt12)
    opt1.childrenOptionList = optionList1
    val opt2 = OptionData()
    opt2.optionString = "opt2"
    val optionList2: ArrayList<OptionData> = arrayListOf()
    val opt21 = OptionData()
    opt21.optionString = "opt2.1"
    opt21.viewLevel = 1
    optionList2.add(opt21)
    val opt22 = OptionData()
    opt22.optionString = "opt2.2"
    opt22.viewLevel = 1
    val optionList22: ArrayList<OptionData> = arrayListOf()
    val opt221 = OptionData()
    opt221.optionString = "opt2.2.1"
    opt221.viewLevel = 2
    optionList22.add(opt221)
    val opt222 = OptionData()
    opt222.optionString = "opt2.2.2"
    opt222.viewLevel = 2
    val optionList222: ArrayList<OptionData> = arrayListOf()
    val opt2221 = OptionData()
    opt2221.optionString = "opt2.2.2.1"
    opt2221.viewLevel = 3
    optionList222.add(opt2221)
    val opt2222 = OptionData()
    opt2222.optionString = "opt2.2.2.2"
    opt2222.viewLevel = 3
    optionList222.add(opt2222)
    opt222.childrenOptionList = optionList222
    optionList22.add(opt222)
    opt22.childrenOptionList = optionList22
    optionList2.add(opt22)
    opt2.childrenOptionList = optionList2
    optionList.add(opt2)
    val other = OptionData()
    other.optionString = "Other"
    other.isOther = true
    other.checked = true
    optionList.add(other)
    val list by rememberSaveable { mutableStateOf(optionList.toList()) }
    Column(
        modifier = Modifier
            .wrapContentSize()
            .padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
    ) {
        MDNestedCheckBox(list, arrayListOf(), isRadio = true, enable = true)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NestedRadioPreview() {
    val optionList: ArrayList<OptionData> = arrayListOf()
    val opt1 = OptionData()
    opt1.optionString =
        "opt1"
    optionList.add(opt1)
    val optionList1: ArrayList<OptionData> = arrayListOf()
    val opt11 = OptionData()
    opt11.optionString =
        "opt1.1"
    opt11.viewLevel = 1
    val optionList11: ArrayList<OptionData> = arrayListOf()
    val opt111 = OptionData()
    opt111.optionString =
        "opt1.1.1"
    opt111.viewLevel = 2
    optionList11.add(opt111)
    val opt112 = OptionData()
    opt112.optionString =
        "opt1.1.2"
    opt112.viewLevel = 2
    optionList11.add(opt112)
    opt11.childrenOptionList = optionList11
    val optionList111: ArrayList<OptionData> = arrayListOf()
    val opt1111 = OptionData()
    opt1111.optionString = "opt1.1.1.1"
    opt1111.viewLevel = 3
    val optionList1111: ArrayList<OptionData> = arrayListOf()
    val opt11111 = OptionData()
    opt11111.optionString = "opt1.1.1.1.1"
    opt11111.viewLevel = 4
    optionList1111.add(opt11111)
    opt1111.childrenOptionList = optionList1111
    optionList111.add(opt1111)
    opt111.childrenOptionList = optionList111
    optionList1.add(opt11)
    val opt12 = OptionData()
    opt12.optionString = "opt1.2"
    opt12.viewLevel = 1
    optionList1.add(opt12)
    opt1.childrenOptionList = optionList1
    val opt2 = OptionData()
    opt2.optionString = "opt2"
    val optionList2: ArrayList<OptionData> = arrayListOf()
    val opt21 = OptionData()
    opt21.optionString = "opt2.1"
    opt21.viewLevel = 1
    optionList2.add(opt21)
    val opt22 = OptionData()
    opt22.optionString = "opt2.2"
    opt22.viewLevel = 1
    val optionList22: ArrayList<OptionData> = arrayListOf()
    val opt221 = OptionData()
    opt221.optionString = "opt2.2.1"
    opt221.viewLevel = 2
    optionList22.add(opt221)
    val opt222 = OptionData()
    opt222.optionString = "opt2.2.2"
    opt222.viewLevel = 2
    val optionList222: ArrayList<OptionData> = arrayListOf()
    val opt2221 = OptionData()
    opt2221.optionString = "opt2.2.2.1"
    opt2221.viewLevel = 3
    optionList222.add(opt2221)
    val opt2222 = OptionData()
    opt2222.optionString = "opt2.2.2.2"
    opt2222.viewLevel = 3
    optionList222.add(opt2222)
    opt222.childrenOptionList = optionList222
    optionList22.add(opt222)
    opt22.childrenOptionList = optionList22
    optionList2.add(opt22)
    opt2.childrenOptionList = optionList2
    optionList.add(opt2)
    val other = OptionData()
    other.optionString = "Other"
    other.isOther = true
    other.checked = true
    optionList.add(other)
    val list by rememberSaveable { mutableStateOf(optionList.toList()) }

    MDNestedRadioButton(
        Modifier
            .wrapContentSize()
            .padding(top = 4.dp, bottom = 4.dp), list
    )
}
//endregion


