package com.origin.jpa;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * расширенная аннотация для таблицы
 * добавляем некоторые данные для удобства
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableExtended
{
	/**
	 * суффикс создания таблицы (можно указать движок, кодировку, комментарий)
	 */
	String creationSuffix();

	/**
	 * надо ли создавать таблицу при инициализации
	 */
	boolean create() default true;
}
