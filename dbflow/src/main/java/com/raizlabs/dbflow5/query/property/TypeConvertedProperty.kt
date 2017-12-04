package com.raizlabs.dbflow5.query.property

import com.raizlabs.dbflow5.adapter.ModelAdapter
import com.raizlabs.dbflow5.converter.TypeConverter
import com.raizlabs.dbflow5.query.NameAlias
import com.raizlabs.dbflow5.query.Operator

/**
 * Description: Provides convenience methods for [TypeConverter] when constructing queries.
 *
 * @author Andrew Grosner (fuzz)
 */

class TypeConvertedProperty<T, V> : Property<V> {

    private var databaseProperty: TypeConvertedProperty<V, T>? = null

    private var convertToDB: Boolean = false

    private val getter: TypeConverterGetter

    override val operator: Operator<V>
        get() = Operator.op(nameAlias, getter.getTypeConverter(table!!), convertToDB)

    /**
     * Generated by the compiler, looks up the type converter based on [ModelAdapter] when needed.
     * This is so we can properly retrieve the type converter at any time.
     */
    interface TypeConverterGetter {

        fun getTypeConverter(modelClass: Class<*>): TypeConverter<*, *>
    }

    constructor(table: Class<*>, nameAlias: NameAlias,
                convertToDB: Boolean,
                getter: TypeConverterGetter) : super(table, nameAlias) {
        this.convertToDB = convertToDB
        this.getter = getter
    }

    constructor(table: Class<*>, columnName: String,
                convertToDB: Boolean,
                getter: TypeConverterGetter) : super(table, columnName) {
        this.convertToDB = convertToDB
        this.getter = getter
    }

    /**
     * @return A new [Property] that corresponds to the inverted type of the [TypeConvertedProperty].
     * Provides a convenience for supplying type converted methods within the DataClass of the [TypeConverter]
     */
    fun invertProperty(): Property<T> {
        if (databaseProperty == null) {
            databaseProperty = TypeConvertedProperty(table!!, nameAlias,
                !convertToDB, object : TypeConverterGetter {
                override fun getTypeConverter(modelClass: Class<*>): TypeConverter<*, *> =
                    getter.getTypeConverter(modelClass)
            })
        }
        return databaseProperty!!
    }

    override fun withTable(tableNameAlias: NameAlias): Property<V> {
        val nameAlias = this.nameAlias
            .newBuilder()
            .withTable(tableNameAlias.query)
            .build()
        return TypeConvertedProperty<Any, V>(this.table!!, nameAlias, this.convertToDB, this.getter)
    }
}
