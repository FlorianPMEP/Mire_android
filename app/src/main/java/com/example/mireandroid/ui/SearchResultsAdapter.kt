package com.example.mireandroid.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mireandroid.R
import com.example.mireandroid.api.Dossier
import com.example.mireandroid.api.Email
import com.example.mireandroid.api.Organisation
import com.example.mireandroid.api.Person
import com.example.mireandroid.api.Phone
import kotlin.math.abs

class SearchResultsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val searchResults: MutableList<Any> = mutableListOf()

    override fun getItemViewType(position: Int): Int {
        return when (searchResults[position]) {
            is Person -> VIEW_TYPE_PERSON
            is Organisation -> VIEW_TYPE_ORGANISATION
            is Dossier -> VIEW_TYPE_DOSSIER
            is Phone -> VIEW_TYPE_PHONE
            is Email -> VIEW_TYPE_EMAIL
            else -> {
                Log.e("SearchResultsAdapter", "Invalid type of data at position $position: ${searchResults[position]::class.java.simpleName}")
                throw IllegalArgumentException("Invalid type of data $position")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_PERSON -> PersonViewHolder(inflater.inflate(R.layout.item_person, parent, false))
            VIEW_TYPE_ORGANISATION -> OrganisationViewHolder(inflater.inflate(R.layout.item_organisation, parent, false))
            VIEW_TYPE_DOSSIER -> DossierViewHolder(inflater.inflate(R.layout.item_dossier, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PersonViewHolder -> holder.bind(searchResults[position] as Person)
            is OrganisationViewHolder -> holder.bind(searchResults[position] as Organisation)
            is DossierViewHolder -> holder.bind(searchResults[position] as Dossier)
        }
    }

    override fun getItemCount(): Int = searchResults.size

    fun updateResults(results: List<Any>) {
        val flattenedResults = flattenResults(results)
        searchResults.clear()
        searchResults.addAll(flattenedResults)
        notifyDataSetChanged()
    }

    private fun flattenResults(results: List<Any>): List<Any> {
        val flattenedList = mutableListOf<Any>()
        results.forEach { item ->
            if (item is List<*>) {
                flattenedList.addAll(flattenResults(item.filterNotNull()))
            } else {
                flattenedList.add(item)
            }
        }
        return flattenedList
    }

    companion object {
        private const val VIEW_TYPE_PERSON = 0
        private const val VIEW_TYPE_ORGANISATION = 1
        private const val VIEW_TYPE_DOSSIER = 2
        private const val VIEW_TYPE_PHONE = 3
        private const val VIEW_TYPE_EMAIL = 4
    }

    // ViewHolder classes for each data type
    class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.name_text_view)
        private val firstNameTextView: TextView = itemView.findViewById(R.id.first_name_text_view)

        fun bind(person: Person) {
            if (person.name.isNullOrEmpty()) {
                nameTextView.visibility = View.GONE
            } else {
                nameTextView.text = person.name
                nameTextView.visibility = View.VISIBLE
            }

            if (person.first_name.isNullOrEmpty()) {
                firstNameTextView.visibility = View.GONE
            } else {
                firstNameTextView.text = person.first_name
                firstNameTextView.visibility = View.VISIBLE
            }
        }
    }


    class OrganisationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val organisationNameTextView: TextView = itemView.findViewById(R.id.organisation_name_text_view)
        private val organisationDetailsTextView: TextView = itemView.findViewById(R.id.organisation_details_text_view)

        fun bind(organisation: Organisation) {
            if (organisation.organisation_name.isNullOrEmpty()) {
                organisationNameTextView.visibility = View.GONE
            } else {
                organisationNameTextView.text = organisation.organisation_name
                organisationNameTextView.visibility = View.VISIBLE
            }

            if (organisation.adress.isNullOrEmpty()) {
                organisationDetailsTextView.visibility = View.GONE
            } else {
                organisationDetailsTextView.text = organisation.adress
                organisationDetailsTextView.visibility = View.VISIBLE
            }
        }
    }


    class DossierViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dossierNameTextView: TextView = itemView.findViewById(R.id.dossier_name_text_view)
        private val dossierDetailsTextView: TextView = itemView.findViewById(R.id.dossier_details_text_view)

        fun bind(dossier: Dossier) {
            if (dossier.dossier_name.isNullOrEmpty()) {
                dossierNameTextView.visibility = View.GONE
            } else {
                dossierNameTextView.text = dossier.dossier_name
                dossierNameTextView.visibility = View.VISIBLE
            }

            // Assuming you want to hide the details text view if the details are empty
            dossierDetailsTextView.visibility = View.GONE
        }
    }


}

