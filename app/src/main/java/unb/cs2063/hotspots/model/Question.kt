package unb.cs2063.hotspots.model

class Question(private val title: String?, private val description: String?){


    /*
    public properties
     */
    val Description: String
        get() = "$description"
    val Title: String
        get() = "$title"
}
