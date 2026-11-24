package com.charm.phr.mphr.ui.lab.resultDetail

import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemLabResultDetailTestListBinding
import com.charm.phr.mphr.model.MedicalRecordEntry

class LabResultDetailListAdapter : RecyclerView.Adapter<LabResultDetailListAdapter.ViewHolder>() {

    private var medicalRecordEntries: ArrayList<MedicalRecordEntry> = arrayListOf()
    private val viewPool = RecyclerView.RecycledViewPool()

    private var listener: LabResultDetailListAdapterListener? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val medicalRecordEntry = medicalRecordEntries[position]
        holder.onBind(medicalRecordEntry, position)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLabResultDetailTestListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = medicalRecordEntries.size

    fun setMedicalRecordEntries(medicalRecordEntries: ArrayList<MedicalRecordEntry>) {
        this.medicalRecordEntries.clear()
        this.medicalRecordEntries.addAll(medicalRecordEntries)
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemLabResultDetailTestListBinding) : RecyclerView.ViewHolder(binding.root) {

        private val labResultDetailTestNameTV = binding.labResultDetailTestNameTV

        //        private val labResultDetailInterpretationTV = binding.labResultDetailInterpretationTV
        private val labResultDetailShowMoreBtn = binding.labResultDetailShowMoreBtn
        private val labResultDetailListParamRV = binding.labResultDetailListParamRV
        private val labResultDetailCardView = binding.labResultDetailCardView
        private val labResultDetailListHeading = binding.labResultDetailListHeading

        private val cardMinHeight = binding.root.resources.getDimensionPixelSize(R.dimen.lab_result_detail_test_min_width)

        private val paramItemHeight = binding.root.resources.getDimensionPixelSize(R.dimen.lab_result_detail_param_layout_height)

        private var totalSubItemCount = 0

        fun onBind(medicalRecordEntry: MedicalRecordEntry, position: Int) {
            labResultDetailTestNameTV.text = medicalRecordEntry.medicalRecord?.medicalRecordName

//            if (medicalRecordEntry.isAbnormalResult == true){
//                labResultDetailInterpretationTV.setText(R.string.lab_result_interpretation_abnormal)
//                labResultDetailInterpretationTV.setTextColor(ContextCompat.getColor(itemView.context , R.color.labResultInterpretationAbnormalTxtColor))
//            }
//            else{
//                labResultDetailInterpretationTV.setText(R.string.lab_result_interpretation_normal)
//                labResultDetailInterpretationTV.setTextColor(ContextCompat.getColor(itemView.context , R.color.labResultInterpretationNormalTxtColor))
//            }

            val childLayoutManager = LinearLayoutManager(labResultDetailListParamRV.context, RecyclerView.VERTICAL, false)

            totalSubItemCount = medicalRecordEntry.mrParamValueMapList.size



            if (totalSubItemCount == 0) {
                labResultDetailShowMoreBtn.visibility = View.GONE
            } else {
                labResultDetailShowMoreBtn.visibility = View.VISIBLE
                if (medicalRecordEntry.isViewOpened) {
                    labResultDetailCardView.requestLayout()
                    labResultDetailListParamRV.visibility = View.VISIBLE
                    labResultDetailShowMoreBtn.rotation = 180f
                } else {
                    labResultDetailShowMoreBtn.rotation = 0f
                }

                labResultDetailListHeading.setOnClickListener {

                    if (medicalRecordEntry.viewHeight != 0) {

                        var cardHeight = cardMinHeight

                        if (!medicalRecordEntry.isViewOpened) {
                            cardHeight = /*cardMinHeight + (paramItemHeight * totalSubItemCount)*/ (medicalRecordEntry.viewHeight)
                            labResultDetailListParamRV.visibility = View.VISIBLE
                            labResultDetailShowMoreBtn.animate().rotation(180f).start()
                            medicalRecordEntry.isViewOpened = true
                        } else {
                            labResultDetailShowMoreBtn.animate().rotation(0f).start()
                            labResultDetailListParamRV.visibility = View.GONE
                            medicalRecordEntry.isViewOpened = false
                        }

                        val valueAnimator = ValueAnimator.ofInt(labResultDetailCardView.measuredHeight, cardHeight)
                        valueAnimator.addUpdateListener { valueAnimatorValue ->
                            labResultDetailCardView.layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
                            labResultDetailCardView.requestLayout()
                        }
                        valueAnimator.duration = 400
                        valueAnimator.start()
                    }
                }

            }

            labResultDetailListParamRV.apply {
                layoutManager = childLayoutManager
                val labResultDetailParamAdapter = LabResultDetailParamAdapter(medicalRecordEntry.mrParamValueMapList)
                labResultDetailParamAdapter.setLabResultDetailPlottingListener(object :
                    LabResultDetailParamAdapter.LabResultDetailPlottingListener {
                    override fun labResultDetailPlot(
                        paramName: String, referenceText: String?, recordUnit: String?
                    ) {
                        listener?.labResultDetailPlot(paramName, referenceText, recordUnit)
                    }

                })
                adapter = labResultDetailParamAdapter
                setRecycledViewPool(viewPool)
            }


            if (medicalRecordEntry.viewHeight == 0) {
                labResultDetailListParamRV.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {

                    override fun onGlobalLayout() {
                        // Ensure you call it only once :
                        labResultDetailListParamRV.viewTreeObserver.removeOnGlobalLayoutListener(
                            this
                        )

                        val labResultDetailCardViewHeight = labResultDetailListParamRV.height

                        var extra1LinesInRef = 0
                        var extra2LinesInRef = 0
                        var extra3LinesInRef = 0

                        medicalRecordEntry.mrParamValueMapList.forEach {
                            if (it.medicalRecordValue?.referenceMax.isNullOrBlank() || it.medicalRecordValue?.referenceMin.isNullOrBlank()) {
                                val range = it.medicalRecordValue?.range ?: ""

                                val count = range.count { char -> char == '\n' }

                                if (count == 1) {
                                    extra1LinesInRef++
                                } else if (count in 2..4) {
                                    extra2LinesInRef++
                                } else {
                                    extra3LinesInRef++
                                }
                            }
                        }


                        if (medicalRecordEntry.viewHeight == 0 && labResultDetailCardViewHeight != 0) {
                            medicalRecordEntry.viewHeight =
                                labResultDetailCardViewHeight + cardMinHeight + (/*medicalRecordEntry.mrParamValueMapList.size **/ 20) + (extra2LinesInRef * 55) + (extra1LinesInRef * 10) + (extra3LinesInRef * 85)
                            var cardHeight = cardMinHeight

                            if (medicalRecordEntry.isViewOpened) {
                                cardHeight = /*cardMinHeight + (paramItemHeight * totalSubItemCount)*/(medicalRecordEntry.viewHeight)
                                labResultDetailListParamRV.visibility = View.VISIBLE
                                labResultDetailShowMoreBtn.animate().rotation(180f).start()
                            } else {
                                labResultDetailListParamRV.visibility = View.GONE
                                labResultDetailShowMoreBtn.animate().rotation(0f).start()
                            }

                            labResultDetailCardView.layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
                            labResultDetailCardView.requestLayout()

                            var canCallbackBeSent = true
                            medicalRecordEntries.forEach {
                                if (it.viewHeight == 0) {
                                    canCallbackBeSent = false
                                    return@forEach
                                }
                            }
                            if (canCallbackBeSent) {

                                medicalRecordEntries.forEachIndexed { index, medicalRecordEntry ->

                                    if (index == 0) {
                                        medicalRecordEntry.isViewOpened = medicalRecordEntry.mrParamValueMapList.isNotEmpty()
                                    } else {
                                        medicalRecordEntry.isViewOpened = false
                                    }
                                }
                                notifyDataSetChanged()

                                listener?.notifyLabResultHeightIsTaken()

                            }
                        }

                    }
                })

            } else {
                var cardHeight = cardMinHeight

                if (medicalRecordEntry.isViewOpened) {
                    cardHeight = /*cardMinHeight + (paramItemHeight * totalSubItemCount)*/medicalRecordEntry.viewHeight
                    labResultDetailListParamRV.visibility = View.VISIBLE
                    labResultDetailShowMoreBtn.animate().rotation(180f).start()
                } else {
                    labResultDetailListParamRV.visibility = View.GONE
                    labResultDetailShowMoreBtn.animate().rotation(0f).start()
                }

                labResultDetailCardView.layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
                labResultDetailCardView.requestLayout()
            }

        }
    }

    fun setLabResultDetailListAdapterListener(labResultDetailListAdapterListener: LabResultDetailListAdapterListener) {
        listener = labResultDetailListAdapterListener
    }

    interface LabResultDetailListAdapterListener {
        fun notifyLabResultHeightIsTaken()
        fun labResultDetailPlot(
            paramName: String, referenceText: String?, recordUnit: String?
        )
    }

}