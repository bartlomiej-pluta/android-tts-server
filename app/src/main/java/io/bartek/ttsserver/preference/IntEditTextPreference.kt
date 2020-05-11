package io.bartek.ttsserver.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.EditTextPreference

class IntEditTextPreference : EditTextPreference {
   constructor(
      context: Context?,
      attrs: AttributeSet?,
      defStyleAttr: Int,
      defStyleRes: Int
   ) : super(context, attrs, defStyleAttr, defStyleRes)

   constructor(
      context: Context?,
      attrs: AttributeSet?,
      defStyleAttr: Int
   ) : super(context, attrs, defStyleAttr)

   constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

   constructor(context: Context?) : super(context)

   override fun getPersistedString(defaultReturnValue: String?) = (defaultReturnValue ?: "-1")
      .let { Integer.valueOf(it) }
      .let { getPersistedInt(it) }
      .toString()

   override fun persistString(value: String?) = (value ?: "-1")
      .let { Integer.valueOf(it) }
      .let { persistInt(it) }
}