package com.origin.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "characters")
public class Character
{
	@Id
	private int _id;

	private String _name;

}
