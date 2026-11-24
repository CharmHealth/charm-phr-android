package com.charm.phr.mphr.ui.demographics.save_pat_demo

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.Window
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.DialogRaceListBinding
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil

class RaceSelectDialog(context: Context ,val width: Int ,val raceET: EditText): Dialog(context) {

    val raceList = arrayListOf<String>()

    private lateinit var binding: DialogRaceListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogRaceListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setCancelable(false)
        setCanceledOnTouchOutside(false)


        binding.raceListCardView.layoutParams?.width = width
        binding.raceListCardView.requestLayout()


        val selectedRaceStr = raceET.text.toString()

        val selectedRace = arrayListOf<String>()

        if (selectedRaceStr.isNotBlank() && selectedRaceStr != ModelUtil.questionnairePersonalDetailDeclineToSpecify){
             selectedRace.addAll(selectedRaceStr.split(",").filter { it.isNotBlank() }.map { it.trim() })
        }
        else{
            if (selectedRaceStr == ModelUtil.questionnairePersonalDetailDeclineToSpecify){
                binding.raceListDTSCheckbox.isChecked = true
            }
        }



        /* <CheckBox
            android:cursorVisible="false"
            android:id="@+id/raceListDTSCheckbox"
            android:paddingStart="@dimen/card_padding_half"
            android:paddingEnd="@dimen/card_padding_half"
            android:layout_marginBottom="@dimen/item_questionnaire_option_bottom_margin"
            android:layout_marginEnd="@dimen/card_padding_half"
            android:fontFamily="@font/open_sans"
            android:textSize="@dimen/medium"
            android:textColor="@color/edittext_txt_color"
            android:text="@string/decline_to_specify"
            android:layout_width="match_parent"
            android:minHeight="@dimen/race_checkbox_height"
            android:layout_height="wrap_content"
            app:layout_constraintTop_toBottomOf="@id/raceListScrollSeparator"
            android:layout_marginStart="@dimen/race_list_checkbox_start_margin"
            />*/
        val raceCheckBoxHeight = context.resources.getDimensionPixelSize(R.dimen.race_checkbox_height)
        val cardPaddingHalf = context.resources.getDimensionPixelSize(R.dimen.card_padding_half)
        val raceCheckBoxSideMargin = context.resources.getDimensionPixelSize(R.dimen.race_list_checkbox_start_margin)

        raceList.forEach { race ->
            val checkbox = CheckBox(context)
            binding.raceListMainLayout.addView(checkbox)

            val lp =  LinearLayout.LayoutParams(MATCH_PARENT ,raceCheckBoxHeight )
            lp.marginStart = raceCheckBoxSideMargin

            checkbox.layoutParams = lp
            checkbox.isCursorVisible = false
            checkbox.setPadding(cardPaddingHalf ,checkbox.paddingTop ,cardPaddingHalf,checkbox.paddingBottom)
            checkbox.setText(race)
            checkbox.typeface = UIUtil.getFont(context,R.font.open_sans)
            checkbox.setTextColor(ContextCompat.getColor(context , R.color.black87))
            checkbox.ellipsize = TextUtils.TruncateAt.END
            checkbox.maxLines = 1




            checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked){
                    binding.raceListDTSCheckbox.isChecked = false
                }
            }

            if (selectedRace.contains(race)){
                checkbox.isChecked = true
            }
        }

        binding.raceListDTSCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->

                disableAll(isChecked)

        }

        binding.raceListOkBtn.setOnClickListener {

            if (binding.raceListDTSCheckbox.isChecked){
                raceET.setText(R.string.decline_to_specify)
            }
            else{

                val selectedList = arrayListOf<String>()

                for (index in 0 until raceList.size){
                    val checkbox = binding.raceListMainLayout.getChildAt(index)
                    if (checkbox is CheckBox){
                        if (checkbox.isChecked){
                            selectedList.add(raceList[index])
                        }
                    }
                }
                raceET.setText(selectedList.joinToString(", "))
            }


            dismiss()
        }

        binding.raceListCancelBtn.setOnClickListener {
            dismiss()
        }

    }

    private fun disableAll(toDisable: Boolean){

        for (index in 0 until raceList.size){
            val checkbox = binding.raceListMainLayout.getChildAt(index)
            if (checkbox is CheckBox){

                if (toDisable) {
                    checkbox.isChecked = false
                    checkbox.isEnabled = false
                }
                else{
                    checkbox.isEnabled = true
                }
            }
        }
    }


}