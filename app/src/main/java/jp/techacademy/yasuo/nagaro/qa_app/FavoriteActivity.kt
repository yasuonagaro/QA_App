package jp.techacademy.yasuo.nagaro.qa_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import jp.techacademy.yasuo.nagaro.qa_app.databinding.ActivityFavoriteBinding

class FavoriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoriteBinding

    private lateinit var database: FirebaseDatabase
    private lateinit var favoriteRef: DatabaseReference
    private lateinit var adapter: QuestionsListAdapter
    private var questionArrayList = ArrayList<Question>()

    private val favoriteListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val questionUid = dataSnapshot.key ?: ""
            val genre = (dataSnapshot.value as Map<*, *>)["genre"]?.toString() ?: ""

            if (genre.isNotEmpty()) {
                database.reference.child(ContentsPATH).child(genre).child(questionUid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val map = snapshot.value as Map<*, *>
                            val title = map["title"] as? String ?: ""
                            val body = map["body"] as? String ?: ""
                            val name = map["name"] as? String ?: ""
                            val uid = map["uid"] as? String ?: ""
                            val imageString = map["image"] as? String ?: ""
                            val bytes =
                                if (imageString.isNotEmpty()) {
                                    android.util.Base64.decode(imageString, android.util.Base64.DEFAULT)
                                } else {
                                    byteArrayOf()
                                }

                            val answerArrayList = ArrayList<Answer>()
                            val answerMap = map["answers"] as Map<*, *>?
                            if (answerMap != null) {
                                for (key in answerMap.keys) {
                                    val temp = answerMap[key] as Map<*, *>
                                    val answerBody = temp["body"] as? String ?: ""
                                    val answerName = temp["name"] as? String ?: ""
                                    val answerUid = temp["uid"] as? String ?: ""
                                    val answer = Answer(answerBody, answerName, answerUid, key as String)
                                    answerArrayList.add(answer)
                                }
                            }

                            val question = Question(
                                title,
                                body,
                                name,
                                uid,
                                snapshot.key ?: "",
                                genre.toInt(),
                                bytes,
                                answerArrayList
                            )
                            questionArrayList.add(question)
                            adapter.notifyDataSetChanged()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // N/A
                        }
                    })
            }
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            val questionUid = dataSnapshot.key ?: ""
            
            for (i in 0 until questionArrayList.size) {
                if (questionArrayList[i].questionUid == questionUid) {
                    questionArrayList.removeAt(i)
                    adapter.notifyDataSetChanged()
                    return
                }
            }
        }
        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
        override fun onCancelled(databaseError: DatabaseError) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "お気に入り一覧"

        database = FirebaseDatabase.getInstance()

        adapter = QuestionsListAdapter(this)
        adapter.setQuestionArrayList(questionArrayList)
        binding.listView.adapter = adapter

        binding.listView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", questionArrayList[position])
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
        } else {
            questionArrayList.clear()
            favoriteRef = database.reference.child(FavoritesPATH).child(user.uid)
            favoriteRef.addChildEventListener(favoriteListener)
        }
    }

    override fun onPause() {
        super.onPause()
        if (this::favoriteRef.isInitialized) {
            favoriteRef.removeEventListener(favoriteListener)
        }
    }
}
