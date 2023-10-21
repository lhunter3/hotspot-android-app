package unb.cs2063.hotspots.ui.info

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import unb.cs2063.hotspots.R
import unb.cs2063.hotspots.model.Question


class RecyclerAdapter(private val parentActivity: Activity, private val mDataset:ArrayList<Question>) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    private val TAG = "RecyclerAdapter"
    class ViewHolder(mTextView: TextView) : RecyclerView.ViewHolder(mTextView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item, parent, false) as TextView
        return  ViewHolder(v)
    }

    override fun getItemCount(): Int {
       return mDataset.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val question = mDataset[position]
        val t = holder.itemView as TextView
        t.text = question.Title

        holder.itemView.setOnClickListener{
            val intent = Intent(parentActivity,RecyclerDetailActivity::class.java)
            intent.putExtra("title", question.Title)
            intent.putExtra("description", question.Description)
        }
    }

}