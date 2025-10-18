import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import jp.techacademy.yasuo.nagaro.qa_app.Question
import jp.techacademy.yasuo.nagaro.qa_app.databinding.ListAnswerBinding
import jp.techacademy.yasuo.nagaro.qa_app.databinding.ListQuestionDetailBinding

class QuestionDetailListAdapter(context: Context, private val question: Question) : BaseAdapter() {
    companion object {
        private const val TYPE_QUESTION = 0
        private const val TYPE_ANSWER = 1
    }

    private var layoutInflater: LayoutInflater

    init {
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return 1 + question.answers.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_QUESTION
        } else {
            TYPE_ANSWER
        }
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Any {
        return question
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        if (getItemViewType(position) == TYPE_QUESTION) {
            // ViewBindingを使うための設定
            val binding = if (convertView == null) {
                ListQuestionDetailBinding.inflate(layoutInflater, parent, false)
            } else {
                ListQuestionDetailBinding.bind(convertView)
            }
            val view: View = convertView ?: binding.root

            binding.bodyTextView.text = question.body
            binding.nameTextView.text = question.name

            val bytes = question.imageBytes
            if (bytes.isNotEmpty()) {
                val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    .copy(Bitmap.Config.ARGB_8888, true)
                binding.imageView.setImageBitmap(image)
            }

            return view
        } else {
            // ViewBindingを使うための設定
            val binding = if (convertView == null) {
                ListAnswerBinding.inflate(layoutInflater, parent, false)
            } else {
                ListAnswerBinding.bind(convertView)
            }
            val view: View = convertView ?: binding.root

            binding.bodyTextView.text = question.answers[position - 1].body
            binding.nameTextView.text = question.answers[position - 1].name

            return view
        }
    }
}