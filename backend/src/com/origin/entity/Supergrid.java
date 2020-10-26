package com.origin.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * содержит в себе гриды (более мелкие куски карты)
 */
@Entity
@Table(name = "supergrids")
public class Supergrid
{
	/**
	 * размер супергрида в гридах
	 */
	public static final int SIZE = 50;

	@Id
	@Column(name = "id", columnDefinition = "INT(11) UNSIGNED NOT NULL AUTO_INCREMENT")
	private int _id;

	@Column(name = "continentId", columnDefinition = "INT(11) UNSIGNED NOT NULL")
	private int _continentId;

	@Column(name = "x", columnDefinition = "INT(11) UNSIGNED NOT NULL")
	private int _x;

	@Column(name = "y", columnDefinition = "INT(11) UNSIGNED NOT NULL")
	private int _y;
}
