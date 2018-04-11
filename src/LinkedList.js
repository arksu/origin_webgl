export default class {
    constructor() {
        this._head = null;
        this._length = 0;
    }

    addToHead(value) {
        const newNode = {value};
        newNode.next = this._head;
        this._head = newNode;
        this._length++;
        return this;
    }

    removeFromHead() {
        if (this._length === 0) {
            return undefined;
        }

        const value = this._head.value;
        this._head = this._head.next;
        this._length--;

        return value;
    }

    find(val) {
        let thisNode = this._head;

        while (thisNode) {
            if (thisNode.value === val) {
                return thisNode;
            }

            thisNode = thisNode.next;
        }

        return thisNode;
    }

    remove(val) {
        if (this._length === 0) {
            return undefined;
        }

        if (this._head.value === val) {
            return this.removeFromHead();
        }

        let previousNode = this._head;
        let thisNode = previousNode.next;

        while (thisNode) {
            if (thisNode.value === val) {
                break;
            }

            previousNode = thisNode;
            thisNode = thisNode.next;
        }

        if (thisNode === null) {
            return undefined;
        }

        previousNode.next = thisNode.next;
        this._length--;
        return this;
    }
}