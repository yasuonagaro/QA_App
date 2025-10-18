package jp.techacademy.yasuo.nagaro.qa_app

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import jp.techacademy.yasuo.nagaro.qa_app.databinding.ActivityAnswerSendBinding

class AnswerSendActivity : AppCompatActivity(), View.OnClickListener,
    DatabaseReference.CompletionListener {
    private lateinit var binding: ActivityAnswerSendBinding

    private lateinit var question: Question

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnswerSendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 渡ってきたQuestionのオブジェクトを保持する
        // API33以上でgetSerializableExtra(key)が非推奨となったため処理を分岐
        @Suppress("UNCHECKED_CAST", "DEPRECATION", "DEPRECATED_SYNTAX_WITH_DEFINITELY_NOT_NULL")
        question = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getSerializableExtra("question", Question::class.java)!!
        else
            intent.getSerializableExtra("question") as? Question!!

        // UIの準備
        binding.sendButton.setOnClickListener(this)
    }

    override fun onComplete(databaseError: DatabaseError?, databaseReference: DatabaseReference) {
        binding.progressBar.visibility = View.GONE

        if (databaseError == null) {
            finish()
        } else {
            Snackbar.make(
                findViewById(android.R.id.content),
                getString(R.string.send_answer_failure),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    override fun onClick(v: View) {
        // キーボードが出てたら閉じる
        val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        val answerRef = dataBaseReference.child(ContentsPATH).child(question.genre.toString())
            .child(question.questionUid).child(AnswersPATH)

        val data = HashMap<String, String>()

        // UID
        data["uid"] = FirebaseAuth.getInstance().currentUser!!.uid

        // 表示名
        // Preferenceから名前を取る
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val name = sp.getString(NameKEY, "")
        data["name"] = name!!

        // 回答を取得する
        val answer = binding.answerEditText.text.toString()

        if (answer.isEmpty()) {
            // 回答が入力されていない時はエラーを表示するだけ
            Snackbar.make(v, getString(R.string.answer_error_message), Snackbar.LENGTH_LONG).show()
            return
        }
        data["body"] = answer

        binding.progressBar.visibility = View.VISIBLE
        answerRef.push().setValue(data, this)
    }
}