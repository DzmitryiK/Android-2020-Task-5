package com.rsschool.cats.utils

import android.widget.ImageView
import android.widget.TextView
import com.rsschool.cats.data.Cat

interface CatListener {
    fun openCat(cat: Cat, text: TextView, image: ImageView)
}
