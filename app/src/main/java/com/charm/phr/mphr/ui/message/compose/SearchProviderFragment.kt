package com.charm.phr.mphr.ui.message.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.FragmentSearchProviderBinding
import com.charm.phr.mphr.model.Member
import com.charm.phr.mphr.model.MessageGroup
import com.charm.phr.mphr.ui.Theme.MPHRTheme
import com.charm.phr.mphr.ui.Theme.MdTheme
import com.charm.phr.mphr.ui.components.ProgressDialog
import com.charm.phr.mphr.ui.components.compose.MDChip
import com.charm.phr.mphr.ui.components.compose.MDHorizontalLine
import com.charm.phr.mphr.ui.components.compose.MDIconToggleButton
import com.charm.phr.mphr.ui.components.compose.MDSearchBar
import com.charm.phr.mphr.ui.components.compose.MDTextView
import com.charm.phr.mphr.ui.components.compose.SearchBar
import com.charm.phr.mphr.util.UIUtil
import kotlinx.coroutines.launch

class SearchProviderFragment : Fragment() {

    lateinit var binding: FragmentSearchProviderBinding

    lateinit var composeMessageViewModel: ComposeMessageViewModel

    lateinit var progressDialog: ProgressDialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSearchProviderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        composeMessageViewModel = ViewModelProvider(requireActivity())[ComposeMessageViewModel::class.java]

        UIUtil.applyViewInsertForToolbar(binding.searchProviderToolbar)
        UIUtil.applyViewInsertForBottomView(binding.searchProviderMainLayout)

        composeMessageViewModel.searchQuery = ""

        progressDialog = ProgressDialog(requireContext())

        setOnClickListener()

        composeMessageViewModel.updateFilteredMemberList()
        composeMessageViewModel.updateFilteredMessageGroupList()
        composeMessageViewModel.showBackPressDialog = false

        binding.composeView.addView(ComposeView(requireActivity()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                MPHRTheme {
                    SearchProviderScreen(composeMessageViewModel)
                }
            }
        })

    }

    fun setOnClickListener() {
        binding.searchProviderBackBtn.setOnClickListener {
            val activity = requireActivity() as? ComposeMessageActivity
            activity?.onBackPressed()
        }
        binding.addProviderBtn.setOnClickListener {
            composeMessageViewModel.showBackPressDialog = false
            val activity = requireActivity() as? ComposeMessageActivity
            activity?.onBackPressed()
        }
    }


    //region Composable components
    @Composable
    fun SearchProviderScreen(viewModel: ComposeMessageViewModel) {
        Column {
            if (viewModel.tempSelectedMemberList.isNotEmpty() || viewModel.tempSelectedMessageGroupList.isNotEmpty()) {
                SelectedListChips(
                    selectedMembers = viewModel.tempSelectedMemberList,
                    selectedGroup = viewModel.tempSelectedMessageGroupList,
                    onMemberRemoved = { member ->
                        composeMessageViewModel.showBackPressDialog = true
                        viewModel.toggleMemberSelection(member) // Deselect on chip click
                    },
                    onGroupRemoved = {
                        composeMessageViewModel.showBackPressDialog = true
                        viewModel.toggleMessageGroupSelection(it)
                    }
                )
            }

            MDSearchBar(
                query = viewModel.searchQuery,
                onQueryChanged = { viewModel.updateSearchQuery(it) },
                hint = getString(R.string.enter_search_text),
            )

            val memberList = viewModel.filteredMemberList
            val groupList = viewModel.filteredMessageGroupList
            if (memberList.isEmpty() && groupList.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MDTextView(
                        value = getString(R.string.no_result_found),
                        style = MdTheme.typography.medium16TextStyle,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyColumn {
                    if (groupList.isNotEmpty()) {
                        item {
                            HeadingCard(
                                title = getString(R.string.groups),
                                icon = R.drawable.ic_groups
                            )
                        }
                        itemsIndexed(groupList) { index, group ->
                            DetailSelectionItem(
                                itemText = group.groupName ?: "",
                                itemDescription = group.groupDesc,
                                isSelected = group.isSelected,
                                onClickHandler = {
                                    if (viewModel.tempSelectedMemberList.isNotEmpty()) {
                                        Toast.makeText(
                                            context,
                                            getString(R.string.both_group_member_not_allowed),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else if (viewModel.tempSelectedMessageGroupList.isNotEmpty() && !group.isSelected) {
                                        Toast.makeText(
                                            context,
                                            getString(R.string.message_to_only_one_group),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        composeMessageViewModel.showBackPressDialog = true
                                        viewModel.toggleMessageGroupSelection(viewModel.tempSelectedMessageGroupList[0])
                                        viewModel.toggleMessageGroupSelection(group)
                                    } else {
                                        composeMessageViewModel.showBackPressDialog = true
                                        viewModel.toggleMessageGroupSelection(group)
                                    }
                                },
                                showBottomLine = index != groupList.lastIndex
                            )
                        }
                    }
                    if (memberList.isNotEmpty()) {
                        item {
                            HeadingCard(
                                title = getString(R.string.patient_list_heading_txt),
                                icon = R.drawable.ic_members
                            )
                        }
                        itemsIndexed(memberList) { index, member ->
                            DetailSelectionItem(
                                itemText = member.nameWithTitle ?: "",
                                itemDescription = member.specialization,
                                isSelected = member.isSelected,
                                onClickHandler = {
                                    if (viewModel.tempSelectedMessageGroupList.isEmpty()) {
                                        composeMessageViewModel.showBackPressDialog = true
                                        viewModel.toggleMemberSelection(member)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            getString(R.string.both_group_member_not_allowed),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                showBottomLine = index != memberList.lastIndex
                            )
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun SelectedListChips(
        selectedMembers: List<Member>,
        selectedGroup: List<MessageGroup>,
        onMemberRemoved: (Member) -> Unit,
        onGroupRemoved: (MessageGroup) -> Unit,
    ) {
        val scrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()
        LaunchedEffect(selectedMembers.size) {
            coroutineScope.launch {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
        }
        Column(
            modifier = Modifier
                .heightIn(min = 0.dp, max = 87.dp)
                .verticalScroll(scrollState)  // Enable vertical scrolling
        ) {
            FlowRow(
                modifier = Modifier
                    .padding(
                        start = MdTheme.dimension.horizontalPadding,
                        top = MdTheme.dimension.doubleCardMargin,
                        end = MdTheme.dimension.horizontalPadding
                    )
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedMembers.forEach { member ->
                    MDChip(
                        chipText =member.nameWithTitle?:"" ,
                        onRemoveHandler = { onMemberRemoved(member) }
                    )
                }
                selectedGroup.forEach { group ->
                    MDChip(
                        chipText = group.groupName ?: "",
                        onRemoveHandler = { onGroupRemoved(group) }
                    )
                }
            }
        }
    }


    @Composable
    fun DetailSelectionItem(
        modifier: Modifier = Modifier,
        itemText: String,
        itemDescription: String?,
        isSelected: Boolean,
        onClickHandler: () -> Unit,
        showBottomLine: Boolean = true
    ) {
        Column {
            Row(
                modifier
                    .fillMaxWidth()
                    .padding(
                        start = 4.dp, end = 4.dp
                    )
                    .clickable { onClickHandler() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                MDIconToggleButton(
                    modifier = Modifier.wrapContentSize(),
                    icRadioSelectedIcon = R.drawable.ic_checkbox_selected,
                    isSelected = isSelected,
                    icRadioUnSelectedIcon = R.drawable.ic_checkbox_unselected,
                    tint = Color.Unspecified,
                    contentDescription = null,
                    onValueChange = {
                        onClickHandler()
                    }
                )
                Column(
                    modifier.heightIn(min=52.dp)
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    MDTextView(
                        value = itemText,
                        style = MdTheme.typography.medium16TextStyle
                    )
                    if (!itemDescription.isNullOrBlank()) {
                        MDTextView(
                            modifier= Modifier.padding(top=2.dp),
                            value = itemDescription,
                            style = MdTheme.typography.regular14TextStyle
                        )
                    }
                }

            }
            if (showBottomLine) {
                MDHorizontalLine(
                    modifier.padding(
                        start = 4.dp, end = 4.dp
                    ), 0.5.dp, colorResource(R.color.separatorColor)
                )
            }
        }
    }

    @Composable
    fun HeadingCard(title: String, icon: Int) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            color = colorResource(R.color.practiceSeparator)
        ) {
            Row {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.padding(
                        start = MdTheme.dimension.doubleCardMargin,
                        top = MdTheme.dimension.horizontalPadding
                    )
                )
                MDTextView(
                    value = title,
                    style = MdTheme.typography.medium16TextStyle,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }

    //endregion

    //region Preview Compose UI
    @Preview(showSystemUi = true)
    @Composable
    fun PreviewItem(modifier: Modifier = Modifier) {
        val member = Member(
            "131341413431",
            "test",
            specialization = "test spe",
            prefix = "dr",
            isSelected = true
        )
        DetailSelectionItem(
            modifier,
            "test",
            "testing",
            true, {}
        )
    }

    @Preview(showSystemUi = true)
    @Composable
    fun PreviewSelectedMembers(modifier: Modifier = Modifier) {
        val member = Member(
            "131341413431",
            "test",
            specialization = "test spe",
            prefix = "dr",
            isSelected = true
        )
        val member1 = Member(
            "131341413431",
            "test1234",
            specialization = "test spe",
            prefix = "dr",
            isSelected = true
        )
        val member2 = Member(
            "131341413431",
            "test1234",
            specialization = "test spe",
            prefix = "dr",
            isSelected = true
        )
        val member3 = Member(
            "131341413431",
            "test1234",
            specialization = "test spe",
            prefix = "dr",
            isSelected = true
        )
        val member4 = Member(
            "131341413431",
            "test1234",
            specialization = "test spe",
            prefix = "dr",
            isSelected = true
        )
        val member5 = Member(
            "131341413431",
            "test1234",
            specialization = "test spe",
            prefix = "dr",
            isSelected = true
        )
        val member6 = Member(
            "131341413431",
            "test1234",
            specialization = "test spe",
            prefix = "dr",
            isSelected = true
        )
        val list = arrayListOf(member, member1, member2, member3, member4, member5, member6)
        // SelectedMemberChips(list, {})
    }

    @Preview(showSystemUi = true)
    @Composable
    fun PreviewSearchBar(modifier: Modifier = Modifier) {
        SearchBar("testing") { }
    }

    @Preview(showBackground = true)
    @Composable
    fun PreviewSearchHeader(modifier: Modifier = Modifier) {
        MPHRTheme {
            HeadingCard("testing",
                R.drawable.ic_members
            )
        }

    }

    //endregion

}