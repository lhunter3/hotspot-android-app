package unb.cs2063.hotspots.ui.info

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import unb.cs2063.hotspots.R

class RecyclerDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        val desc = intent.getStringExtra("description")
        val title = intent.getStringExtra("title")

        findViewById<TextView>(R.id.faqDetails_description).text = desc

        supportActionBar?.title = title
    }

}