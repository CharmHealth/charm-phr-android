package com.charm.phr.mphr.ui.document

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemDocumentListBinding
import com.charm.phr.mphr.model.Document
import com.charm.phr.mphr.util.InfiniteScrollViewHolder
import com.charm.phr.mphr.util.ModelUtil

class DocumentListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var documentList: ArrayList<Document?> = arrayListOf()

    private var documentSelectListener: DocumentSelectListener? = null

    var isSelectionMode: Boolean = false

    var documentIdList: ArrayList<String> = arrayListOf()

    var isSharedByMe: Boolean = false

    private val viewTypeDocument = 1
    private val viewTypeScroll = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder = if (viewType == viewTypeDocument) {
            val binding = ItemDocumentListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ViewHolder(binding)
        } else {
            InfiniteScrollViewHolder.getNewInstance(parent)
        }

        return viewHolder
    }

    override fun getItemCount(): Int = documentList.size

    override fun getItemViewType(position: Int): Int {
        return if (documentList[position] != null) {
            viewTypeDocument
        } else {
            viewTypeScroll
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val document = documentList[position]

        if (document != null && holder is ViewHolder) {

            holder.onBind(document)
            /*holder.itemView.setOnLongClickListener {
                if (!isMultiSelectEnabled) {
                    isMultiSelectEnabled = true
                    problemMultiSelectListener?.onMultiSelect(true)
                    problems.isSelected = true
                    holder.changeBgToSelected()
                }
                else {
                    if (!problems.isSelected) {
                        problems.isSelected = true
                        holder.changeBgToSelected()
                    } else {
                        problems.isSelected = true
                        holder.changeBgToSelected()
                    }
                }

                return@setOnLongClickListener true
            }*/


            holder.itemView.setOnClickListener {
                /* if (isMultiSelectEnabled) {
                     if (problems.isSelected) {
                         problems.isSelected = false
                         holder.changeBgToUnSelected()
                         checkAllIsUnchecked()
                     } else {
                         problems.isSelected = true
                         holder.changeBgToSelected()
                     }
                 } else {*/

                // }


                if (isSelectionMode) {
                    if (holder.binding.documentCardView.isSelected == true) {
                        holder.binding.documentCardView.setBackgroundResource(R.color.white)
                        holder.binding.documentCardView.isSelected = false
                        documentSelectListener?.onUnSelect(document, position)
                        documentIdList.remove(document.fileId ?: "")
                    } else {
                        if (documentIdList.size < 10) {
                            holder.binding.documentCardView.setBackgroundResource(R.color.cardSelectedBgColor)
                            holder.binding.documentCardView.isSelected = true
                            documentSelectListener?.onSelect(document, position)
                            documentIdList.add(document.fileId ?: "")
                        } else {
                            documentSelectListener?.showToastMessage()
                        }
                    }
                } else {
                    documentSelectListener?.onSelect(document, position)
                }
                if (!document.readStatus) {
                    document.readStatus = true
                    holder.changeItemToRead()
                }
            }

            if (!isSharedByMe) {
                holder.itemView.setOnLongClickListener {
                    //  if (document.isAddedByPatient) {
                    if (!isSelectionMode) {
                        holder.binding.documentCardView.setBackgroundResource(R.color.cardSelectedBgColor)
                        holder.binding.documentCardView.isSelected = true
                        documentSelectListener?.onSelection(document, position)
                        documentIdList.clear()
                        documentIdList.add(document.fileId ?: "")
                    } else {
                        if (holder.binding.documentCardView.isSelected == false) {
                            if (documentIdList.size < 10) {
                                holder.binding.documentCardView.setBackgroundResource(R.color.cardSelectedBgColor)
                                holder.binding.documentCardView.isSelected = true
                                documentSelectListener?.onSelection(document, position)
                                documentIdList.add(document.fileId ?: "")
                            } else {
                                documentSelectListener?.showToastMessage()
                            }
                        }
                    }
                    //   }
                    return@setOnLongClickListener true
                }
            }
        }
    }

    fun setDocuments(document: ArrayList<Document?>): Int {
        this.documentIdList.clear()
        this.documentList.clear()
        this.documentList.addAll(document)
        this.notifyDataSetChanged()
        return this.documentList.size
    }

    fun addDocuments(document: ArrayList<Document?>) {
        this.documentList.addAll(document)
        this.notifyDataSetChanged()
    }

    fun removeDocument(document: Document): Int {
        this.documentList.remove(document)
        this.notifyDataSetChanged()
        this.documentIdList.remove(document.fileId)
        return documentList.size
    }

    fun addInfiniteScroll() {
        documentList.add(null)
        notifyItemInserted(documentList.size - 1)
    }

    fun removeInfiniteScroll() {
        documentList.removeAt(documentList.size - 1)
        val scrollPosition = documentList.size
        notifyItemRemoved(scrollPosition)
    }

    fun setDocumentSelectListener(listener: DocumentSelectListener) {
        documentSelectListener = listener
    }

    inner class ViewHolder(val binding: ItemDocumentListBinding) : RecyclerView.ViewHolder(binding.root) {

    val documentDateTV: TextView = binding.documentDateTV
    val documentNameTV: TextView = binding.documentNameTV
    val documentSharedToTV: TextView = binding.documentSharedToTV
    val documentSharedToLabel: TextView = binding.documentSharedToLabel
    val documentSharedByTV: TextView = binding.documentSharedByTV
    val documentSharedByLabel: TextView = binding.documentSharedByLabel
    val documentSeparator = binding.documentSeparator

    val documentLayout: ConstraintLayout = binding.documentLayout
    val documentHPLayout: ConstraintLayout = binding.documentHPLayout
    val documentHPNameTV: TextView = binding.documentHPNameTV
    val documentHPStatusTV: TextView = binding.documentHPTypeTV
    val problemConstraintLayout: ConstraintLayout = binding.documentConstraintLayout
    val documentCardView: CardView = binding.documentCardView


        private val handler = Handler(Looper.getMainLooper())
        private val runnable = Runnable {
            problemConstraintLayout.setBackgroundResource(R.drawable.item_ripple_white_bg)
        }

        fun onBind(document: Document) {


            if (document.isHealthPassport) {
                /*  documentLayout.visibility = View.GONE
                  documentHPLayout.visibility = View.VISIBLE
                  problemHPDateTV.text = problems.dateOfOnset?.toString(ModelUtil.charmDisplayDateFormat)
                */
                documentHPLayout.visibility = View.VISIBLE
                documentLayout.visibility = View.GONE
                documentHPNameTV.text = document.fileName
                documentHPStatusTV.text = document.category?.categoryName
            } else {

                documentHPLayout.visibility = View.GONE
                documentLayout.visibility = View.VISIBLE

                documentDateTV.text = document.date?.toString(ModelUtil.charmDisplayDateFormat())

                documentNameTV.text = document.fileName

                if (document.sentBy.isNullOrBlank() && document.sharedToList.isEmpty()) {
                    documentSeparator.visibility = View.GONE
                } else {
                    documentSeparator.visibility = View.VISIBLE
                }


                if (!document.sentBy.isNullOrBlank()) {
                    documentSharedByLabel.visibility = View.VISIBLE
                    documentSharedByTV.visibility = View.VISIBLE
                    documentSharedByTV.text = document.sentBy
                } else {
                    documentSharedByLabel.visibility = View.GONE
                    documentSharedByTV.visibility = View.GONE
                }

                if (document.sharedToList.isNotEmpty()) {
                    documentSharedToLabel.visibility = View.VISIBLE
                    documentSharedToTV.visibility = View.VISIBLE
                    documentSharedToTV.text = document.sharedToList.joinToString(", ")
                } else {
                    documentSharedToLabel.visibility = View.GONE
                    documentSharedToTV.visibility = View.GONE
                }


                if (document.readStatus) {
                    changeItemToRead()
                } else {
                    changeItemToNotRead()
                }


                /* documentLayout.visibility = View.VISIBLE
                 documentHPLayout.visibility = View.GONE

                 problemDateTV.text = problems.dateOfOnset?.toString(ModelUtil.charmDisplayDateFormat)


                 documentNameTV.text = if (problems.isAddedByPatient) {
                     problems.medicalCondition?.medicalConditionName
                 }
                 else{
                     problems.diagnosis?.diagnosisLong
                 }

                 if (problems.notes.isNullOrBlank()){
                     documentReasonTV.visibility = View.GONE
                 }
                 else{
                     documentReasonTV.visibility = View.VISIBLE
                     documentReasonTV.text = problems.notes
                 }*/

            }
            if (documentIdList.contains(document.fileId)) {
                documentCardView.setBackgroundResource(R.color.cardSelectedBgColor)
                documentCardView.isSelected = true
            } else {
                documentCardView.setBackgroundResource(R.color.white)
                documentCardView.isSelected = false
            }

        }

        private fun changeItemToNotRead() {
            TextViewCompat.setTextAppearance(documentNameTV, R.style.itemIsNotRead)
            TextViewCompat.setTextAppearance(documentSharedByTV, R.style.itemIsNotRead)
            TextViewCompat.setTextAppearance(documentSharedToTV, R.style.itemIsNotRead)

        }

        fun changeItemToRead() {
            TextViewCompat.setTextAppearance(documentNameTV, R.style.itemIsRead)
            TextViewCompat.setTextAppearance(documentSharedByTV, R.style.itemIsRead)
            TextViewCompat.setTextAppearance(documentSharedToTV, R.style.itemIsRead)
        }

        fun changeBgToSelected() {

            handler.removeCallbacks(runnable)
            problemConstraintLayout.setBackgroundResource(R.color.cardSelectedBgColor)
        }

        fun changeBgToUnSelected() {
            problemConstraintLayout.setBackgroundResource(android.R.color.white)
            handler.postDelayed(runnable, ModelUtil.delayedTimingForRippleChange)
        }

    }

    interface DocumentSelectListener {
        fun onDelete(document: Document, position: Int)
        fun onSelect(document: Document, position: Int)
        fun onUnSelect(document: Document, position: Int)
        fun onSelection(document: Document, position: Int)
        fun showToastMessage()
    }

}