package com.charm.phr.mphr.ui.problem_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.charm.phr.mphr.R
import dagger.android.support.DaggerFragment


class HistoryProblemListFragment : DaggerFragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history_problem_list, container, false)
    }

}