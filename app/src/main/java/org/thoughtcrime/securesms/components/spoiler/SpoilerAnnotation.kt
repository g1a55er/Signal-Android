package org.thoughtcrime.securesms.components.spoiler

import android.text.Annotation
import android.text.Selection
import android.text.Spannable
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView

/**
 * Helper for applying spans to text that should be rendered as a spoiler. Also
 * tracks spoilers that have been revealed or not.
 */
object SpoilerAnnotation {

  private const val SPOILER_ANNOTATION = "spoiler"
  private val revealedSpoilers = mutableSetOf<String>()

  @JvmStatic
  fun spoilerAnnotation(hash: Int): Annotation {
    return Annotation(SPOILER_ANNOTATION, hash.toString())
  }

  @JvmStatic
  fun isSpoilerAnnotation(annotation: Any): Boolean {
    return SPOILER_ANNOTATION == (annotation as? Annotation)?.key
  }

  fun getSpoilerAndClickAnnotations(spanned: Spanned, start: Int = 0, end: Int = spanned.length): Map<Annotation, SpoilerClickableSpan?> {
    val spoilerAnnotations: Map<Pair<Int, Int>, Annotation> = spanned.getSpans(start, end, Annotation::class.java)
      .filter { isSpoilerAnnotation(it) }
      .associateBy { (spanned.getSpanStart(it) to spanned.getSpanEnd(it)) }

    val spoilerClickSpans: Map<Pair<Int, Int>, SpoilerClickableSpan> = spanned.getSpans(start, end, SpoilerClickableSpan::class.java)
      .associateBy { (spanned.getSpanStart(it) to spanned.getSpanEnd(it)) }

    return spoilerAnnotations
      .map { (position, annotation) ->
        annotation to spoilerClickSpans[position]
      }
      .toMap()
  }

  @JvmStatic
  fun getSpoilerAnnotations(spanned: Spanned, start: Int, end: Int): List<Annotation> {
    return spanned
      .getSpans(start, end, Annotation::class.java)
      .filter { isSpoilerAnnotation(it) }
  }

  @JvmStatic
  fun resetRevealedSpoilers() {
    revealedSpoilers.clear()
  }

  class SpoilerClickableSpan(private val spoiler: Annotation) : ClickableSpan() {
    val spoilerRevealed
      get() = revealedSpoilers.contains(spoiler.value)

    override fun onClick(widget: View) {
      revealedSpoilers.add(spoiler.value)

      if (widget is TextView) {
        val text = widget.text
        if (text is Spannable) {
          Selection.removeSelection(text)
        }
      }
    }

    override fun updateDrawState(ds: TextPaint) = Unit
  }
}
