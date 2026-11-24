package com.charm.phr.mphr.ui.message.compose

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.annotation.NonNull
import java.util.Arrays

abstract class FilteredArrayAdapter<T>(
    context: Context,
    resource: Int,
    textViewResourceId: Int,
    private val originalObjects: ArrayList<T>
) :
    ArrayAdapter<T>(context, resource, textViewResourceId, ArrayList<T>(originalObjects)) {
    private var filter: Filter? = null

    /**
     * Constructor
     *
     * @param context The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     * instantiating views.
     * @param objects The objects to represent in the ListView.
     */
    constructor(context: Context, resource: Int, objects: Array<T>) : this(
        context,
        resource,
        0,
        objects
    ) {
    }

    /**
     * Constructor
     *
     * @param context The current context.
     * @param resource The resource ID for a layout file containing a layout to use when
     * instantiating views.
     * @param textViewResourceId The id of the TextView within the layout resource to be populated
     * @param objects The objects to represent in the ListView.
     */
    constructor(context: Context, resource: Int, textViewResourceId: Int, objects: Array<T>) : this(
        context, resource, textViewResourceId, ArrayList<T>(
            Arrays.asList<T>(*objects)
        )
    ) {
    }

    /**
     * Constructor
     *
     * @param context The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     * instantiating views.
     * @param objects The objects to represent in the ListView.
     */
    constructor(context: Context, resource: Int, objects: ArrayList<T>) : this(
        context,
        resource,
        0,
        objects
    ) {
    }

    @NonNull
    override fun getFilter(): Filter {
        if (filter == null)
            filter = AppFilter()
        return filter!!
    }

    /**
     * Filter method used by the adapter. Return true if the object should remain in the list
     *
     * @param obj object we are checking for inclusion in the adapter
     * @param mask current text in the edit text we are completing against
     * @return true if we should keep the item in the adapter
     */
    protected abstract fun keepObject(obj: T, mask: String): Boolean

    /**
     * Class for filtering Adapter, relies on keepObject in FilteredArrayAdapter
     *
     * based on gist by Tobias Schürg
     * in turn inspired by inspired by Alxandr
     * (http://stackoverflow.com/a/2726348/570168)
     */

    fun setItems(items:ArrayList<T>){
        originalObjects.clear()
        originalObjects.addAll(items)
        notifyDataSetChanged()
    }

    private inner class AppFilter : Filter() {

        override fun performFiltering(chars: CharSequence?): Filter.FilterResults {
            val sourceObjects = ArrayList(originalObjects)

            val result = Filter.FilterResults()
            if (chars != null && chars.length > 0) {
                val mask = chars.toString()
                val keptObjects = ArrayList<T>()

                for (`object` in sourceObjects) {
                    if (keepObject(`object`, mask))
                        keptObjects.add(`object`)
                }
                result.count = keptObjects.size
                result.values = keptObjects
            } else {
                // add all objects
                result.values = sourceObjects
                result.count = sourceObjects.size
            }
            return result
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            clear()
            if (results?.count ?: 0 > 0) {
                this@FilteredArrayAdapter.addAll(results?.values as Collection<T>)
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }
        }
    }
}