package capstone.myapplication.bottomNav.Home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.SimpleAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import capstone.myapplication.BuildConfig
import capstone.myapplication.R
import capstone.myapplication.databinding.FragmentHomeBinding
import capstone.myapplication.databinding.ItemWeatherBinding
import capstone.myapplication.view.ArticleActivity
import com.bumptech.glide.Glide
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import org.json.JSONObject
import java.util.*

class HomeFragment : Fragment(), AdapterView.OnItemClickListener {

    private lateinit var locationManager: LocationManager
    private lateinit var binding: ItemWeatherBinding
    private lateinit var homeBinding: FragmentHomeBinding
    private var mContext: Context? = null
    private lateinit var cityName: String
    private lateinit var aqi: String
    private lateinit var adapter: SimpleAdapter
    private lateinit var map: HashMap<String, String>
    private lateinit var mylist: ArrayList<HashMap<String, String>>
    private lateinit var title: Array<String>
    private lateinit var caption: Array<String>
    private lateinit var image: Array<String>
    private lateinit var link: Array<String>


    companion object{
        private var city = " "
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeBinding = FragmentHomeBinding.bind(view)
        binding = homeBinding.itemWeather

        binding.location.visibility = View.INVISIBLE
        binding.weather.visibility = View.INVISIBLE

        addArticle()

        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 101
            )
        }

        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
        locationEnable(locationManager)
        homeBinding.listView.setOnItemClickListener(this)
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (mContext != null){
                val geocoder = Geocoder(mContext!!, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                cityName = addresses[0].subLocality
                getParams(cityName)
            }
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private fun addArticle() {
        title = arrayOf(
            "This bird’s protectors are its former hunters: ‘It was my turn to help them’",
            "How one tiny island is rallying to save a critically endangered parrot",
            "Never-before-seen colorful bird hybrid surprises scientists"
        )
        caption = arrayOf(
            "A scarlet macaw perches on a tree branch at sunrise with the full moon in the background in La Mosquitia, Honduras. The region is home to the largest wilderness area in Central America and is the only place in the country where these birds fly freely.",
            "Only 1,500 Rimatara lorikeets remain in the wild. On one French Polynesian island, residents fight to preserve lorikeet habitat and combat invasive rats.",
            "The offspring of a scarlet tanager and rose-breasted grosbeak—distantly related birds whose evolutionary paths diverged 10 million years ago—was recently found in Pennsylvania."
        )
        image = arrayOf(
            Integer.toString(R.drawable.images1),
            Integer.toString(R.drawable.bird2),
            Integer.toString(R.drawable.bird1)
        )
        link = arrayOf(
            "https://www.nationalgeographic.com/animals/article/miskito-scarlet-macaw-conservation-honduras/",
            "https://www.nationalgeographic.com/animals/article/rare-bird-hybrid-distantly-related-parents-scarlet-tanager-grosbeak#:~:text=Never-before-seen%20colorful%20bird%20hybrid%20surprises%20scientists%20The%20offspring,between%20a%20rose-breasted%20grosbeak%20and%20a%20scarlet%20tanager.",
            "https://www.nationalgeographic.com/animals/article/how-one-tiny-island-is-rallying-to-save-a-critically-endangered-parrot#:~:text=How%20one%20tiny%20island%20is%20rallying%20to%20save,Photograph%20by%20AGAMI%20Photo%20Agency%2C%20Alamy%20Stock%20Photo"
        )
        mylist = ArrayList()

        for (i in 0 until title.size) {
            map = HashMap()
            map["Title"] = title[i]
            map["Caption"] = caption[i]
            map["Photo"] = image[i]
            map["Link"] = link[i]
            mylist.add(map)
        }
        adapter = SimpleAdapter(activity, mylist, R.layout.item_list, arrayOf("Title", "Caption", "Photo"), intArrayOf(R.id.title_article, R.id.date_article, R.id.image_article))
        homeBinding.listView.setAdapter(adapter)
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val getPosition = link.get(position)
        val intent = Intent(activity, ArticleActivity::class.java)
        intent.putExtra(ArticleActivity.link, getPosition)
        startActivity(intent)
    }

    private fun locationEnable(lm: LocationManager) {
        var gps_enabled = false
        var network_enabled = false
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (!gps_enabled && !network_enabled) {
            AlertDialog.Builder(requireActivity())
                .setTitle("Enable GPS Service")
                .setMessage("We need your GPS location to show Near Places around you.")
                .setCancelable(false)
                .setPositiveButton("Enable") { _, _ -> startActivity(
                    Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS
                    )
                ) }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        if(city != null){
            getParams(city)
        } else {
            locationListener
        }
    }

    override fun onPause() {
        super.onPause()
        city = binding.location.text.toString()
        Log.d("HomeFragment", binding.location.text.toString())
    }

    private fun getParams(cityName: String) {
        val params = RequestParams()
        params.put("q", cityName)
        params.put("appid", BuildConfig.API_KEY)
        getWeatherData(params)
    }

    private fun getWeatherData(params: RequestParams) {
        val client = AsyncHttpClient()
        client.get(BuildConfig.URL_WEATHER, params, object : AsyncHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray
            ) {
                val result = String(responseBody)
                val responseObject = JSONObject(result)
                val weatherD = DataWeather.fromJson(responseObject)
                showData(weatherD)
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray?,
                error: Throwable?
            ) {}
        })
    }

    @SuppressLint("SetTextI18n")
    private fun showData(weatherD: DataWeather) {

        binding.apply {
            temperature.text = weatherD.getTemperature()
            location.text = weatherD.getCity()
            weather.text = ", " + weatherD.getWeather()
            humidity.text = weatherD.getHumidity()
        }

        binding.location.visibility = View.VISIBLE
        binding.weather.visibility = View.VISIBLE

        Glide.with(this)
            .load(BuildConfig.URL_ICON + weatherD.getIcon())
            .into(binding.imgWeather)

        city = weatherD.getCity().toString()
        getAirQuality(weatherD.getLon(), weatherD.getLat())
    }

    private fun getAirQuality(lon: String?, lat: String?) {
        val params = RequestParams()
        params.put("lat", lat)
        params.put("lon", lon)
        params.put("appid", BuildConfig.API_KEY)
        val client = AsyncHttpClient()
        client.get(BuildConfig.URL_POLLUTION, params, object : AsyncHttpResponseHandler() {

            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray
            ) {
                val result = String(responseBody)
                val responseObject = JSONObject(result)
                aqi = responseObject.getJSONArray("list").getJSONObject(0).getJSONObject("main").getInt("aqi").toString()
                binding.airQuality.text = updateAqi(aqi)
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray?,
                error: Throwable?
            ) {}

        })
    }

    private fun updateAqi(aqi: String): String {
        if(aqi == "1") {
            return "Good"
        }
        else if (aqi == "2"){
            return "Fair"
        }
        else if (aqi == "3"){
            return "Moderate"
        }
        else if (aqi == "4"){
            return "Poor"
        }
        else if (aqi == "5"){
            return "Very Poor"
        }
        return "null"
    }

    override fun onDetach() {
        super.onDetach()
        mContext = null
    }
}