package jp.techacademy.yasuo.nagaro.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import jp.techacademy.yasuo.nagaro.qa_app.databinding.ListQuestionsBinding

class QuestionsListAdapter(context: Context) : BaseAdapter() {
    private var layoutInflater: LayoutInflater
    private var questionArrayList = ArrayList<Question>()

    init {
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return questionArrayList.size
    }

    override fun getItem(position: Int): Any {
        return questionArrayList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // ViewBindingを使うための設定
        val binding = if (convertView == null) {
            ListQuestionsBinding.inflate(layoutInflater, parent, false)
        } else {
            ListQuestionsBinding.bind(convertView)
        }
        val view: View = convertView ?: binding.root

        binding.titleTextView.text = questionArrayList[position].title
        binding.nameTextView.text = questionArrayList[position].name
        binding.resTextView.text = questionArrayList[position].answers.size.toString()

        val bytes = questionArrayList[position].imageBytes
        if (bytes.isNotEmpty()) {
            val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                .copy(Bitmap.Config.ARGB_8888, true)
            binding.imageView.setImageBitmap(image)
        }

        return view
    }

    fun setQuestionArrayList(questionArrayList: ArrayList<Question>) {
        this.questionArrayList = questionArrayList
    }
}