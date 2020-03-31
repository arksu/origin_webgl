package com.origin.model;

import com.origin.entity.InventoryItem;

/**
 * кукла персонажа, что одето на персе
 */
public class Paperdoll
{
	public enum Slot
	{
		LEFT_HAND(0),
		RIGHT_HAND(1),
		HEAD(2),
		BODY(3),
		LEGS(4);

		private final int _id;

		Slot(int id)
		{
			_id = id;
		}

		public int getId()
		{
			return _id;
		}
	}

	private InventoryItem[] _items;

}
