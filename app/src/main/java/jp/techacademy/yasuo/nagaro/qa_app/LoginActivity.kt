package jp.techacademy.yasuo.nagaro.qa_app
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.preference.PreferenceManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import jp.techacademy.yasuo.nagaro.qa_app.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var createAccountListener: OnCompleteListener<AuthResult>
    private lateinit var loginListener: OnCompleteListener<AuthResult>
    private lateinit var databaseReference: DatabaseReference

    // アカウント作成時にフラグを立て、ログイン処理後に名前をFirebaseに保存する
    private var isCreateAccount = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseReference = FirebaseDatabase.getInstance().reference

        // FirebaseAuthのオブジェクトを取得する
        auth = FirebaseAuth.getInstance()

        // アカウント作成処理のリスナー
        createAccountListener = OnCompleteListener { task ->
            if (task.isSuccessful) {
                // 成功した場合
                // ログインを行う
                val email = binding.emailText.text.toString()
                val password = binding.passwordText.text.toString()
                login(email, password)
            } else {

                // 失敗した場合
                // エラーを表示する
                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(
                    view,
                    getString(R.string.create_account_failure_message),
                    Snackbar.LENGTH_LONG
                ).show()

                // プログレスバーを非表示にする
                binding.progressBar.visibility = View.GONE
            }
        }

        // ログイン処理のリスナー
        loginListener = OnCompleteListener { task ->
            if (task.isSuccessful) {
                // 成功した場合
                val user = auth.currentUser
                val userRef = databaseReference.child(UsersPATH).child(user!!.uid)

                if (isCreateAccount) {
                    // アカウント作成の時は表示名をFirebaseに保存する
                    val name = binding.nameText.text.toString()

                    val data = HashMap<String, String>()
                    data["name"] = name
                    userRef.setValue(data)

                    // 表示名をPreferenceに保存する
                    saveName(name)
                } else {
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val data = snapshot.value as Map<*, *>?
                            saveName(data!!["name"] as String)
                        }

                        override fun onCancelled(firebaseError: DatabaseError) {}
                    })
                }

                // プログレスバーを非表示にする
                binding.progressBar.visibility = View.GONE

                // Activityを閉じる
                finish()

            } else {
                // 失敗した場合
                // エラーを表示する
                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(view, getString(R.string.login_failure_message), Snackbar.LENGTH_LONG)
                    .show()

                // プログレスバーを非表示にする
                binding.progressBar.visibility = View.GONE
            }
        }

        // タイトルの設定
        title = getString(R.string.login_title)

        binding.createButton.setOnClickListener { v ->
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val email = binding.emailText.text.toString()
            val password = binding.passwordText.text.toString()
            val name = binding.nameText.text.toString()

            if (email.isNotEmpty() && password.length >= 6 && name.isNotEmpty()) {
                // ログイン時に表示名を保存するようにフラグを立てる
                isCreateAccount = true

                createAccount(email, password)
            } else {
                // エラーを表示する
                Snackbar.make(v, getString(R.string.login_error_message), Snackbar.LENGTH_LONG)
                    .show()
            }
        }

        binding.loginButton.setOnClickListener { v ->
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val email = binding.emailText.text.toString()
            val password = binding.passwordText.text.toString()

            if (email.isNotEmpty() && password.length >= 6) {
                // フラグを落としておく
                isCreateAccount = false

                login(email, password)
            } else {
                // エラーを表示する
                Snackbar.make(v, getString(R.string.login_error_message), Snackbar.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun createAccount(email: String, password: String) {
        // プログレスバーを表示する
        binding.progressBar.visibility = View.VISIBLE

        // アカウントを作成する
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(createAccountListener)
    }

    private fun login(email: String, password: String) {
        // プログレスバーを表示する
        binding.progressBar.visibility = View.VISIBLE

        // ログインする
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(loginListener)
    }

    private fun saveName(name: String) {
        // Preferenceに保存する
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sp.edit()
        editor.putString(NameKEY, name)
        editor.apply()
    }
}