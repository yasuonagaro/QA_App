package jp.techacademy.yasuo.nagaro.qa_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import jp.techacademy.yasuo.nagaro.qa_app.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding

    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Preferenceから表示名を取得してEditTextに反映させる
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val name = sp.getString(NameKEY, "")
        binding.nameText.setText(name)

        databaseReference = FirebaseDatabase.getInstance().reference

        // UIの初期設定
        title = getString(R.string.settings_title)

        binding.changeButton.setOnClickListener { v ->
            // キーボードが出ていたら閉じる
            val im = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていない場合は何もしない
                Snackbar.make(v, getString(R.string.no_login_user), Snackbar.LENGTH_LONG).show()
            } else {
                // 変更した表示名をFirebaseに保存する
                val name2 = binding.nameText.text.toString()
                val userRef = databaseReference.child(UsersPATH).child(user.uid)
                val data = HashMap<String, String>()
                data["name"] = name2
                userRef.setValue(data)

                // 変更した表示名をPreferenceに保存する
                val sp2 = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                val editor = sp2.edit()
                editor.putString(NameKEY, name2)
                editor.apply()

                Snackbar.make(v, getString(R.string.change_display_name), Snackbar.LENGTH_LONG)
                    .show()
            }
        }

        binding.logoutButton.setOnClickListener { v ->
            FirebaseAuth.getInstance().signOut()
            binding.nameText.setText("")
            Snackbar.make(v, getString(R.string.logout_complete_message), Snackbar.LENGTH_LONG)
                .show()
        }
    }
}