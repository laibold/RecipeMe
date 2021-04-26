package de.hs_rm.recipe_me

import android.text.Editable
import android.text.InputFilter

class EditableMock : Editable {

    private lateinit var str: String

    constructor(str: String) {
        this.str = str
    }

    constructor(int: Int) {
        this.str = int.toString()
    }

    constructor(double: Double) {
        this.str = double.toString()
    }

    override fun get(index: Int): Char {
        return str[index]
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return str.subSequence(startIndex, endIndex)
    }

    override fun getChars(start: Int, end: Int, dest: CharArray?, destoff: Int) {}

    override fun <T : Any?> getSpans(start: Int, end: Int, type: Class<T>?): Array<T> {
        throw RuntimeException("Stub!")
    }

    override fun getSpanStart(tag: Any?): Int {
        return 0
    }

    override fun getSpanEnd(tag: Any?): Int {
        return 0
    }

    override fun getSpanFlags(tag: Any?): Int {
        return 0
    }

    override fun nextSpanTransition(start: Int, limit: Int, type: Class<*>?): Int {
        return 0
    }

    override fun setSpan(what: Any?, start: Int, end: Int, flags: Int) {}

    override fun removeSpan(what: Any?) {}

    override fun append(text: CharSequence?): Editable {
        return this
    }

    override fun append(text: CharSequence?, start: Int, end: Int): Editable {
        return this
    }

    override fun append(text: Char): Editable {
        return this
    }

    override fun replace(st: Int, en: Int, source: CharSequence?, start: Int, end: Int): Editable {
        return this
    }

    override fun replace(st: Int, en: Int, text: CharSequence?): Editable {
        return this
    }

    override fun insert(where: Int, text: CharSequence?, start: Int, end: Int): Editable {
        return this
    }

    override fun insert(where: Int, text: CharSequence?): Editable {
        return this
    }

    override fun delete(st: Int, en: Int): Editable {
        return this
    }

    override fun clear() {}

    override fun clearSpans() {}

    override fun setFilters(filters: Array<out InputFilter>?) {}

    override fun getFilters(): Array<InputFilter> {
        return arrayOf()
    }

    override fun toString(): String {
        return str
    }

    override val length: Int
        get() = str.length
}