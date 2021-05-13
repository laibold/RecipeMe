package de.hs_rm.recipe_me.declaration

import org.mockito.Mockito

fun <T> anyNotNull(type: Class<T>): T = Mockito.any(type)

fun <T> eqNotNull(obj: T): T = Mockito.eq(obj)
