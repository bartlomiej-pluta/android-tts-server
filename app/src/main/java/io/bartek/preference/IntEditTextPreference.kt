package io.bartek.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.EditTextPreference
import java.lang.Integer.parseInt

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

    override fun getPersistedString(defaultReturnValue: String?) =
        getPersistedInt(Integer.valueOf(defaultReturnValue ?: "-1")).toString()

    override fun persistString(value: String?) = persistInt(Integer.valueOf(value ?: "-1"))


}