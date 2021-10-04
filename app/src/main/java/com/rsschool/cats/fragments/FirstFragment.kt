package com.rsschool.cats.fragments

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rsschool.cats.*
import com.rsschool.cats.data.Cat
import com.rsschool.cats.databinding.FragmentFirstBinding
import com.rsschool.cats.utils.CatListener
import com.rsschool.cats.utils.CatsListAdapter
import com.rsschool.cats.viewmodel.CatViewModel


class FirstFragment : Fragment(), CatListener {

    private var _binding: FragmentFirstBinding? = null
    private val itemAdapter = CatsListAdapter(this)
    private lateinit var catViewModel: CatViewModel

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        //For fixing perspective distortion in card flip animation
        val scale = requireContext().resources.displayMetrics.density
        binding.root.cameraDistance = 8000*scale

        /*
        //For sharedElement transitions
        sharedElementEnterTransition = TransitionInflater.from(this.context).inflateTransition(R.transition.change_bounds)
        sharedElementReturnTransition = TransitionInflater.from(this.context).inflateTransition(R.transition.change_bounds)
        postponeEnterTransition()   */

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recycler.apply {
            adapter = itemAdapter
            layoutManager = LinearLayoutManager(context)
        }

        catViewModel = ViewModelProvider(requireActivity()).get(CatViewModel::class.java)
        catViewModel.items.observe(requireActivity(), Observer {
            it ?: return@Observer
            itemAdapter.submitList(it)
        })

        setRecyclerViewScrollListener(binding.recycler)

        /*
        // For sharedElement transitions
        binding.recycler
            .viewTreeObserver
            .addOnGlobalLayoutListener(
                object : OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        // At this point the layout is complete and the
                        // dimensions of recyclerView and any child views
                        // are known.
                        startPostponedEnterTransition()
                        binding.recycler
                            .viewTreeObserver
                            .removeOnGlobalLayoutListener(this)
                    }
                })*/
    }

    private fun setRecyclerViewScrollListener(recycler:RecyclerView) {
        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            private var isLoading: Boolean = false

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val isNotLoaded = itemAdapter.currentList.size == 1 && itemAdapter.currentList[0].number == 0
                if (!isLoading && !isNotLoaded) {
                    val lastVisible: Int =
                        (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                    if (lastVisible == itemAdapter.currentList.size - 1) {
                        isLoading = true
                        catViewModel.addCats().invokeOnCompletion { isLoading = false }
                    }
                }
            }
        })
    }

    override fun openCat(cat: Cat, text: TextView, image: ImageView) {

        /*
        //For sharedElement transitions
        val extras = FragmentNavigatorExtras(
            text to cat.number.toString(),
            image to cat.imageUrl!!
        )*/
		val bundle = Bundle()
            bundle.putString("number", cat.number.toString())
            bundle.putString("id", cat.id.toString())
            bundle.putString("image", cat.imageUrl)
            bundle.putString("title", String.format(getString(R.string.cat_item_title),cat.number.toString()))

        findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment, bundle, null, /*extras*/null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
