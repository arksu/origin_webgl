package com.origin.entity;

import com.origin.jpa.TableExtended;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "characters")
@TableExtended(truncate = true, drop = true)
public class Character
{
	@Id
	@Column(name = "id")
	private int _id;

	@Column(name = "name")
	private String _name;

	@Column(name = "createTime", columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP")
	private Timestamp _createTime;

}
