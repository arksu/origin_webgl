package com.origin.model;

import com.origin.entity.InventoryItem;

/**
 * кукла персонажа, что одето на персе
 */
public class Paperdoll
{
	public enum Slot
	{
		LEFT_HAND(1),
		RIGHT_HAND(2),
		HEAD(3),
		EYES(4),
		BODY(5),
		LEGS(6);

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

	/**
	 * храним вещи одетые на сущность в массиве
	 * доступ к элементам по индексу {@see Paperdoll.Slot}
	 */
	private InventoryItem[] _items;

}
