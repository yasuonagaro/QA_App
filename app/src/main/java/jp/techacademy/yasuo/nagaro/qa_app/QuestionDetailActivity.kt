package jp.techacademy.yasuo.nagaro.qa_app

import QuestionDetailListAdapter
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import jp.techacademy.yasuo.nagaro.qa_app.databinding.ActivityQuestionDetailBinding

class QuestionDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuestionDetailBinding

    private lateinit var question: Question
    private lateinit var adapter: QuestionDetailListAdapter
    private lateinit var answerRef: DatabaseReference
    private var favoriteRef: DatabaseReference? = null

    private val eventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<*, *>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in question.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] as? String ?: ""
            val name = map["name"] as? String ?: ""
            val uid = map["uid"] as? String ?: ""

            val answer = Answer(body, name, uid, answerUid)
            question.answers.add(answer)
            adapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
        override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
        override fun onCancelled(databaseError: DatabaseError) {}
    }

    private var favoriteListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 渡ってきたQuestionのオブジェクトを保持する
        @Suppress("UNCHECKED_CAST", "DEPRECATION", "DEPRECATED_SYNTAX_WITH_DEFINITELY_NOT_NULL")
        question = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getSerializableExtra("question", Question::class.java)!!
        else
            intent.getSerializableExtra("question") as? Question!!

        title = question.title

        // ListViewの準備
        adapter = QuestionDetailListAdapter(this, question)
        binding.listView.adapter = adapter
        adapter.notifyDataSetChanged()

        binding.fab.setOnClickListener {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動する
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", question)
                startActivity(intent)
            }
        }

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        answerRef = dataBaseReference.child(ContentsPATH).child(question.genre.toString())
            .child(question.questionUid).child(AnswersPATH)
        answerRef.addChildEventListener(eventListener)

        // お気に入り登録処理
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            binding.favoriteButton.visibility = View.VISIBLE

            favoriteRef = dataBaseReference.child(FavoritesPATH).child(user.uid).child(question.questionUid)
            favoriteListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    question.isFavorite = snapshot.exists()
                    updateFavoriteButton()
                }

                override fun onCancelled(error: DatabaseError) {
                    // N/A
                }
            }
            favoriteRef!!.addValueEventListener(favoriteListener!!)

            binding.favoriteButton.setOnClickListener {
                if (question.isFavorite) {
                    favoriteRef!!.removeValue()
                } else {
                    val data = HashMap<String, String>()
                    data["genre"] = question.genre.toString()
                    favoriteRef!!.setValue(data)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (favoriteListener != null) {
            favoriteRef?.removeEventListener(favoriteListener!!)
        }
    }

    private fun updateFavoriteButton() {
        if (question.isFavorite) {
            binding.favoriteButton.setImageResource(R.drawable.ic_star)
        } else {
            binding.favoriteButton.setImageResource(R.drawable.ic_star_border)
        }
    }
}
