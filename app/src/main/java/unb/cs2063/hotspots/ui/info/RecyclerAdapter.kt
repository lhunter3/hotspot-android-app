package unb.cs2063.hotspots.ui.info

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import unb.cs2063.hotspots.R
import unb.cs2063.hotspots.model.Question


class RecyclerAdapter(private val parentActivity: Activity, private val mDataset:ArrayList<Question>) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    private val TAG = "RecyclerAdapter"
    class ViewHolder(mTextView: CardView) : RecyclerView.ViewHolder(mTextView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item, parent, false) as CardView
        return  ViewHolder(v)
    }

    override fun getItemCount(): Int {
       return mDataset.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val question = mDataset[position]

        Log.i(TAG,holder.itemView.id.toString())
        val t = holder.itemView as CardView
        val text = t.findViewById<TextView>(R.id.faq_title)
        text.text = question.Title

        holder.itemView.setOnClickListener{
            Log.i(TAG, "Clicked CardView")

            val scale = 0.9f // The target scale
            val duration = 100L // Animation duration in milliseconds

            holder.itemView.animate()
                .scaleX(scale)
                .scaleY(scale)
                .setDuration(duration)
                .withEndAction {
                    // Restore the original scale after the animation
                    holder.itemView.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(duration)
                        .setInterpolator(AccelerateDecelerateInterpolator())
                        .start()
                }
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()

            startDetailedRecyclerActivity(question)
        }
    }

    private fun startDetailedRecyclerActivity(question: Question){

        val intent = Intent(parentActivity,RecyclerDetailActivity::class.java)
        Log.i(TAG,question.Title)
        intent.putExtra("title", question.Title)
        intent.putExtra("description", question.Description)

        parentActivity.startActivity(intent)

    }

}