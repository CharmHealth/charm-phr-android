package com.charm.phr.mphr.ui.dashboard

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.charm.phr.mphr.model.SectionConfigList

class DashboardViewPagerAdapter (fa : FragmentActivity ):FragmentStateAdapter(fa) {

    private var fragments: ArrayList<Fragment> = arrayListOf(HomeFragment(), HealthDataFragment())


    fun updateFeeds() {

        try {
            val homeFragment = fragments[0]
            if (homeFragment is HomeFragment) {
                homeFragment.fetchFeeds()
            }
        } catch (e: Exception) {
            Log.d("updateFeeds", e.toString())
        }

    }

    fun updateCallback(callback: Any? , isFromWeb: Boolean = false){



            fragments.forEach{
                try {

                    val dashboardFragment = it

                    if (dashboardFragment is DashboardActivity.DashboardFragmentsCallback) {
                        Log.d("SectionConfigList", callback.toString() ?: "S")
                        if (callback is SectionConfigList) {
                            Log.d("SectionConfigList", "update ")

                            dashboardFragment.startPopulate(callback, isFromWeb)
                        } else if (callback is DashboardCallback) {

                            Log.d("MDProgress", "$callback")

                            if (callback == DashboardCallback.PROGRESS) {
                                dashboardFragment.showProgress()
                                Log.d("MDProgress", "progress in adapter")
                            } else {
                                dashboardFragment.showErrorPage()
                            }
                        } else if (callback == null) {
                            dashboardFragment.startPopulate(callback, isFromWeb)
                        }

                    }
                }
                catch (e:NullPointerException){
                    Log.d("MDProgress", "NULL: ${e}")

                }

                catch (e:IllegalStateException){
                    Log.d("MDProgress", "Illegal: ${e}")

                }
                catch (e: UninitializedPropertyAccessException){
                    Log.d("MDProgress", "UnIntitialized: ${e}")
                }
            }

    }


    fun updateScroll(toBlock: Boolean){

        try {

            val homeFragment = fragments[0]

            if(homeFragment is HomeFragment){
                if(toBlock) {
                    homeFragment.blockHomeScrollView()
                }
                else{
                    homeFragment.unblockHomeScrollView()
                }
            }
        }
        catch (e:NullPointerException){

        }

        catch (e:IllegalStateException){

        }
        catch (e: UninitializedPropertyAccessException){

        }
    }

    enum class DashboardCallback{PROGRESS , ERROR , DATA}

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

}