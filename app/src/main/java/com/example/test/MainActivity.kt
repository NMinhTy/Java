package com.example.test

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.test.databinding.ActivityMainBinding
import com.google.firebase.database.*


class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    private var database: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sử dụng Data Binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Kết nối tới Firebase Realtime Database
        database = FirebaseDatabase
            .getInstance("https://test-197f9-default-rtdb.firebaseio.com")
            .reference

        // Đọc dữ liệu nhiệt độ và độ ẩm từ Firebase
        fetchDHTData()

        // Gắn sự kiện cho các nút điều khiển relay
        setupRelayButtons()
    }

    private fun fetchDHTData() {
        database!!.child("DHT11").addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Lấy dữ liệu và chuyển đổi đúng kiểu (Sử dụng Double nếu cần)
                    val temperature = snapshot.child("Temperature").getValue(Double::class.java)
                    val humidity = snapshot.child("Humidity").getValue(Double::class.java)

                    // Cập nhật UI bằng Data Binding (Chuyển đổi sang String nếu cần thiết)
                    binding!!.tvTemperature.text = "Temperature: " + (temperature?.toString() ?: "--") + " °C"
                    binding!!.tvHumidity.text = "Humidity: " + (humidity?.toString() ?: "--") + " %"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@MainActivity,
                    "Failed to fetch data: " + error.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setupRelayButtons() {
        setupRelayButton(binding!!.btnRelay1, "Relay1")
        setupRelayButton(binding!!.btnRelay2, "Relay2")
        setupRelayButton(binding!!.btnRelay3, "Relay3")
        setupRelayButton(binding!!.btnRelay4, "Relay4")
    }

    private fun setupRelayButton(button: View, relayKey: String) {
        button.setOnClickListener {
            toggleRelay(relayKey)
        }
    }

    private fun toggleRelay(relayKey: String) {
        database!!.child(relayKey).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Lấy dữ liệu relay dưới dạng String, sau đó cố gắng chuyển thành Long
                    val currentStateString = snapshot.getValue(String::class.java)

                    // Nếu giá trị là String, chuyển đổi nó thành Long
                    val currentStateLong = currentStateString?.toLong() ?: 0L

                    // Đảo ngược trạng thái của relay
                    val newState = if (currentStateLong == 1L) "0" else "1"

                    // Cập nhật trạng thái mới của relay (dưới dạng String)
                    database!!.child(relayKey).setValue(newState)
                        .addOnSuccessListener { aVoid: Void? ->
                            // Hiển thị trạng thái mới của relay sau khi cập nhật
                            val newStateText = if (newState == "0") "ON" else "OFF"

                            Toast.makeText(
                                this@MainActivity,
                                "$relayKey updated to $newStateText",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Cập nhật UI để hiển thị trạng thái mới
                            updateRelayButtonState(relayKey, newStateText)
                        }
                        .addOnFailureListener { e: Exception ->
                            Toast.makeText(
                                this@MainActivity,
                                "Failed to update " + relayKey + ": " + e.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to toggle $relayKey", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Hàm để cập nhật trạng thái trên UI (button)
    private fun updateRelayButtonState(relayKey: String, newStateText: String) {
        when (relayKey) {
            "Relay1" -> binding!!.btnRelay1.text = newStateText
            "Relay2" -> binding!!.btnRelay2.text = newStateText
            "Relay3" -> binding!!.btnRelay3.text = newStateText
            "Relay4" -> binding!!.btnRelay4.text = newStateText
        }
    }

}
