// File: AlternativeFragment.kt
package com.google.ar.sceneform.samples.gltf.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.ar.sceneform.samples.gltf.R

class ReciteFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recite, container, false)
    }
}