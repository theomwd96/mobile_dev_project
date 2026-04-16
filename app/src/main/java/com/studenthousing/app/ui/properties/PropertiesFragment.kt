package com.studenthousing.app.ui.properties

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.studenthousing.app.R
import com.studenthousing.app.StudentHousingApp
import com.studenthousing.app.data.repo.ResultState
import com.studenthousing.app.databinding.FragmentPropertiesBinding
import com.studenthousing.app.ui.CommonViewModelFactory
import com.studenthousing.app.util.NetworkUtils

class PropertiesFragment : Fragment(R.layout.fragment_properties) {

    private var _binding: FragmentPropertiesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PropertiesViewModel
    private lateinit var adapter: PropertyAdapter

    // Store last selected campus from filter so the near-campus switch can use it
    private var lastCampusLat: Double = 0.0
    private var lastCampusLng: Double = 0.0

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Snackbar.make(binding.root, "Location enabled", Snackbar.LENGTH_SHORT).show()
            adapter.submitList(viewModel.sortByDistanceToCampus(true, lastCampusLat, lastCampusLng))
        } else {
            binding.nearCampusSwitch.isChecked = false
            Snackbar.make(
                binding.root,
                "Location permission required for this feature",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPropertiesBinding.bind(view)

        val app = requireContext().applicationContext as StudentHousingApp
        viewModel = ViewModelProvider(
            this,
            CommonViewModelFactory(app.container.repository)
        )[PropertiesViewModel::class.java]

        adapter = PropertyAdapter { property ->
            findNavController().navigate(
                R.id.action_properties_to_detail,
                Bundle().apply { putString("property_id", property.id) }
            )
        }
        binding.propertiesRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.propertiesRecycler.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { loadProperties() }

        // Owner vs Student button
        val isOwner = app.container.tokenStore.cachedUserType == "owner"
        if (isOwner) {
            binding.bookingsButton.text = getString(R.string.add_property)
            binding.bookingsButton.setOnClickListener {
                findNavController().navigate(R.id.addPropertyFragment)
            }
        } else {
            binding.bookingsButton.setOnClickListener {
                findNavController().navigate(R.id.action_properties_to_bookings)
            }
        }

        // Filter button
        binding.filterButton.setOnClickListener {
            FilterBottomSheet().show(childFragmentManager, "filter")
        }

        // Listen for filter results — now includes campus + distance
        childFragmentManager.setFragmentResultListener(
            FilterBottomSheet.FILTER_REQUEST_KEY, viewLifecycleOwner
        ) { _, bundle ->
            val type      = bundle.getString(FilterBottomSheet.KEY_TYPE, "")
            val minRooms  = bundle.getInt(FilterBottomSheet.KEY_MIN_ROOMS, 0)
            val maxPrice  = bundle.getDouble(FilterBottomSheet.KEY_MAX_PRICE, 0.0)
            val campusLat = bundle.getDouble(FilterBottomSheet.KEY_CAMPUS_LAT, 0.0)
            val campusLng = bundle.getDouble(FilterBottomSheet.KEY_CAMPUS_LNG, 0.0)
            val maxDist   = bundle.getDouble(FilterBottomSheet.KEY_MAX_DIST_KM, 0.0)

            // Save campus coordinates for the near-campus switch
            lastCampusLat = campusLat
            lastCampusLng = campusLng

            viewModel.applyFilters(
                type          = type.ifBlank { null },
                minRooms      = if (minRooms > 0) minRooms else null,
                maxPrice      = if (maxPrice > 0.0) maxPrice else null,
                campusLat     = if (campusLat != 0.0) campusLat else null,
                campusLng     = if (campusLng != 0.0) campusLng else null,
                maxDistanceKm = if (maxDist > 0.0) maxDist else null
            )
        }

        // Near campus switch — sorts by distance if campus was selected in filter
        binding.nearCampusSwitch.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                if (hasPermission) {
                    adapter.submitList(
                        viewModel.sortByDistanceToCampus(true, lastCampusLat, lastCampusLng)
                    )
                } else {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            } else {
                adapter.submitList(viewModel.sortByDistanceToCampus(false))
            }
        }

        // Search with debounce
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.search(s?.toString().orEmpty())
            }
        })

        viewModel.offline.observe(viewLifecycleOwner) { offline ->
            binding.offlineBanner.visibility = if (offline) View.VISIBLE else View.GONE
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = false
            when (state) {
                is ResultState.Loading -> binding.propertiesProgress.visibility = View.VISIBLE
                is ResultState.Success -> {
                    binding.propertiesProgress.visibility = View.GONE
                    val list = if (binding.nearCampusSwitch.isChecked) {
                        viewModel.sortByDistanceToCampus(true, lastCampusLat, lastCampusLng)
                    } else state.data
                    adapter.submitList(list)
                }
                is ResultState.Error -> {
                    binding.propertiesProgress.visibility = View.GONE
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        loadProperties()
    }

    private fun loadProperties() {
        val online = NetworkUtils.isOnline(requireContext())
        viewModel.load(online)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
