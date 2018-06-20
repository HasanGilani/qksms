package feature.compose.part

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import common.Navigator
import common.base.QkAdapter
import common.base.QkViewHolder
import common.util.Colors
import common.util.extensions.forwardTouches
import feature.compose.BubbleUtils.canGroup
import kotlinx.android.synthetic.main.message_list_item_in.view.*
import model.Message
import model.MmsPart
import util.extensions.isImage
import util.extensions.isVCard
import util.extensions.isVideo

class PartsAdapter(context: Context, navigator: Navigator, theme: Colors.Theme) : QkAdapter<MmsPart>() {

    private val partBinders = listOf(
            MediaBinder(context, navigator),
            VCardBinder(context, navigator, theme)
    )

    private lateinit var message: Message
    private var previous: Message? = null
    private var next: Message? = null
    private var messageView: View? = null
    private var bodyVisible: Boolean = true

    fun setData(message: Message, previous: Message?, next: Message?, messageView: View) {
        this.message = message
        this.previous = previous
        this.next = next
        this.messageView = messageView
        this.bodyVisible = messageView.body.visibility == View.VISIBLE
        this.data = message.parts.filter { it.isImage() || it.isVideo() || it.isVCard() }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val layout = partBinders.getOrNull(viewType)?.partLayout ?: 0
        return QkViewHolder(LayoutInflater.from(parent.context).inflate(layout, parent, false))
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val part = data[position]
        val view = holder.itemView

        messageView?.let(view::forwardTouches)

        val canGroupWithPrevious = canGroup(message, previous) || position > 0
        val canGroupWithNext = canGroup(message, next) || position < itemCount - 1 || bodyVisible

        partBinders
                .firstOrNull { it.canBindPart(part) }
                ?.bindPart(view, part, message, canGroupWithPrevious, canGroupWithNext)
    }

    override fun getItemViewType(position: Int): Int {
        val part = data[position]
        return partBinders.indexOfFirst { it.canBindPart(part) }
    }

}