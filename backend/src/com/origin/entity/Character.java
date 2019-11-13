package com.origin.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "characters")
public class Character
{
	@Id
	private int _id;

	@Column(name = "name")
	private String _name;

}
