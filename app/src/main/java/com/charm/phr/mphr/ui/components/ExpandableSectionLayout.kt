package com.charm.phr.mphr.ui.components

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.charm.phr.mphr.databinding.ItemQuestionnaireSectionHeaderTemplateBinding
import com.charm.phr.mphr.util.ModelUtil

class ExpandableSectionLayout constructor(context: Context) : LinearLayout(context) {

    private var isExpanded = false
    private var headerView: ItemQuestionnaireSectionHeaderTemplateBinding? = null
    private var contentView: LinearLayout? = null


    init {
        orientation = VERTICAL
        layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        isFocusable = true
    }


    fun setHeaderView(headerView: ItemQuestionnaireSectionHeaderTemplateBinding, isCollapsible: Boolean) {
        this.headerView = headerView
        addView(headerView.root)
        if (isCollapsible) {
            headerView.root.setOnClickListener {
                toggleExpandCollapse()
            }
        } else {
            headerView.quesSectionCollapseImg.visibility = View.GONE
        }
    }

    fun setContentView(contentView: LinearLayout, isExpanded: Boolean = false) {
        this.contentView = contentView
        addView(contentView)
        this.isExpanded = isExpanded
        if (!isExpanded) {
            contentView.visibility = View.GONE
            headerView?.quesSectionSeparatorView?.visibility=View.GONE
        }
        else
        {
            headerView?.quesSectionCollapseImg?.animate()?.rotation(180.0f)?.start()
        }
    }

    fun getIsExpanded(): Boolean {
        return isExpanded
    }

    fun setExpanded(isExpanded: Boolean)
    {
        this.isExpanded=isExpanded
    }

    fun getHeaderView(): ItemQuestionnaireSectionHeaderTemplateBinding? {
        return headerView
    }

    fun getContentView(): LinearLayout? {
        return contentView
    }

    private fun toggleExpandCollapse() {
        if (isExpanded) {
             collapse()
        } else {
             expand()
        }
    }

    private fun expand() {
        contentView?.let {contentView->
            val screenHeight = context.resources.displayMetrics.heightPixels

            val screenLocation = intArrayOf(0, 0)
            contentView.getLocationInWindow(screenLocation)

            val maxHeight = screenHeight - screenLocation[1]

            val heightAnimator = ValueAnimator.ofInt(contentView.measuredHeight, maxHeight)
            heightAnimator.addUpdateListener {
                val layoutParams = contentView.layoutParams
                layoutParams?.height = LayoutParams.WRAP_CONTENT
                contentView.layoutParams = layoutParams
                headerView?.quesSectionCollapseImg?.animate()?.rotation(180.0f)?.start()
            }
            heightAnimator.addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    headerView?.quesSectionSeparatorView?.visibility=View.VISIBLE
                    contentView.visibility = View.VISIBLE
                    isExpanded = true
                }
            })

            heightAnimator.duration = ModelUtil.animationDuration
            heightAnimator.start()
        }
    }

    private fun collapse() {
        contentView?.let { contentView ->
            val heightAnimator = ValueAnimator.ofInt(contentView.measuredHeight, headerView?.root?.measuredHeight ?: 0)
            heightAnimator.addUpdateListener { valueAnimator ->
                val animVal = valueAnimator.animatedValue as Int
                val layoutParams = contentView.layoutParams
                layoutParams?.height = animVal
                contentView.layoutParams = layoutParams
                headerView?.quesSectionCollapseImg?.animate()?.rotation(0.0f)?.start()
            }

            heightAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    headerView?.quesSectionSeparatorView?.visibility=View.GONE
                    contentView.visibility = View.GONE
                    isExpanded = false
                }
            })
            heightAnimator.duration = ModelUtil.animationDuration
            heightAnimator.start()
        }
    }


}
