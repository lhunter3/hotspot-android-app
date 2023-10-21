package unb.cs2063.hotspots.ui.info

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import unb.cs2063.hotspots.R
import unb.cs2063.hotspots.databinding.FragmentInfoBinding
import unb.cs2063.hotspots.utils.JsonUtils
import unb.cs2063.hotspots.utils.LoadDataTask


class InfoFragment : Fragment() {


    private val TAG = "InfoFragment"



    private var binding: FragmentInfoBinding? = null
    private val Binding get() = binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        //setup bindings
        binding = FragmentInfoBinding.inflate(inflater, container, false)
        val root: View = Binding.root

        /*

        //dataTask.execute()
        setup recyclerView
        Binding.recycler.layoutManager = LinearLayoutManager(activity)
        println(uti.getQuestions().toString())
        //Binding.recyclerView.adapter = RecyclerAdapter(requireActivity(),uti.getQuestions())


        */




        //Contact Button
        Binding.textInfo.text = resources.getString(R.string.contact_us_title)
        Binding.btnContact.text = resources.getString(R.string.btn_contact_us)
        Binding.btnContact.setOnClickListener {
            val emails = arrayOf(resources.getString(R.string.contact_email))
            val subject = resources.getString(R.string.contact_subject)
            composeEmail(emails, subject)
            Log.i(TAG, "Contact Button Clicked")
        }

        return root
    }

    /*
    Creates an email intent.
     */
    fun composeEmail(addresses: Array<String>, subject: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_EMAIL, addresses)
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}


