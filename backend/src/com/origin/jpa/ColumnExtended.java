package com.origin.jpa;

import javax.persistence.Id;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * расширяем данные для колонки сущности
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ColumnExtended
{
	/**
	 * надо ли обновлять ид поле после инсерта. записать в него полученный ид
	 * можем навесить только на {@link Id} поле
	 */
	boolean updateInsertId() default false;
}
