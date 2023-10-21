package unb.cs2063.hotspots.ui.info

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import unb.cs2063.hotspots.R
import unb.cs2063.hotspots.databinding.FragmentInfoBinding


class InfoFragment : Fragment() {


    private val TAG = "InfoFragment"

    private var binding: FragmentInfoBinding? = null
    private val Binding get() = binding!!

    private var recyclerView: RecyclerView? = null




    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,savedInstanceState: Bundle?): View {

        //setup bindings
        binding = FragmentInfoBinding.inflate(inflater, container, false)
        val root: View = Binding.root


        /*
        TODO
        setup recyclerView
        Binding.recycler.layoutManager = LinearLayoutManager(activity)
        */


        //Contact Button
        Binding.textInfo.text = resources.getString(R.string.contact_us_title)
        Binding.btnContact.text = resources.getString(R.string.btn_contact_us)
        Binding.btnContact.setOnClickListener{
            val emails = arrayOf(resources.getString(R.string.contact_email))
            val subject = resources.getString(R.string.contact_subject)
            composeEmail(emails, subject)
            Log.i(TAG,"Contact Button Clicked")
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


